package joetater.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import joetater.common.toswr.CommandKillLOTRTraders;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;

@Mod(modid = "joetater", name = "Joetater: Region Saver", version = "1.9", acceptableRemoteVersions = "*")
public class Joetater
{
	@Mod.Instance("joetater")
	public static Joetater instance;
	public static Logger logger;
	private static IngameChecker ingameChecker;
	private static JoetaterIPHandler ipHandler;
	private static LoginHandler loginHandler;
	private static MountEventHandler mountEventHandler;
	private static HorseLogger horseLogger;
	
	@Mod.EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		findLogger();
		JoetaterConfig.setupAndLoad(event);
		ingameChecker = new IngameChecker();
		ipHandler = new JoetaterIPHandler();
		loginHandler = new LoginHandler();
		mountEventHandler = new MountEventHandler();
		horseLogger = new HorseLogger();
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
	public void onServerAboutToStart(FMLServerAboutToStartEvent event)
	{
		LevelDatRestorer.attemptRestore();
	}
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandJoetate());
		event.registerServerCommand(new CommandSetBiome());
		event.registerServerCommand(new CommandSafeRestart());
		event.registerServerCommand(new CommandSetRenderDistance());
		event.registerServerCommand(new CommandViewEnder());
		event.registerServerCommand(new CommandResetPlayer());
		event.registerServerCommand(new CommandResetRegions());
		event.registerServerCommand(new CommandListRiders()); 
		event.registerServerCommand(new CommandKillLOTRTraders());
		//event.registerServerCommand(new CommandDelFile()); // For mass-deleting a folder of region files
		//event.registerServerCommand(new CommandCopyJoetated());
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
