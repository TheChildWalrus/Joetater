package joetater.common;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.Files;
import com.mojang.authlib.GameProfile;

public class CommandResetPlayer extends CommandBase
{
	private static class ResetInstance
	{
		private boolean resetting = false;
		private String resetName;
		private UUID resetID;
		
		private void clearResetInfo()
		{
			resetting = false;
			resetName = null;
			resetID = null;
		}

	}

	private Map<UUID, ResetInstance> resetInstances = new HashMap();
	
	private ResetInstance getForAdmin(EntityPlayer entityplayer)
	{
		UUID playerID = entityplayer.getPersistentID();
		ResetInstance ri = resetInstances.get(playerID);
		if (ri == null)
		{
			ri = new ResetInstance();
			resetInstances.put(playerID, ri);
		}
		return ri;
	}
	
	@Override
    public String getCommandName()
    {
        return "jResetPlayer";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jResetPlayer <player> then /jResetPlayer CONFIRM to confirm";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		MinecraftServer server = MinecraftServer.getServer();
		EntityPlayerMP user = getCommandSenderAsPlayer(sender);
		if (user != null)
		{
			ResetInstance ri = getForAdmin(user);
			
			if (args.length >= 1)
			{
				if (!ri.resetting)
				{
					String username = args[0];
					GameProfile profile = null;
					boolean isTargetOnline = false;

					ServerConfigurationManager scm = server.getConfigurationManager();
					EntityPlayerMP entityplayer = scm.func_152612_a(username);
					if (entityplayer != null)
					{
						profile = entityplayer.getGameProfile();
						isTargetOnline = true;
					}
					else
					{
			            profile = server.func_152358_ax().func_152655_a(username);
			            isTargetOnline = false;
			            if (profile == null)
			            {
			                throw new CommandException("Could not locate player data for username %s", username);
			            }
					}
					
					if (profile == null || profile.getId() == null)
					{
						throw new CommandException("Could not locate player data for username %s", username);
					}
					else
					{
						if (isTargetOnline)
						{
							String msg = "Cannot reset player data for " + username + " while they are online";
							user.addChatMessage(new ChatComponentText(msg));
							return;
						}
						
						ri.resetting = true;
						ri.resetName = username;
						ri.resetID = profile.getId();
						
						String msg = EnumChatFormatting.AQUA + "Reset data for " + username + "?";
						user.addChatMessage(new ChatComponentText(msg));
						msg = EnumChatFormatting.DARK_AQUA + "This may not work if they have been online recently!";
						user.addChatMessage(new ChatComponentText(msg));
						msg = EnumChatFormatting.AQUA + "Type " + EnumChatFormatting.GREEN + "/"  + getCommandName() + " CONFIRM" + EnumChatFormatting.AQUA + " to confirm";
						user.addChatMessage(new ChatComponentText(msg));
						return;
					}
				}
				
				if (ri.resetting)
				{
					String command = args[0];
					String resetName = ri.resetName;
					UUID resetID = ri.resetID;
					
					ri.clearResetInfo();
					
					if (resetName != null && resetID != null)
					{
						if (command.equals("CONFIRM"))
						{
							try
							{
								File worldRootDir = DimensionManager.getCurrentSaveRootDirectory();
								File copyDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "joetater" + File.separator + "player_reset");
								File copyDirPD = new File(copyDir, "playerdata");
								File copyDirLOTR = new File(copyDir, "LOTR");
								if (!copyDirPD.exists())
								{
									copyDirPD.mkdirs();
								}
								if (!copyDirLOTR.exists())
								{
									copyDirLOTR.mkdirs();
								}
	
								boolean flag = false;
								
								File dirPD = new File(worldRootDir, "playerdata");
								File playerPD = new File(dirPD, resetID.toString() + ".dat");
								if (playerPD.exists() && playerPD.isFile())
								{
									File copyPD = new File(copyDirPD, resetID.toString() + ".dat");
									if (!copyPD.exists())
									{
										copyPD.createNewFile();
									}
									Files.copy(playerPD, copyPD);
									
									playerPD.delete();
									
									flag = true;
								}

			                	File dirLOTR = new File(worldRootDir, "LOTR" + File.separator + "players");
			                    File playerLOTR = new File(dirLOTR, resetID.toString() + ".dat");
			                    if (playerLOTR.exists() && playerLOTR.isFile())
			                    {
			                    	File copyLOTR = new File(copyDirLOTR, resetID.toString() + ".dat");
			    					if (!copyLOTR.exists())
			    					{
			    						copyLOTR.createNewFile();
			    					}
			    					Files.copy(playerLOTR, copyLOTR);
			    					
			    					playerLOTR.delete();
			    					
			                    	flag = true;
			                    }
			                    
			                    if (flag)
			                    {
									String msg = EnumChatFormatting.AQUA + "Successfully attempted data reset for " + resetName + " and created backup" + EnumChatFormatting.RESET;
									func_152373_a(sender, this, msg);
									msg = EnumChatFormatting.DARK_AQUA + "This may not work if they have been online recently!";
									user.addChatMessage(new ChatComponentText(msg));
									return;
			                    }
			                    else
			                    {
			                    	throw new CommandException("No data existed for " + resetName + "!");
			                    }
							}
							catch (IOException e)
							{
								e.printStackTrace();
								throw new CommandException("Error resetting data: " + e.getLocalizedMessage());
							}
						}
					}
					else
					{
						throw new CommandException("Error resetting data");
					}
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
    	if (args.length >= 1)
    	{
    		return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    	}
    	return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return false;
    }
}
