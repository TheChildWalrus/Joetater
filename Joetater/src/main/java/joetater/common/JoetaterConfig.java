package joetater.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class JoetaterConfig
{
	public static Configuration config;
	
	private static List<String> allCategories = new ArrayList();
	private static String CATEGORY_GENERAL = getCategory("general");
	
	private static String getCategory(String category)
	{
		allCategories.add(category);
		return category;
	}
	
	public static int maxJoetateSize;
	public static boolean commandKillTraders;
	public static boolean adminSlots;
	public static int mountLimit;
	public static int dismountGracePeriod;
	public static int mountLogoffGracePeriod;
	
	public static void setupAndLoad(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		load();
		IngameChecker.loadConfig(event);
		LoginHandler.loadConfig(event);
	}
	
	public static void load()
	{
		maxJoetateSize = config.getInt("Max Joetate Size", CATEGORY_GENERAL, 2000, 0, 20000, "Maximum size of a Joetater region. Note: For 'radius' mode, the maximum range will be half this");
		commandKillTraders = config.getBoolean("Trader delete command", CATEGORY_GENERAL, false, "Enable joetater-like command for deleting all non-structure-bound LOTR traders/captains within an area");
		adminSlots = config.getBoolean("Admin Slots", CATEGORY_GENERAL, true, "Allow admins to connect if the server is full");
		mountLimit = config.getInt("Mount Limit", CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "Set > 0 to limit how many players can ride mounts at once. 0 disables mounts entirely. Values < 0 have no effect");
		dismountGracePeriod = config.getInt("Mount Limit: Dismount Grace Period", CATEGORY_GENERAL, 10, 0, 60, "If mounts are limited, the grace period (seconds) for a player to dismount and keep their mount slot");
		mountLogoffGracePeriod = config.getInt("Mount Limit: Disconnect Grace Period", CATEGORY_GENERAL, 20, 0, 60, "If mounts are limited, the grace period (seconds) for a disconnected player to relog and keep their mount slot");
		
		IngameChecker.messageAdmins = config.getBoolean("Ingame check message admins", CATEGORY_GENERAL, true, "");
		
		IngameChecker.invCheckInterval = config.getInt("Inventory check interval", CATEGORY_GENERAL, 100, 1, Integer.MAX_VALUE, "Length of time (ticks) between checking player inventories");
		IngameChecker.invWarnInterval = config.getInt("Inventory check warn interval", CATEGORY_GENERAL, 1200, 1, Integer.MAX_VALUE, "Length of time (ticks) between successive inventory warnings for the same player");
		
		IngameChecker.entityThreshold = config.getInt("Entity check threshold", CATEGORY_GENERAL, 0, 0, Integer.MAX_VALUE, "Threshold for nearby entity count warning");
		IngameChecker.entityArea = config.getInt("Entity check area", CATEGORY_GENERAL, 200, 0, 2000, "Square radius for checking nearby entities");
		IngameChecker.entityCheckInterval = config.getInt("Entity check interval", CATEGORY_GENERAL, 100, 1, Integer.MAX_VALUE, "Length of time (ticks) between checking player nearby entities");
		IngameChecker.entityWarnInterval = config.getInt("Entity check warn interval", CATEGORY_GENERAL, 1200, 1, Integer.MAX_VALUE, "Length of time (ticks) between successive nearby entity warnings for the same player");
		
		IngameChecker.dupeCheckInterval = config.getInt("Dupe check interval", CATEGORY_GENERAL, 10, 1, Integer.MAX_VALUE, "Length of time (ticks) between checking players for dupes");
		IngameChecker.dupeWarnInterval = config.getInt("Dupe check warn interval", CATEGORY_GENERAL, 1200, 1, Integer.MAX_VALUE, "Length of time (ticks) between successive dupe warnings for the same player");
		
		JoetaterIPHandler.enableIPHandler = config.getBoolean("Enable IP handler", CATEGORY_GENERAL, true, "Enable IP logging and matching against IPs of banned players");
		JoetaterIPHandler.retainIPTimeDays = config.getInt("Days to retain IPs", CATEGORY_GENERAL, 200, 1, Integer.MAX_VALUE, "Number of days to retain logged IPs for purpose of IP matching. IPs logged longer ago than this amount of days will be deleted. Prevents the file getting too large over time");

		LevelDatRestorer.restoreLevelDat = config.getBoolean("Restore level.dat", CATEGORY_GENERAL, false, "If level.dat is missing, attempt to restore it from level.dat_old");
		
		HorseLogger.enableLogger = config.getBoolean("Horse logger", CATEGORY_GENERAL, false, "Track server performance vs. number of players riding mounts");
		
		if (config.hasChanged())
		{
			config.save();
		}
	}

	public static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList();
		for (String category : allCategories)
		{
			list.addAll(new ConfigElement(config.getCategory(category)).getChildElements());
		}
		return list;
	}
}
