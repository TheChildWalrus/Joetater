package joetater.common;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class JoetaterUtil
{
	public static EntityPlayer findPlayerForUUID(MinecraftServer srv, UUID uuid)
	{
		List players = srv.getConfigurationManager().playerEntityList;
		for (Object obj : players)
		{
			EntityPlayer player = (EntityPlayer)obj;
			if (player.getUniqueID().equals(uuid))
			{
				return player;
			}
		}
		return null;
	}
}
