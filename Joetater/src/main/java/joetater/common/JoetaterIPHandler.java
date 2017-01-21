package joetater.common;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListEntry;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class JoetaterIPHandler
{
	private static File fileIPList = new File("joetater-ips.json");
	private JoetaterIPList ipList;
	
	public static boolean enableIPHandler;
	public static int retainIPTimeDays;
	
	public JoetaterIPHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onServerStarting(FMLServerStartingEvent event)
	{
		if (event.getServer().isDedicatedServer() && enableIPHandler)
		{
			ipList = new JoetaterIPList(fileIPList);
			loadIPList();
			saveIPList();
		}
	}
	
	public void onServerStopping(FMLServerStoppingEvent event)
	{
		if (isDedicatedServer() && enableIPHandler)
		{
			saveIPList();
		}
	}
	
	private boolean isDedicatedServer()
	{
		return MinecraftServer.getServer().isDedicatedServer();
	}
	
	@SubscribeEvent
	public void onPlayerJoined(PlayerLoggedInEvent event)
	{
		if (isDedicatedServer() && enableIPHandler)
		{
			MinecraftServer server = MinecraftServer.getServer();
			
			EntityPlayerMP entityplayer = (EntityPlayerMP)event.player;
			GameProfile profile = entityplayer.getGameProfile();
			
			String ip = entityplayer.playerNetServerHandler.netManager.getSocketAddress().toString();
			ip = ip.trim();
			if (ip.startsWith("/"))
			{
				ip = ip.substring(1);
			}
			int portIndex = ip.indexOf(":");
			if (portIndex >= 0)
			{
				ip = ip.substring(0, portIndex);
			}
			
			Date dateNow = new Date();
			ipList.getOrCreateIPListEntry(profile).addConnectedIP(ip, dateNow);
			saveIPList();
			
			for (Entry<String, UserListEntry> entry : ipList.getIPListMap().entrySet())
			{
				JoetaterIPListEntry ipListEntry = (JoetaterIPListEntry)entry.getValue();
				if (ipListEntry.containsMatchingIP(ip))
				{
					GameProfile ipMatchProfile = ipListEntry.getGameProfile();
					if (server.getConfigurationManager().func_152608_h().func_152702_a(ipMatchProfile))
					{
						String message = String.format("Joetater: WARNING! Player %s connected with IP matching previously banned player %s", profile.getName(), ipMatchProfile.getName());
						Joetater.logger.info(message);
						Joetater.messageAllAdmins(server, message);
					}
				}
			}
		}
	}

	private void loadIPList()
	{
		try
		{
			ipList.func_152679_g();
		}
		catch (IOException e)
		{
			Joetater.logger.warn("Joetater: Failed to load IP list", e);
		}
	}
		
	private void saveIPList()
	{
		try
		{
			ipList.func_152678_f();
		}
		catch (IOException e)
		{
			Joetater.logger.warn("Joetater: Failed to save IP list", e);
		}
	}
}
