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

import org.bukkit.Bukkit;
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

import io.papermc.lib.PaperLib;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// This class primarily handles updating each player's ping time in the tab list.

public final class Listeners implements Listener
{
    private TabListPing plugin;
    private IEssentials ess;
    
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    
    // Map containing Keep Alive time and ping time
    private Map<UUID, List<Long>> keepAliveTime = Collections.synchronizedMap(new HashMap<UUID, List<Long>>());
    
    // -------------------------------------------------------------------------
    
    // Constructor
    
    public Listeners(TabListPing plugin)
    {
        this.plugin = plugin;
        
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
        // Save time in hashmap
        UUID uuid = player.getUniqueId();
        Long currentTime = System.currentTimeMillis();
        List<Long> timeData = keepAliveTime.get(uuid); // possibly blocking
        if (timeData == null)
        {
            timeData = new ArrayList<Long>(2);
            timeData.add(0L);
            timeData.add(0L);
        }
        timeData.set(0, currentTime);
        keepAliveTime.put(uuid, timeData); // possibly blocking
    }
    
    // -------------------------------------------------------------------------
    
    // Keep Alive response from client to server
    
    public void processClientToServer(Player player)
    {
        // Get time from hashmap and calculate ping time in msec
        Long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        
        Long pingTime = 0L;
        List<Long> timeData = keepAliveTime.get(uuid); // possibly blocking
        if (timeData == null)
        {
            timeData = new ArrayList<Long>(2);
            timeData.add(0L);
            timeData.add(0L);
        }
        else
        {
            pingTime = currentTime - timeData.get(0);
            timeData.set(1, pingTime);
        }
        keepAliveTime.put(uuid, timeData); // possibly blocking
        final Long ping = pingTime;
        
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
                    setTabList(player, ping, afk);
                }
            }
        });
    }
    
    // -------------------------------------------------------------------------
    
    // Set tab list - Call from main thread only
    
    @SuppressWarnings("deprecation")
    private void setTabList(Player player, Long ping, boolean afk)
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
        
        if (PaperLib.isPaper())
        {
            // Convert displayName to MiniMessage
            String displayName = mm.serialize(player.displayName());
            
            // Replace variables in MiniMessage
            format = format.replace("%name%",        player.getName()).
                            replace("%displayname%", displayName).
                            replace("%ping%",        ping.toString());

            // Convert formatted MiniMessage to Component
            Component component = mm.deserialize(format);
            
            // Put in tab list
            player.playerListName(component);
        }
        else
        {
            // Convert displayName with section characters to MiniMessage
            TextComponent tc = legacy.deserialize(player.getDisplayName());
            String displayName = mm.serialize(tc);
            
            // Replace variables in MiniMessage
            format = format.replace("%name%",        player.getName()).
                            replace("%displayname%", displayName).
                            replace("%ping%",        ping.toString());

            // Convert formatted MiniMessage to legacy string with section characters
            Component component = mm.deserialize(format);
            String legacyStr = legacy.serialize(component);
            
            // Put in tab list
            player.setPlayerListName(legacyStr);
        }
    }
    
    // -------------------------------------------------------------------------
    
    // AFK change
    
    public void onAfk(AfkStatusChangeEvent event)
    {
        IUser user = event.getAffected();
        @SuppressWarnings("deprecation")
        Player player = user.getBase();
        if (player.isOnline())
        {
            UUID uuid = player.getUniqueId();
            List<Long> timeData = keepAliveTime.get(uuid); // possibly blocking
            Long ping = 0L;
            if (timeData != null)
            {
                ping = timeData.get(1);
            }
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
        keepAliveTime.remove(uuid); // possibly blocking
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
