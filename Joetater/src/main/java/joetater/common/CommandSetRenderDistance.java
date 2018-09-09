package joetater.common;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;

public class CommandSetRenderDistance extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "jViewRange";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jViewRange <range>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		if (args.length >= 1)
		{
			int range = parseIntBounded(sender, args[0], 3, 20);
			MinecraftServer.getServer().getConfigurationManager().func_152611_a(range);
			func_152373_a(sender, this, String.format("Updated view distance to %d", range));
			return;
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
