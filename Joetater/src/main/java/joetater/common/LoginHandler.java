package joetater.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.base.Charsets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class LoginHandler
{
	private static List<String> tellraws = new ArrayList();
	
	public LoginHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void loadConfig(FMLPreInitializationEvent event)
	{
		File config = new File(event.getModConfigurationDirectory(), "joetater_login_tellraws.txt");
		if (config.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(config), Charsets.UTF_8.name()));
				
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("#"))
					{
						continue;
					}
					else
					{
						tellraws.add(line);
					}
				}
				
				reader.close();
			}
			catch (IOException e)
			{
				Joetater.logger.info("Joetater: Error loading login tellraws config file");
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				config.createNewFile();
			}
			catch (IOException e)
			{
				Joetater.logger.info("Joetater: Could not create login tellraws config file");
				e.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent event)
	{
		EntityPlayer player = event.player;
		String name = player.getCommandSenderName();

		MinecraftServer server = MinecraftServer.getServer();
		for (String tell : tellraws)
		{
			server.getCommandManager().executeCommand(server, "/tellraw " + name + " " + tell);
		}
	}
}
