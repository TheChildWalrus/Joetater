package joetater.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;

@Mod(modid = "joetater", name = "Joetater: Region Saver", version = "1.4", acceptableRemoteVersions = "*")
public class Joetater
{
	@Mod.Instance("joetater")
	public static Joetater instance;
	public static Logger logger;
	private static IngameChecker ingameChecker;
	private static JoetaterIPHandler ipHandler;
	
	@Mod.EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		findLogger();
		JoetaterConfig.setupAndLoad(event);
		ingameChecker = new IngameChecker();
		ipHandler = new JoetaterIPHandler();
	}
	
	private static void findLogger()
	{
		try
		{
			Field[] fields = MinecraftServer.class.getDeclaredFields();
			for (Field f : fields)
			{
		        f.setAccessible(true);
		        Field modifiersField = Field.class.getDeclaredField("modifiers");
		        modifiersField.setAccessible(true);
		        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				Object obj = f.get(null);
				if (obj instanceof Logger)
				{
					logger = (Logger)obj;
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		logger.info("Joetater found logger");
	}
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandJoetate());
		event.registerServerCommand(new CommandSetBiome());
		ipHandler.onServerStarting(event);
	}
	
	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event)
	{
		ipHandler.onServerStopping(event);
	}
	
	public static void messageAllAdmins(MinecraftServer server, String message)
	{
		IChatComponent adminMessage = new ChatComponentText(message);
		adminMessage.getChatStyle().setColor(EnumChatFormatting.GRAY);
		adminMessage.getChatStyle().setItalic(Boolean.valueOf(true));
        
		for (Object admin : server.getConfigurationManager().playerEntityList)
		{
			EntityPlayer adminPlayer = (EntityPlayer)admin;
			if (server.getConfigurationManager().func_152596_g(adminPlayer.getGameProfile()))
			{
				adminPlayer.addChatComponentMessage(adminMessage);
			}
		}
	}
}
