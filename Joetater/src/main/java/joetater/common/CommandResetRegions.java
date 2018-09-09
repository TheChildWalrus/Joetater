package joetater.common;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.Files;

public class CommandResetRegions extends CommandBase
{
	private static class ResetInstance
	{
		private boolean resetting = false;
		private int xMin;
		private int xMax;
		private int zMin;
		private int zMax;
		
		private void clearResetInfo()
		{
			resetting = false;
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
        return "jDeleteRegions";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jDeleteRegions <min-x> <max-x> <min-z> <max-z> then /jResetRegions CONFIRM to confirm";
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
				if (!ri.resetting && args.length >= 4)
				{
					int xMin = parseInt(sender, args[0]);
					int xMax = parseInt(sender, args[1]);
					int zMin = parseInt(sender, args[2]);
					int zMax = parseInt(sender, args[3]);
					
					if (xMin > xMax || zMin > zMax)
					{
						throw new WrongUsageException("Minimum coordinates cannot be greater than maximum coordinates");
					}
					
					ri.resetting = true;
					ri.xMin = xMin;
					ri.xMax = xMax;
					ri.zMin = zMin;
					ri.zMax = zMax;
					
					String msg = EnumChatFormatting.AQUA + String.format("Delete all regions from r.%d.%d.mca to r.%d.%d.mca?", xMin, zMin, xMax, zMax);
					user.addChatMessage(new ChatComponentText(msg));
					msg = EnumChatFormatting.DARK_AQUA + "This may not work if regions have been loaded recently!";
					user.addChatMessage(new ChatComponentText(msg));
					msg = EnumChatFormatting.AQUA + "Type " + EnumChatFormatting.GREEN + "/"  + getCommandName() + " CONFIRM" + EnumChatFormatting.AQUA + " to confirm";
					user.addChatMessage(new ChatComponentText(msg));
					return;
				}
				
				if (ri.resetting && args[0].equals("CONFIRM"))
				{
					int xMin = ri.xMin;
					int xMax = ri.xMax;
					int zMin = ri.zMin;
					int zMax = ri.zMax;
					
					ri.clearResetInfo();

					try
					{
						World world = sender.getEntityWorld();
				        String dimFolder = world.provider.getSaveFolder();
				        if (dimFolder == null)
				        {
				        	dimFolder = "";
				        }
				        else
				        {
				        	dimFolder += File.separator;
				        }
				        dimFolder += "region";
						File worldRegionDir = new File(world.getSaveHandler().getWorldDirectory(), dimFolder);
						File copyDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "joetater" + File.separator + "deleted_regions" + File.separator + dimFolder);
						if (!copyDir.exists())
						{
							copyDir.mkdirs();
						}
						
						int deleted = 0;
						for (int i = xMin; i <= xMax; i++)
						{
							for (int k = zMin; k <= zMax; k++)
							{
								String filename = RegionSaver.getFilename(i, k);
								File regionFile = new File(worldRegionDir, filename);
								if (regionFile.exists())
								{
									File copyRegionFile = new File(copyDir, filename);
									if (!copyRegionFile.exists())
									{
										copyRegionFile.createNewFile();
									}
									Files.copy(regionFile, copyRegionFile);
									regionFile.delete();
									deleted++;
								}
							}
						}
	                    
						String msg = EnumChatFormatting.AQUA + String.format("Successfully attempted delete of %d regions in %s (%s) and created backups", deleted, (dimFolder == null ? "" : dimFolder) + "/region", world.provider.getDimensionName()) + EnumChatFormatting.RESET;
						func_152373_a(sender, this, msg);
						msg = EnumChatFormatting.DARK_AQUA + "This may not work if regions have been loaded recently!";
						user.addChatMessage(new ChatComponentText(msg));
						return;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						throw new CommandException("Error resetting regions: " + e.getLocalizedMessage());
					}
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
    	return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return false;
    }
}
