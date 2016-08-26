package joetater.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "joetater", name = "Joetater: Region Saver", version = "1.0", acceptableRemoteVersions = "*")
public class Joetater
{
	@Mod.Instance("joetater")
	public static Joetater instance;
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandJoetate());
	}
}