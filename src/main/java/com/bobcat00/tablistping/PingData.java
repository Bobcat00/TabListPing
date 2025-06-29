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

// This class handles the data that needs to be stored in a map. It allows for
// a specified number of ping times to be averaged.

class PingData
{
    private long lastPacketTime; // Time of last S->C Keep Alive packet
    private int  pingTime[];
    private int  head;
    private int  count;
    private long lastUpdateTime; // Time of last ping time calculation update
    
    final int size = 3;

    // -------------------------------------------------------------------------

    // Constructor

    PingData()
    {
        this.lastPacketTime = 0;
        this.pingTime = new int[size];
        this.head = 0;
        this.count = 0;
        this.lastUpdateTime = 0;
    }
    
    // -------------------------------------------------------------------------
    
    // Set last packet time (Server to client time)
    
    void setPacketTime(long packetTime)
    {
        lastPacketTime = packetTime;
    }
    
    // -------------------------------------------------------------------------
    
    // Get last packet time (Server to client time)
    
    long getLastPacketTime()
    {
        return lastPacketTime;
    }
    
    // -------------------------------------------------------------------------
    
    // Add ping time and return new average
    
    int addPingTime(int ping)
    {
        pingTime[head] = ping;
        
        // Increment array index
        head++;
        if (head >= size)
        {
            head = 0;
        }
        
        // Increment number of values saved
        if (count < size)
        {
            count ++;
        }
        
        return getAveragePingTime();
    }
    
    // -------------------------------------------------------------------------
    
    // Calculate average ping time
    
    int getAveragePingTime()
    {
        if (count == 0)
        {
            return 0;
        }
        
        int sum = 0;
        for (int i = 0; i < count; i++)
        {
            sum += pingTime[i];
        }
        
        return sum / count;
    }
    
    // -------------------------------------------------------------------------
    
    // Set update time (ping time calculation)
    
    void setUpdateTime(long updateTime)
    {
        lastUpdateTime = updateTime;
    }
    
    // -------------------------------------------------------------------------
    
    // Get update time (ping time calculation)
    
    long getUpdateTime()
    {
        return lastUpdateTime;
    }
    
}
