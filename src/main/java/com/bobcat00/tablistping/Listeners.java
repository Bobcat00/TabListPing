// TabListPing - Displays ping time in CraftBukkit/Spigot player list
// Copyright 2019 Bobcat00
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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;

import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;

// This class primarily handles updating each player's ping time in the tab list.

@SuppressWarnings("deprecation")
public final class Listeners implements Listener
{
    private TabListPing plugin;
    private DataStore dataStore;
    private IEssentials ess;
    
    // -------------------------------------------------------------------------
    
    // Constructor
    
    public Listeners(TabListPing plugin)
    {
        this.plugin = plugin;
        
        this.dataStore = new DataStore(plugin);
        
        // Hook in to Essentials
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials != null && essentials.isEnabled())
        {
            ess = (IEssentials)essentials;
        }
        
        // Register events
        
        plugin.getServer().getPluginManager().registerEvent(PlayerQuitEvent.class, this, EventPriority.MONITOR,
                new EventExecutor() { public void execute(Listener l, Event e) { onPlayerQuit((PlayerQuitEvent)e); }},
                plugin, true); // ignoreCancelled=true
        
        plugin.getServer().getPluginManager().registerEvent(PlayerChangedWorldEvent.class, this, EventPriority.MONITOR,
                new EventExecutor() { public void execute(Listener l, Event e) { onChangedWorld((PlayerChangedWorldEvent)e); }},
                plugin, true); // ignoreCancelled=true
        
        if (ess != null)
        {
            plugin.getServer().getPluginManager().registerEvent(AfkStatusChangeEvent.class, this, EventPriority.MONITOR,
                    new EventExecutor() { public void execute(Listener l, Event e) { onAfk((AfkStatusChangeEvent)e); }},
                    plugin, true); // ignoreCancelled=true);
        }
    }
    
    // -------------------------------------------------------------------------
    
    // Keep Alive from server to client
    
    public void processServerToClient(Player player)
    {
        Long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        dataStore.saveTime(uuid, currentTime);
    }
    
    // -------------------------------------------------------------------------
    
    // Keep Alive response from client to server
    
    public void processClientToServer(Player player)
    {
        // Get time from hashmap and calculate ping time in msec
        Long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        final int pingTime = dataStore.calculatePing(uuid, currentTime);
        
        // go to the main thread
        Bukkit.getScheduler().runTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                if (player.isOnline())
                {
                    // Get AFK status
                    boolean afk = false;
                    if (ess != null)
                    {
                        User user = ess.getUser(player);
                        if (user != null)
                        {
                            afk = user.isAfk();
                        }
                    }
                    setTabList(player, pingTime, afk);
                }
            }
        });
    }
    
    // -------------------------------------------------------------------------
    
    // Set tab list - Call from main thread only
    
    private void setTabList(Player player, int ping, boolean afk)
    {
        String format;
        if (afk)
        {
            format = plugin.config.getFormatAfk();
        }
        else
        {
            format = plugin.config.getFormat();
        }
        
        // Put ping time in tab list
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',
                   format.replace("%name%",        player.getName()).
                          replace("%displayname%", player.getDisplayName()).
                          replace("%ping%",        String.valueOf(ping))));
    }
    
    // -------------------------------------------------------------------------
    
    // AFK change
    
    public void onAfk(AfkStatusChangeEvent event)
    {
        IUser user = event.getAffected();
        Player player = user.getBase();
        if (player.isOnline())
        {
            UUID uuid = player.getUniqueId();
            int ping = dataStore.getPing(uuid);
            setTabList(player, ping, event.getValue());
        }
    }
    
    // -------------------------------------------------------------------------
    
    // Player quit or was kicked
    
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        // Remove player's entry from hashmap
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        dataStore.removeUuid(uuid);
    }
    
    // -------------------------------------------------------------------------
    
    // Player changed world - Used for header/footer
    
    public void onChangedWorld(PlayerChangedWorldEvent event)
    {
        if (plugin.config.getEnableTps() && (plugin.tpsTask != null))
        {
            Player player = event.getPlayer();
            plugin.tpsTask.setHeaderFooter(player);
        }
    }
    
}    
