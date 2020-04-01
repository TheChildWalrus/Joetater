package joetater.common.toswr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;

public class CommandDelFile extends CommandBase
{
	private File inProgressDeleteTarget = null;
	
	@Override
    public String getCommandName()
    {
        return "jDelFile";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jDelFile <name> then /jDelFile CONFIRM";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		if (args.length >= 1)
		{
			String arg = args[0];
			
			if (inProgressDeleteTarget != null)
			{
				if (arg.equals("CONFIRM"))
				{
					if (inProgressDeleteTarget.isDirectory())
					{
						try
						{
							FileUtils.deleteDirectory(inProgressDeleteTarget);
						}
						catch (IOException e)
						{
							e.printStackTrace();
							throw new CommandException(e.getMessage());
						}
						func_152373_a(sender, this, String.format("Deleted directory %s", inProgressDeleteTarget.getName()));
					}
					else
					{
						inProgressDeleteTarget.delete();
						func_152373_a(sender, this, String.format("Deleted file %s", inProgressDeleteTarget.getName()));
					}
					
					inProgressDeleteTarget = null;
					return;
				}
				else
				{
					inProgressDeleteTarget = null;
					throw new WrongUsageException("Joetater file deletion cancelled - not CONFIRMed correctly!");
				}
			}
			else
			{
				File file = getFile(arg);
				if (file.isDirectory())
				{
					func_152373_a(sender, this, String.format("Beware: File '%s' is a folder containing %d files, which will all be deleted!", file.getName(), file.listFiles().length));
					func_152373_a(sender, this, String.format("Really delete directory %s? Type /jDelFile CONFIRM to confirm", file.getName()));
				}
				else
				{
					func_152373_a(sender, this, String.format("Really delete file %s? Type /jDelFile CONFIRM to confirm", file.getName()));
				}
				
				inProgressDeleteTarget = file;
				return;
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }
	
	private File getFile(String path)
	{
		return MinecraftServer.getServer().getFile(path);
	}
}
