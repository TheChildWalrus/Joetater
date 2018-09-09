package joetater.common.coremod;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class JoetaterClassTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (name.equals("oi") || name.equals("net.minecraft.server.management.ServerConfigurationManager"))
		{
			return patchSCM(name, basicClass);
		}
		
		return basicClass;
	}

	private byte[] patchSCM(String name, byte[] bytes)
	{
		String targetMethodName = "allowUserToConnect";
		String targetMethodNameObf = "func_148542_a";
		String targetMethodSign = "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;";
		String targetMethodSignObf = targetMethodSign;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		for (MethodNode method : classNode.methods)
		{
			if ((method.name.equals(targetMethodName) || method.name.equals(targetMethodNameObf)) && (method.desc.equals(targetMethodSign) || method.desc.equals(targetMethodSignObf)))
			{
				int skipped = 0;
				while (true)
				{
					InsnNode targetNode = new InsnNode(Opcodes.ARETURN);
					InsnNode foundNode = findNodeInMethod(method, targetNode, skipped);
					if (foundNode != null)
					{
						InsnList newIns = new InsnList();
				        newIns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				        newIns.add(new VarInsnNode(Opcodes.ALOAD, 1));
				        newIns.add(new VarInsnNode(Opcodes.ALOAD, 2));
				        newIns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "joetater/common/coremod/ReplacedMethods$SCM", "allowUserToConnect", "(Ljava/lang/String;Lnet/minecraft/server/management/ServerConfigurationManager;Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;", false));
				        
				        method.instructions.insertBefore(foundNode, newIns);
				        
						skipped++;
					}
					else
					{
						break;
					}
				}
				
				System.out.println("JoetaterCore: Patched method " + method.name + " " + skipped + " times");
			}
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	private static <N extends AbstractInsnNode> N findNodeInMethod(MethodNode method, N target)
	{
		return findNodeInMethod(method, target, 0);
	}
	
	private static <N extends AbstractInsnNode> N findNodeInMethod(MethodNode method, N targetAbstract, final int skip)
	{
		int skipped = 0;
		
		Iterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext())
		{
			AbstractInsnNode nextAbstract = it.next();
			boolean matched = false;
			
			if (nextAbstract.getClass() == targetAbstract.getClass())
			{
				if (targetAbstract.getClass() == InsnNode.class)
				{
					InsnNode next = (InsnNode)nextAbstract;
					InsnNode target = (InsnNode)targetAbstract;
					if (next.getOpcode() == target.getOpcode())
					{
						matched = true;
					}
				}
				else if (targetAbstract.getClass() == VarInsnNode.class)
				{
					VarInsnNode next = (VarInsnNode)nextAbstract;
					VarInsnNode target = (VarInsnNode)targetAbstract;
					if (next.getOpcode() == target.getOpcode() && next.var == target.var)
					{
						matched = true;
					}
				}
				else if (targetAbstract.getClass() == LdcInsnNode.class)
				{
					LdcInsnNode next = (LdcInsnNode)nextAbstract;
					LdcInsnNode target = (LdcInsnNode)targetAbstract;
					if (next.cst.equals(target.cst))
					{
						matched = true;
					}
				}
				else if (targetAbstract.getClass() == TypeInsnNode.class)
				{
					TypeInsnNode next = (TypeInsnNode)nextAbstract;
					TypeInsnNode target = (TypeInsnNode)targetAbstract;
					if (next.getOpcode() == target.getOpcode() && next.desc.equals(target.desc))
					{
						matched = true;
					}
				}
				else if (targetAbstract.getClass() == FieldInsnNode.class)
				{
					FieldInsnNode next = (FieldInsnNode)nextAbstract;
					FieldInsnNode target = (FieldInsnNode)targetAbstract;
					if (next.getOpcode() == target.getOpcode() && next.owner.equals(target.owner) && next.name.equals(target.name) && next.desc.equals(target.desc))
					{
						matched = true;
					}
				}
				else if (targetAbstract.getClass() == MethodInsnNode.class)
				{
					MethodInsnNode next = (MethodInsnNode)nextAbstract;
					MethodInsnNode target = (MethodInsnNode)targetAbstract;
					if (next.getOpcode() == target.getOpcode() && next.owner.equals(target.owner) && next.name.equals(target.name) && next.desc.equals(target.desc) && next.itf == target.itf)
					{
						matched = true;
					}
				}
			}
			
			if (matched)
			{
				if (skipped >= skip)
				{
					return (N)nextAbstract;
				}
				else
				{
					skipped++;
				}
			}
		}
		
		return null;
	}
}
