package joetater.common.toswr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;

public class CommandCopyJoetated extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "jCopyJoetated";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jCopyJoetated";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		for (String dimPath : new String[] {"", "MiddleEarth/"})
		{
			File srcDir = getFile("world/joetater/" + dimPath + "region");
			File destDir = getFile("world/" + dimPath + "region");
			
			int copiedCount = 0;
			for (File regionFile : srcDir.listFiles())
			{
				if (!regionFile.isDirectory())
				{
					try
					{
						FileUtils.copyFileToDirectory(regionFile, destDir);
						copiedCount++;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						throw new CommandException(e.getMessage());
					}
				}
			}
			
			func_152373_a(sender, this, String.format("Copied across %d joetated files in %s", copiedCount, dimPath));
		}
		
		return;
    }
	
	private File getFile(String path)
	{
		return MinecraftServer.getServer().getFile(path);
	}
}
