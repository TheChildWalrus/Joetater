package joetater.common;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class IngameChecker
{
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	public static boolean messageAdmins;
	
	private static Map<Pair<String, Integer>, Integer> checkItemIDs = new HashMap();
	private static final int checkMetaAny = -1;
	public static int invCheckInterval;
	public static int invWarnInterval;
	private static Map<UUID, Integer> playersInvWarnTimes = new HashMap();
	
	public static int entityThreshold;
	public static int entityArea;
	public static int entityCheckInterval;
	public static int entityWarnInterval;
	private static Map<UUID, Integer> playersEntityWarnTimes = new HashMap();
	
	private static Map<UUID, Integer> playersDupeWarnTimes = new HashMap();
	public static int dupeCheckInterval;
	public static int dupeWarnInterval;
	private static final String dupeLogPath = "joetater/dupes";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	
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
				UUID playerID = entityplayer.getUniqueID();
				World world = entityplayer.worldObj;
				InventoryPlayer inv = entityplayer.inventory;
				
				if (playersInvWarnTimes.containsKey(playerID) && playersInvWarnTimes.get(playerID) > 0)
				{
					int time = playersInvWarnTimes.get(playerID);
					time--;
					playersInvWarnTimes.put(playerID, time);
				}
				
				if (server.getTickCounter() % invCheckInterval == 0)
				{
					if (!playersInvWarnTimes.containsKey(playerID) || playersInvWarnTimes.get(playerID) <= 0)
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
							playersInvWarnTimes.put(playerID, invWarnInterval);
						}
					}
				}
				
				if (playersDupeWarnTimes.containsKey(playerID) && playersDupeWarnTimes.get(playerID) > 0)
				{
					int time = playersDupeWarnTimes.get(playerID);
					time--;
					playersDupeWarnTimes.put(playerID, time);
				}
				
				if (server.getTickCounter() % dupeCheckInterval == 0)
				{
					if (!playersDupeWarnTimes.containsKey(playerID) || playersDupeWarnTimes.get(playerID) <= 0)
					{
						boolean duping = false;
						String dupeItemName = "item???";
						dupeLoop:
						for (int slot = 0; slot < inv.getSizeInventory(); slot++)
						{
							ItemStack itemstack = inv.getStackInSlot(slot);
							if (itemstack != null && itemstack.stackSize <= 0)
							{
								duping = true;
								dupeItemName = itemstack.getUnlocalizedName();
								break dupeLoop;
							}
						}
								
						if (duping)
						{
							String message = String.format("Joetater: WARNING! Player %s is possibly duplicating items (%s)", entityplayer.getCommandSenderName(), dupeItemName);
							Joetater.logger.info(message);
							
							if (messageAdmins)
							{
								Joetater.messageAllAdmins(server, message);
							}
							
							playersDupeWarnTimes.put(playerID, dupeWarnInterval);
							
							try
							{
							    String messageLong = String.format("%s,%s,%s,%s,%s,%s,%s,%s", new Object[]
							    {
							    	TIME_FORMAT.format(Calendar.getInstance().getTime()),
							    	entityplayer.getCommandSenderName(),
							    	entityplayer.getPersistentID(),
							    	entityplayer.dimension,
							    	MathHelper.floor_double(entityplayer.posX),
							    	MathHelper.floor_double(entityplayer.boundingBox.minY),
							    	MathHelper.floor_double(entityplayer.posZ),
							    	dupeItemName
							    }) + System.getProperty("line.separator");
								
								File dupeLogDir = new File(DimensionManager.getCurrentSaveRootDirectory(), dupeLogPath);
								if (!dupeLogDir.exists())
								{
									dupeLogDir.mkdirs();
								}
								final File logFileToday = new File(dupeLogDir, DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".csv");
								if (!logFileToday.exists())
								{
					            	Files.append("timestamp,username,UUID,dim,x,y,z,itemname" + System.getProperty("line.separator"), logFileToday, CHARSET);
								}
								
					            Files.append(messageLong, logFileToday, CHARSET);
							}
							catch (IOException e)
							{
								Joetater.logger.warn("Joetater: Failed saving dupe log");
								e.printStackTrace();
							}
						}
					}
				}
				
				if (entityThreshold > 0)
				{
					if (playersEntityWarnTimes.containsKey(playerID) && playersEntityWarnTimes.get(playerID) > 0)
					{
						int time = playersEntityWarnTimes.get(playerID);
						time--;
						playersEntityWarnTimes.put(playerID, time);
					}
					
					if (server.getTickCounter() % entityCheckInterval == 0)
					{
						if (!playersEntityWarnTimes.containsKey(playerID) || playersEntityWarnTimes.get(playerID) <= 0)
						{
							boolean sentWarning = false;
							
							AxisAlignedBB checkBox = entityplayer.boundingBox.expand(entityArea, entityArea, entityArea);
							List nearbyEntities = world.getEntitiesWithinAABBExcludingEntity(entityplayer, checkBox);
							List nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, checkBox);
							int eCount = nearbyEntities.size();
							int players = nearbyPlayers.size();
							if (players > 1)
							{
								eCount = Math.round((float)eCount / (float)players);
							}
							
							if (eCount >= entityThreshold)
							{
								String message = String.format("Joetater: WARNING! Player %s (DIM%d) has %d entities per player in range %d", entityplayer.getCommandSenderName(), entityplayer.dimension, eCount, entityArea);
								Joetater.logger.info(message);
								
								if (messageAdmins)
								{
									Joetater.messageAllAdmins(server, message);
								}
								
								sentWarning = true;
							}
							
							if (sentWarning)
							{
								playersEntityWarnTimes.put(playerID, entityWarnInterval);
							}
						}
					}
				}
			}
		}
	}
}
