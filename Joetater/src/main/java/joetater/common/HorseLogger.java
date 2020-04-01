package joetater.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.*;
import java.util.Calendar;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.io.Files;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class HorseLogger
{
	public static boolean enableLogger;
	private int tickCounter;
	
	private static final String logPath = "joetater/horse-log";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	public HorseLogger()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		if (enableLogger)
		{
			if (event.phase == TickEvent.Phase.END)
			{
				MinecraftServer server = MinecraftServer.getServer();
				
				tickCounter++;
				
				if (tickCounter % (20 * 10) == 0)
				{
					List playerList = server.getConfigurationManager().playerEntityList;
					int numPlayers = playerList.size();
					int numOnMounts = 0;
					for (Object p : playerList)
					{
						EntityPlayerMP player = (EntityPlayerMP)p;
						if (player.ridingEntity instanceof EntityLivingBase)
						{
							numOnMounts++;
						}
					}
					
					double tickTime = MathHelper.average(server.tickTimeArray) * 1.0E-6D;
					double tickTimePerPlayer = tickTime / numPlayers;
					
					try
					{
					    String messageLong = String.format("%s,%s,%s,%s,%s", new Object[]
					    {
					    	TIME_FORMAT.format(Calendar.getInstance().getTime()),
					    	numPlayers,
					    	numOnMounts,
					    	DECIMAL_FORMAT.format(tickTime),
					    	DECIMAL_FORMAT.format(tickTimePerPlayer),
					    }) + System.getProperty("line.separator");
						
						File logDir = new File(DimensionManager.getCurrentSaveRootDirectory(), logPath);
						if (!logDir.exists())
						{
							logDir.mkdirs();
						}
						final File logFileToday = new File(logDir, DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".csv");
						if (!logFileToday.exists())
						{
			            	Files.append("timestamp,num-players,num-on-mounts,tick-time,tick-time-per-player" + System.getProperty("line.separator"), logFileToday, CHARSET);
						}
						
			            Files.append(messageLong, logFileToday, CHARSET);
					}
					catch (IOException e)
					{
						Joetater.logger.warn("Joetater: Failed saving horse log");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
