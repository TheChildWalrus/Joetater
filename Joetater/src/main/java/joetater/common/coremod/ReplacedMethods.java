package joetater.common.coremod;

import java.net.SocketAddress;

import net.minecraft.server.management.*;

import com.mojang.authlib.GameProfile;

public class ReplacedMethods
{
	public static class SCM
	{
		public static String allowUserToConnect(String result, ServerConfigurationManager scm, SocketAddress address, GameProfile profile)
	    {
			if (result != null && result.equals("The server is full!"))
			{
				if (scm.func_152596_g(profile))
				{
					return null;
				}
			}
			return result;
	    }
	}
}
