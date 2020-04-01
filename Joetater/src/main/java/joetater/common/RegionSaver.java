package joetater.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.Files;

public class RegionSaver
{
	private static boolean initTraderClasses = false;
	private static Class clsTrader = null;
	private static Class clsCaptain = null;
	
	public static int saveRegions(WorldServer world, RegionCriteria criteria) throws IOException, MinecraftException, CommandException
	{
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
		File copyDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "joetater" + File.separator + dimFolder);
		if (!copyDir.exists())
		{
			copyDir.mkdirs();
		}
		int copied = 0;
		
		int x0 = criteria.getMinX();
		int x1 = criteria.getMaxX();
		int z0 = criteria.getMinZ();
		int z1 = criteria.getMaxZ();
		
		int rx0 = toRegionCoords(x0);
		int rx1 = toRegionCoords(x1);
		int rz0 = toRegionCoords(z0);
		int rz1 = toRegionCoords(z1);

		for (int i = rx0; i <= rx1; i++)
		{
			for (int k = rz0; k <= rz1; k++)
			{
				String filename = getFilename(i, k);
				File regionFile = new File(worldRegionDir, filename);
				if (regionFile.exists())
				{
					File copyRegionFile = new File(copyDir, filename);
					if (!copyRegionFile.exists())
					{
						copyRegionFile.createNewFile();
					}
					Files.copy(regionFile, copyRegionFile);
					copied++;
				}
			}
		}
		
		return copied;
	}
	
	public static int killTraders(WorldServer world, RegionCriteria criteria)
	{
		int x0 = criteria.getMinX();
		int x1 = criteria.getMaxX();
		int z0 = criteria.getMinZ();
		int z1 = criteria.getMaxZ();
		
		int rx0 = toRegionCoords(x0);
		int rx1 = toRegionCoords(x1);
		int rz0 = toRegionCoords(z0);
		int rz1 = toRegionCoords(z1);
	
		if (!initTraderClasses)
		{
			try
			{
				clsTrader = Class.forName("lotr.common.entity.npc.LOTRTradeable");
				clsCaptain = Class.forName("lotr.common.entity.npc.LOTRUnitTradeable");
				
				initTraderClasses = true;
			}
			catch (Exception e)
			{
				throw new CommandException("Could not locate LOTR trader classes in the code!");
			}
		}
		
		int tradersKilled = 0;
		
		for (int i = rx0; i <= rx1; i++)
		{
			for (int k = rz0; k <= rz1; k++)
			{
				int chX0 = i * 32;
				int chZ0 = k * 32;
				for (int chX = chX0; chX < chX0 + 32; chX++)
				{
					for (int chZ = chZ0; chZ < chZ0 + 32; chZ++)
					{
						Chunk chunk = world.getChunkFromChunkCoords(chX, chZ);
						
						AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(chX << 4, 0, chZ << 4, (chX << 4) + 16, world.getHeight(), (chZ << 4) + 16);
						aabb = aabb.expand(8D, 8D, 8D);
						List traders = world.selectEntitiesWithinAABB(EntityCreature.class, aabb, new IEntitySelector()
						{
							@Override
							public boolean isEntityApplicable(Entity entity)
							{
								EntityCreature living = (EntityCreature)entity;
								if (!living.hasHome())
								{
									Class entityClass = living.getClass();
									return living.isEntityAlive() && (clsTrader.isAssignableFrom(entityClass) || clsCaptain.isAssignableFrom(entityClass));
								}
								return false;
							}
						});
						
						for (Object obj : traders)
						{
							Entity entity = (Entity)obj;
							entity.setDead();
							tradersKilled++;
						}
						
						chunk.setChunkModified();
					}
				}
			}
		}

		return tradersKilled;
	}
	
	public static int toRegionCoords(int i)
	{
		return MathHelper.floor_double((double)i / 512D);
	}
	
	public static String getFilename(int x, int z)
	{
		return "r." + Integer.toString(x) + "." + Integer.toString(z) + ".mca";
	}
}
