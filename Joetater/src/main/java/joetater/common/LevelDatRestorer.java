package joetater.common;

import java.io.File;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;

import com.google.common.io.Files;

public class LevelDatRestorer
{
	public static boolean restoreLevelDat;
	
	public static void attemptRestore()
	{
		if (restoreLevelDat)
		{
			MinecraftServer server = MinecraftServer.getServer();
			if (server != null && server.isDedicatedServer())
			{
				File serverDir = ((AnvilSaveConverter)server.getActiveAnvilConverter()).savesDirectory;
				String worldName = server.getFolderName();
				File worldDir = new File(serverDir, worldName);
				
				File levelDat = new File(worldDir, "level.dat");
				File levelDat_old = new File(worldDir, "level.dat_old");
				
				if (levelDat_old.exists() && !levelDat.exists())
				{
					Joetater.logger.info("Joetater: Attempting to restore missing level.dat from level.dat_old");
					
					try
					{
						levelDat.createNewFile();
						Files.copy(levelDat_old, levelDat);
						
						Joetater.logger.info("Joetater: Successfully restored missing level.dat");
					}
					catch (IOException e)
					{
						Joetater.logger.info("Joetater: WARNING! Failed to restore missing level.dat!");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
