package joetater.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListEntry;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

public class JoetaterIPList extends UserList
{
	public JoetaterIPList(File file)
	{
		super(file);
	}
	
	@Override
    protected UserListEntry func_152682_a(JsonObject key)
    {
        return new JoetaterIPListEntry(key);
    }
	
	public JoetaterIPListEntry getOrCreateIPListEntry(GameProfile profile)
	{
		JoetaterIPListEntry entry = (JoetaterIPListEntry)func_152683_b(profile);
		if (entry == null)
		{
			entry = new JoetaterIPListEntry(profile, new HashMap());
			func_152687_a(entry);
		}
		return entry;
	}
	
	@Override
    protected String func_152681_a(Object value)
    {
        return ((GameProfile)value).getId().toString();
    }
	
	public Map<String, UserListEntry> getIPListMap()
	{
		return func_152688_e();
	}
}
