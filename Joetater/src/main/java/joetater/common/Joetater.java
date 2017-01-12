package joetater.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "joetater", name = "Joetater: Region Saver", version = "1.3", acceptableRemoteVersions = "*")
public class Joetater
{
	@Mod.Instance("joetater")
	public static Joetater instance;
	
	private static IngameChecker ingameChecker;
	
	@Mod.EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		JoetaterConfig.setupAndLoad(event);
		ingameChecker = new IngameChecker();
	}
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandJoetate());
		event.registerServerCommand(new CommandSetBiome());
	}
}
