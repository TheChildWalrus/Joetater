package joetater.common;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class CommandSetBiome extends CommandBase
{
	private static final int MAX_RANGE = 1000;
	
	@Override
    public String getCommandName()
    {
        return "jbiome";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jbiome r <biomeid> <square-radius> [origin-x] [origin-z] OR /jbiome b <biomeid> <min-x> <max-x> <min-z> <max-z>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		WorldServer world = (WorldServer)sender.getEntityWorld();
		
		if (args.length >= 2)
		{
			int biomeID = parseIntBounded(sender, args[1], 0, 255);
			
			RegionCriteria criteria = null;
			String mode = args[0];
			if (mode.equals("r"))
			{
				int radius = parseIntBounded(sender, args[2], 0, MAX_RANGE);
				ChunkCoordinates coords = sender.getPlayerCoordinates();
				int posX = coords.posX;
				int posZ = coords.posZ;
				
				if (args.length >= 4)
				{
					posX = parseInt(sender, args[3]);
					posZ = parseInt(sender, args[4]);
				}
				
				criteria = RegionCriteriaFactory.inRadius(posX, posZ, radius);
			}
			else if (mode.equals("b"))
			{
				int xMin = parseInt(sender, args[2]);
				int xMax = parseInt(sender, args[3]);
				int zMin = parseInt(sender, args[4]);
				int zMax = parseInt(sender, args[5]);
				
				criteria = RegionCriteriaFactory.inBox(xMin, xMax, zMin, zMax);
			}
			
			if (criteria != null)
			{
				int count = 0;
				
				for (int i = criteria.getMinX(); i <= criteria.getMaxX(); i++)
				{
					for (int k = criteria.getMinZ(); k <= criteria.getMaxZ(); k++)
					{
						Chunk chunk = world.getChunkFromBlockCoords(i, k);
						byte[] biomes = chunk.getBiomeArray();
						int chunkX = i & 15;
						int chunkZ = k & 15;
						int index = chunkZ << 4 | chunkX;
						
						biomes[index] = (byte)biomeID;
						chunk.setBiomeArray(biomes);
						chunk.setChunkModified();
						
						count++;
					}
				}
				
				func_152373_a(sender, this, String.format("Successfully set biome ID %d in %d block columns. You may need to relog", biomeID, count));
				return;
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
