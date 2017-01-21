package joetater.common;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Charsets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class IngameChecker
{
	private static Map<Pair<String, Integer>, Integer> checkItemIDs = new HashMap();
	private static final int checkMetaAny = -1;
	
	public static int checkInterval;
	public static int warnInterval;
	public static boolean messageAdmins;
	private static Map<UUID, Integer> playersTimeSinceWarned = new HashMap();
	
	public IngameChecker()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void loadConfig(FMLPreInitializationEvent event)
	{
		File config = new File(event.getModConfigurationDirectory(), "joetater_ingame_checker.txt");
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
						String s = line;
						s = s.trim();
						
						boolean error = false;
						
						int comma1 = s.indexOf(",");
						if (comma1 >= 0)
						{
							int comma2 = s.indexOf(",", comma1 + 1);
							if (comma2 >= 0)
							{
								String sItemID = s.substring(0, comma1).trim();
								String sItemMeta = s.substring(comma1 + 1, comma2).trim();
								String sThreshold = s.substring(comma2 + 1).trim();
								
								int itemMeta = Integer.valueOf(sItemMeta);
								int threshold = Integer.valueOf(sThreshold);
								
								checkItemIDs.put(Pair.of(sItemID, itemMeta), threshold);
								Joetater.logger.info(String.format("Joetater: Ingame checks added check for %s.%d >= %d", sItemID, itemMeta, threshold));
							}
							else
							{
								error = true;
							}
						}
						else
						{
							error = true;
						}
						
						if (error)
						{
							Joetater.logger.info(String.format("Joetater: ERROR in ingame checks config, line \"%s\"", line));
						}
					}
				}
				
				reader.close();
			}
			catch (IOException e)
			{
				Joetater.logger.info("Joetater: Error loading ingame checks config file");
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				config.createNewFile();
				
				PrintStream writer = new PrintStream(new FileOutputStream(config));
				
				writer.println("# JOETATER: Ingame Checks Configuration");
				writer.println("#");
				writer.println("# This is a config file. Lines starting with # will be ignored.");
				writer.println("#");
				writer.println("# Enter below your desired items to be checked for ingame.");
				writer.println("# You can specify the item ID, metadata values (-1 for all values) and warning threshold, separated by commas.");
				writer.println("# The warning will only activate if a player has as many items as the threshold.");
				writer.println("#");
				writer.println("# Example: To check for at least 20 planks of any metadata.");
				writer.println("# minecraft:planks, -1, 20");
				writer.println("#");
				
				writer.close();
			}
			catch (IOException e)
			{
				Joetater.logger.info("Joetater: Could not create ingame checks config file");
				e.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			MinecraftServer server = MinecraftServer.getServer();
			
			for (Object player : server.getConfigurationManager().playerEntityList)
			{
				EntityPlayer entityplayer = (EntityPlayer)player;
				InventoryPlayer inv = entityplayer.inventory;
				UUID playerID = entityplayer.getUniqueID();
				
				if (playersTimeSinceWarned.containsKey(playerID) && playersTimeSinceWarned.get(playerID) > 0)
				{
					int time = playersTimeSinceWarned.get(playerID);
					time--;
					playersTimeSinceWarned.put(playerID, time);
				}
				
				if (server.getTickCounter() % checkInterval == 0)
				{
					if (!playersTimeSinceWarned.containsKey(playerID) || playersTimeSinceWarned.get(playerID) <= 0)
					{
						boolean sentWarning = false;
						for (Entry<Pair<String, Integer>, Integer> entry : checkItemIDs.entrySet())
						{
							Pair<String, Integer> itemKey = entry.getKey();
							String checkItemID = itemKey.getKey();
							int checkItemMeta = itemKey.getValue();
							int threshold = entry.getValue();
							
							if (threshold > 0)
							{
								int count = 0;
								for (int slot = 0; slot < inv.getSizeInventory(); slot++)
								{
									ItemStack itemstack = inv.getStackInSlot(slot);
									if (itemstack != null)
									{
										Item item = itemstack.getItem();
										String itemID = Item.itemRegistry.getNameForObject(item);
										int meta = itemstack.getItemDamage();
										if (itemID.equalsIgnoreCase(checkItemID) && (checkItemMeta == checkMetaAny || checkItemMeta == meta))
										{
											count += itemstack.stackSize;
										}
									}
								}
								
								if (count >= threshold)
								{
									String message = String.format("Joetater: WARNING! Player %s has %d of item %s:%d", entityplayer.getCommandSenderName(), count, checkItemID, checkItemMeta);
									Joetater.logger.info(message);
									
									if (messageAdmins)
									{
										Joetater.messageAllAdmins(server, message);
									}
									
									sentWarning = true;
								}
							}
						}
						
						if (sentWarning)
						{
							playersTimeSinceWarned.put(playerID, warnInterval);
						}
					}
				}
			}
		}
	}
}
