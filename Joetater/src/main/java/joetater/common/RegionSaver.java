package joetater.common;

import java.io.File;
import java.io.IOException;

import net.minecraft.util.MathHelper;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.Files;

public class RegionSaver
{
	public static int saveRegions(WorldServer world, RegionCriteria criteria) throws IOException, MinecraftException
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
	
	public static int toRegionCoords(int i)
	{
		return MathHelper.floor_double((double)i / 512D);
	}
	
	public static String getFilename(int x, int z)
	{
		return "r." + Integer.toString(x) + "." + Integer.toString(z) + ".mca";
	}
}
