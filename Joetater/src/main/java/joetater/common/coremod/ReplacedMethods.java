package joetater.common.coremod;

import java.net.SocketAddress;

import joetater.common.CommandSafeRestart;
import joetater.common.JoetaterConfig;
import net.minecraft.server.management.*;

import com.mojang.authlib.GameProfile;

public class ReplacedMethods
{
	public static class SCM
	{
		public static String allowUserToConnect(String result, ServerConfigurationManager scm, SocketAddress address, GameProfile profile)
	    {
			if (result == null && CommandSafeRestart.doingSafeRestart && !scm.func_152596_g(profile))
			{
				return "The server is in Joetater safe restarting mode - you can rejoin after the restart!";
			}
			
			if (result != null && result.equals("The server is full!"))
			{
				if (JoetaterConfig.adminSlots && scm.func_152596_g(profile))
				{
					return null;
				}
			}
			
			return result;
	    }
	}
}
