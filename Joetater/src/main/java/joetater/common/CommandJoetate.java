package joetater.common;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLLog;

public class CommandJoetate extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "joetate";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/joetate r <square-radius> [origin-x] [origin-z] OR /joetate b <min-x> <max-x> <min-z> <max-z>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		WorldServer world = (WorldServer)sender.getEntityWorld();
		
		RegionCriteria criteria = null;
		if (args.length >= 1)
		{
			String mode = args[0];
			if (mode.equals("r"))
			{
				int radius = parseIntBounded(sender, args[1], 0, JoetaterConfig.maxJoetateSize / 2);
				ChunkCoordinates coords = sender.getPlayerCoordinates();
				int posX = coords.posX;
				int posZ = coords.posZ;
				
				if (args.length >= 3)
				{
					posX = parseInt(sender, args[2]);
					posZ = parseInt(sender, args[3]);
				}
				
				criteria = RegionCriteriaFactory.inRadius(posX, posZ, radius);
			}
			else if (mode.equals("b"))
			{
				int xMin = parseInt(sender, args[1]);
				int xMax = parseInt(sender, args[2]);
				int zMin = parseInt(sender, args[3]);
				int zMax = parseInt(sender, args[4]);
				
				int width = Math.abs(xMax - xMin);
				int length = Math.abs(zMax - zMin);
				if (width > JoetaterConfig.maxJoetateSize || length > JoetaterConfig.maxJoetateSize)
				{
					throw new WrongUsageException("Maximum joetate size is " + JoetaterConfig.maxJoetateSize);
				}
				
				criteria = RegionCriteriaFactory.inBox(xMin, xMax, zMin, zMax);
			}
			
			if (criteria != null)
			{
				try
				{
					int copied = RegionSaver.saveRegions(world, criteria);
					String dimFolder = world.provider.getSaveFolder();
					func_152373_a(sender, this, String.format("Successfully joetated %d regions in %s (%s)", copied, (dimFolder == null ? "" : dimFolder) + "/region", world.provider.getDimensionName()));
					return;
				}
				catch (Exception e)
				{
					FMLLog.severe("Joetating failed!");
					e.printStackTrace();
					throw new CommandException(String.format("Joetating failed in %s - check server logs for crash report", world.provider.getDimensionName()));
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
