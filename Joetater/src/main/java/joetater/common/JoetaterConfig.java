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
	
	public static void setupAndLoad(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		load();
		IngameChecker.loadConfig(event);
	}
	
	public static void load()
	{
		maxJoetateSize = config.getInt("Max Joetate Size", CATEGORY_GENERAL, 2000, 0, 20000, "Maximum size of a Joetater region. Note: For 'radius' mode, the maximum range will be half this");
		IngameChecker.checkInterval = config.getInt("Ingame check interval", CATEGORY_GENERAL, 100, 1, 2400, "Length of time (ticks) between checking player inventories");
		IngameChecker.warnInterval = config.getInt("Ingame check warn interval", CATEGORY_GENERAL, 1200, 1, 6000, "Length of time (ticks) between successive warnings for the same player");
		IngameChecker.messageAdmins = config.getBoolean("Ingame check message admins", CATEGORY_GENERAL, true, "");
		JoetaterIPHandler.enableIPHandler = config.getBoolean("Enable IP handler", CATEGORY_GENERAL, true, "Enable IP logging and matching against IPs of banned players");
		JoetaterIPHandler.retainIPTimeDays = config.getInt("Days to retain IPs", CATEGORY_GENERAL, 200, 1, Integer.MAX_VALUE, "Number of days to retain logged IPs for purpose of IP matching. IPs logged longer ago than this amount of days will be deleted. Prevents the file getting too large over time");

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
