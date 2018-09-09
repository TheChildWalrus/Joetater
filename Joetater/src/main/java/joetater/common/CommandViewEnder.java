package joetater.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.storage.ISaveHandler;

import com.mojang.authlib.GameProfile;

public class CommandViewEnder extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "viewender";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/viewender <player>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		EntityPlayerMP user = getCommandSenderAsPlayer(sender);
		if (user != null)
		{
			if (args.length >= 1)
			{
				String username = args[0];
	            
				MinecraftServer server = MinecraftServer.getServer();
				ServerConfigurationManager scm = server.getConfigurationManager();
				EntityPlayerMP entityplayer = scm.func_152612_a(username);
				if (entityplayer != null)
				{
					InventoryEnderChest echest = entityplayer.getInventoryEnderChest();
					user.displayGUIChest(echest);
					return;
				}
				else
				{
		            GameProfile profile = server.func_152358_ax().func_152655_a(username);
		            if (profile == null)
		            {
		                throw new CommandException("Could not locate player data for username %s", username);
		            }
		            else
		            {
		            	ISaveHandler playerNBTManager = server.worldServerForDimension(0).getSaveHandler();
		                NBTTagCompound nbt = null;

		                try
		                {
		                	File worldDir = playerNBTManager.getWorldDirectory();
		                	File playerDir = new File(worldDir, "playerdata");
		                    File playerDat = new File(playerDir, profile.getId().toString() + ".dat");
		                    if (playerDat.exists() && playerDat.isFile())
		                    {
		                    	nbt = CompressedStreamTools.readCompressed(new FileInputStream(playerDat));
		                    }
		                }
		                catch (Exception exception)
		                {
		                	throw new CommandException("Failed to load player data for offline player %s", username);
		                }

		                if (nbt != null)
		                {
		                    NBTTagList enderTags = nbt.getTagList("EnderItems", 10);
		                    InventoryEnderChest echest = new InventoryEnderChest();
		                    echest.loadInventoryFromNBT(enderTags);
		                    echest.func_110133_a("read-only offline ender chest");
		                    user.displayGUIChest(echest);
		                    return;
		                }
		            }
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
    	if (args.length >= 1)
    	{
    		return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    	}
    	return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return i == 0;
    }
}
