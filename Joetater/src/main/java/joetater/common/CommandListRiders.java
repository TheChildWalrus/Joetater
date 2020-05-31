package joetater.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;

public class CommandListRiders extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "listRiders";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/listRiders";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		MinecraftServer server = MinecraftServer.getServer();
		List<EntityPlayer> ridingPlayers = MountEventHandler.getRiderList(server);

        String riderListMsg = "";
        for (int l = 0; l < ridingPlayers.size(); l++)
        {
            if (l > 0)
            {
            	riderListMsg = riderListMsg + ", ";
            }
            
            EntityPlayer player = ridingPlayers.get(l);
            String thisPlayer = player.getCommandSenderName() + " (" + EntityList.getEntityString(player.ridingEntity) + ")";
            riderListMsg = riderListMsg + thisPlayer;
        }
		
		sender.addChatMessage(new ChatComponentText(String.format("There are %d/%d online players riding mounts:", ridingPlayers.size(), server.getCurrentPlayerCount())));
		sender.addChatMessage(new ChatComponentText(riderListMsg));
    }
}
