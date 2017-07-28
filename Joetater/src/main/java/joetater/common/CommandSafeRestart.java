package joetater.common;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class CommandSafeRestart extends CommandBase
{
	public static boolean doingSafeRestart = false;
	public static String safeRestartMessage;
	
	public CommandSafeRestart()
	{
		super();
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
    public String getCommandName()
    {
        return "jSafeRestart";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jSafeRestart <message>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		String message = "All players have been kicked to enable a safe restart";
		if (args.length >= 1)
		{
			message = func_147178_a(sender, args, 0).getUnformattedText();
		}

		func_152373_a(sender, this, "Kicking all players to enable a safe restart");
		
		doingSafeRestart = true;
		safeRestartMessage = message;
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
    
	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP entityplayer = (EntityPlayerMP)event.player;
			
			if (doingSafeRestart)
			{
				MinecraftServer server = MinecraftServer.getServer();
				if (!server.getConfigurationManager().func_152596_g(entityplayer.getGameProfile()))
				{
					entityplayer.playerNetServerHandler.kickPlayerFromServer(safeRestartMessage);
				}
			}
		}
	}
}
