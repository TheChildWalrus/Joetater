package joetater.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.UserListEntry;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLLog;

public class JoetaterIPListEntry extends UserListEntry
{
	private static SimpleDateFormat dateFormat = BanEntry.dateFormat;
	private GameProfile theGameProfile;
	private Map<String, Date> connectedIPs = new HashMap();
	
    public JoetaterIPListEntry(GameProfile profile, Map<String, Date> ips)
    {
        super(profile);
        theGameProfile = profile;
        connectedIPs.putAll(ips);
        purgeOldIPs();
    }

    public JoetaterIPListEntry(JsonObject obj)
    {
        this(getProfileFromJSON(obj), getIPsFromJSON(obj));
    }
    
    private void purgeOldIPs()
    {
    	Date dateNow = new Date();
    	int dayLimit = JoetaterIPHandler.retainIPTimeDays;
    	Set<String> removeIPs = new HashSet();
    	for (Entry<String, Date> entry : connectedIPs.entrySet())
    	{
    		String ip = entry.getKey();
    		Date date = entry.getValue();
    		long diff = dateNow.getTime() - date.getTime();
    		long sec = diff / 1000;
    		long min = sec / 60;
    		long hours = min / 60;
    		long days = hours / 24;
    		if (days > dayLimit)
    		{
    			removeIPs.add(ip);
    		}
    	}
    	if (!removeIPs.isEmpty())
    	{
	    	for (String ip : removeIPs)
	    	{
	    		connectedIPs.remove(ip);
	    	}
	    	Joetater.logger.info(String.format("Joetater: Removed %d old IPs for player %s which were logged %d days ago", removeIPs.size(), theGameProfile.getName(), dayLimit));
    	}
    }
    
    public GameProfile getGameProfile()
    {
    	return theGameProfile;
    }
    
    public void addConnectedIP(String ip, Date date)
    {
    	connectedIPs.put(ip, date);
    }
    
    public boolean containsMatchingIP(String ipToMatch)
    {
    	return connectedIPs.containsKey(ipToMatch);
    }

    @Override
    protected void func_152641_a(JsonObject jsonObj)
    {
    	jsonObj.addProperty("uuid", theGameProfile.getId() == null ? "" : theGameProfile.getId().toString());
    	jsonObj.addProperty("name", theGameProfile.getName());
        super.func_152641_a(jsonObj);
        if (!connectedIPs.isEmpty())
        {
        	JsonArray ipArray = new JsonArray();
        	for (Entry<String, Date> entry : connectedIPs.entrySet())
        	{
        		String ip = entry.getKey();
        		Date date = entry.getValue();
        		JsonObject ipDateObj = new JsonObject();
        		ipDateObj.addProperty("ip", ip);
        		ipDateObj.addProperty("date", dateFormat.format(date));
        		ipArray.add(ipDateObj);
        	}
        	jsonObj.add("ips", ipArray);
        }
    }

    private static GameProfile getProfileFromJSON(JsonObject jsonObj)
    {
        if (jsonObj.has("uuid") && jsonObj.has("name"))
        {
            String s = jsonObj.get("uuid").getAsString();
            UUID uuid;
            try
            {
                uuid = UUID.fromString(s);
            }
            catch (Throwable throwable)
            {
                return null;
            }
            return new GameProfile(uuid, jsonObj.get("name").getAsString());
        }
        else
        {
            return null;
        }
    }
    
    private static Map<String, Date> getIPsFromJSON(JsonObject jsonObj)
    {
        if (jsonObj.has("ips"))
        {
        	JsonArray ipArray = jsonObj.getAsJsonArray("ips");
        	Map<String, Date> ipMap = new HashMap();
        	for (JsonElement elem : ipArray)
        	{
        		if (elem.isJsonObject())
        		{
        			JsonObject elemObj = elem.getAsJsonObject();
	        		if (elemObj.has("ip") && elemObj.has("date"))
	        		{
	        			String ip = elemObj.get("ip").getAsString();
	        			Date date = null;
	        			try
	        			{
	        				date = dateFormat.parse(elemObj.get("date").getAsString());
	        			}
	        			catch (ParseException e)
	        			{
	        				FMLLog.warning("Joetater: Error loading IP date");
	        				e.printStackTrace();
	        			}
	        			if (date != null)
	        			{
	        				ipMap.put(ip, date);
	        			}
	        		}
        		}
        	}
        	return ipMap;
        }
        else
        {
        	return new HashMap();
        }
    }
}
    