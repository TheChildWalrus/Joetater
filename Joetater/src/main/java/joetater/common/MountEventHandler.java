package joetater.common;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.*;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;

public class MountEventHandler
{
	public MountEventHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private List<UUID> mountRiders = new ArrayList();
	private Map<UUID, Integer> dismountGracePeriods = new HashMap();
	private Map<UUID, Integer> disconnectGracePeriods = new HashMap();
	private List<UUID> disconnectedTooLong = new ArrayList();
	private int tick;

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (JoetaterConfig.mountLimit >= 0)
		{
			if (event.side == Side.SERVER && event.phase == Phase.END)
			{
				MinecraftServer srv = MinecraftServer.getServer();
				EntityPlayer player = event.player;
				UUID uuid = player.getUniqueID();
				
				if (isRidingMount(player))
				{
					boolean dismounted = false;
					
					if (!mountRiders.contains(uuid))
					{
						int riders = countRiders(srv);
						if (riders > JoetaterConfig.mountLimit)
						{
							player.mountEntity(null);
							dismounted = true;
						}
						else
						{
							mountRiders.add(uuid);
						}
					}
					
					if (dismountGracePeriods.containsKey(uuid))
					{
						dismountGracePeriods.remove(uuid);
					}
					
					if (disconnectedTooLong.contains(uuid))
					{
						disconnectedTooLong.remove(uuid);
						IChatComponent msg1 = new ChatComponentText("You rejoined after your " + JoetaterConfig.mountLogoffGracePeriod + "-second disconnection grace period had run out!");
						msg1.getChatStyle().setColor(EnumChatFormatting.RED);
						player.addChatMessage(msg1);
						
						if (dismounted)
						{
							IChatComponent msg2 = new ChatComponentText("Unfortunately there are now too many players riding mounts. Try again in a while.");
							msg2.getChatStyle().setColor(EnumChatFormatting.RED);
							player.addChatMessage(msg2);
						}
						else
						{
							IChatComponent msg2 = new ChatComponentText("Luckily there aren't many players on mounts right now, so you can keep riding.");
							msg2.getChatStyle().setColor(EnumChatFormatting.RED);
							player.addChatMessage(msg2);
						}
					}
					else if (dismounted)
					{
						IChatComponent msg = new ChatComponentText("There are too many players already riding mounts! Try again in a while.");
						msg.getChatStyle().setColor(EnumChatFormatting.RED);
						player.addChatMessage(msg);
					}
				}
				else
				{
					if (mountRiders.contains(uuid) && !dismountGracePeriods.containsKey(uuid))
					{
						int gracePeriod = JoetaterConfig.dismountGracePeriod;
						dismountGracePeriods.put(uuid, gracePeriod);
						IChatComponent msg = new ChatComponentText("Mount slots are limited! You have a " + gracePeriod + "-second grace period to remount and keep your slot.");
						msg.getChatStyle().setColor(EnumChatFormatting.RED);
						player.addChatMessage(msg);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (JoetaterConfig.mountLimit >= 0)
		{
			if (event.side == Side.SERVER && event.phase == Phase.END)
			{
				tick++;
				if (tick % 20 == 0)
				{
					MinecraftServer srv = MinecraftServer.getServer();
					
					if (!dismountGracePeriods.isEmpty())
					{
						Set<UUID> removes = new HashSet();
						
						for (Entry<UUID, Integer> e : dismountGracePeriods.entrySet())
						{
							UUID uuid = e.getKey();
							int s = e.getValue();
							s--;
							e.setValue(s);
							
							if (s <= 0)
							{
								removes.add(uuid);
							}
						}
						
						for (UUID uuid : removes)
						{
							dismountGracePeriods.remove(uuid);
							mountRiders.remove(uuid);
							
							EntityPlayer matchingPlayer = JoetaterUtil.findPlayerForUUID(srv, uuid);
							if (matchingPlayer != null)
							{
								int gracePeriod = JoetaterConfig.dismountGracePeriod;
								IChatComponent msg = new ChatComponentText("Your " + gracePeriod + "-second dismount grace period has expired.");
								msg.getChatStyle().setColor(EnumChatFormatting.RED);
								matchingPlayer.addChatMessage(msg);
							}
						}
					}
					
					if (!disconnectGracePeriods.isEmpty())
					{
						Set<UUID> removes = new HashSet();
						
						for (Entry<UUID, Integer> e : disconnectGracePeriods.entrySet())
						{
							UUID uuid = e.getKey();
							int s = e.getValue();
							s--;
							e.setValue(s);
							
							if (s <= 0)
							{
								removes.add(uuid);
							}
						}
						
						for (UUID uuid : removes)
						{
							disconnectGracePeriods.remove(uuid);
							mountRiders.remove(uuid);
							disconnectedTooLong.add(uuid);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLogout(PlayerLoggedOutEvent event)
	{
		EntityPlayer player = event.player;
		UUID uuid = player.getUniqueID();
		
		if (mountRiders.contains(uuid))
		{
			disconnectGracePeriods.put(uuid, JoetaterConfig.mountLogoffGracePeriod);
		}
	}
	
	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent event)
	{
		EntityPlayer player = event.player;
		UUID uuid = player.getUniqueID();
		
		if (disconnectGracePeriods.containsKey(uuid))
		{
			disconnectGracePeriods.remove(uuid);
			IChatComponent msg = new ChatComponentText("You rejoined in time to keep your mount slot.");
			msg.getChatStyle().setColor(EnumChatFormatting.RED);
			player.addChatMessage(msg);
		}
	}
	
	public static boolean isRidingMount(EntityPlayer player)
	{
		Entity mount = player.ridingEntity;
		return mount != null && !(mount instanceof EntityMinecart); // don't include minecarts for now
	}
	
	public static List<EntityPlayer> getRiderList(MinecraftServer server)
	{
		ServerConfigurationManager scm = server.getConfigurationManager();
		List allPlayers = scm.playerEntityList;
		
		List<EntityPlayer> ridingPlayers = new ArrayList();
		for (Object obj : allPlayers)
		{
			EntityPlayer player = (EntityPlayer)obj;
			if (isRidingMount(player))
			{
				ridingPlayers.add(player);
			}
		}
		return ridingPlayers;
	}
	
	public static int countRiders(MinecraftServer server)
	{
		return getRiderList(server).size();
	}
}
