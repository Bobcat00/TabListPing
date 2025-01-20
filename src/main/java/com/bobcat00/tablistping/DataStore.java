// TabListPing - Displays ping time in CraftBukkit/Spigot player list
// Copyright 2025 Bobcat00
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.bobcat00.tablistping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataStore
{
    @SuppressWarnings("unused")
    private TabListPing plugin;
    
    // Map containing ping data for each UUID
    private Map<UUID, PingData> pingMap = Collections.synchronizedMap(new HashMap<UUID, PingData>());
    
    // -------------------------------------------------------------------------
    
    // Constructor
    
    public DataStore(TabListPing plugin)
    {
        this.plugin = plugin;
    }
    
    // -------------------------------------------------------------------------
    
    // Call upon KeepAlive from server to client
    
    public void saveTime(UUID uuid, long currentTime)
    {
        PingData pingData = pingMap.get(uuid);
        if (pingData == null)
        {
            // Create new entry
            pingData = new PingData();
        }
        pingData.addPacketTime(currentTime);
        pingMap.put(uuid, pingData);
    }
    
    // -------------------------------------------------------------------------
    
    // Call upon KeepAlive from client to server
    
    public int calculatePing(UUID uuid, long currentTime)
    {
        int pingTime = 0;
        PingData pingData = pingMap.get(uuid);
        if (pingData == null)
        {
            // Nothing to do since this is the client's response
            return 0;
        }
        
        pingTime = (int)(currentTime - pingData.getLastPacketTime());
        pingTime = pingData.addPingTime(pingTime);
        
        return pingTime;
    }
    
    // -------------------------------------------------------------------------
    
    // Return ping time
    
    public int getPing(UUID uuid)
    {
        PingData pingData = pingMap.get(uuid);
        if (pingData == null)
        {
            return 0;
        }
        return pingData.getAveragePingTime();
    }
    
    // -------------------------------------------------------------------------
    
    // Remove data for UUID
    
    public void removeUuid(UUID uuid)
    {
        pingMap.remove(uuid);
    }
    
}
