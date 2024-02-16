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

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.tinyprotocol.Reflection;
import com.comphenix.tinyprotocol.TinyProtocol;

import io.netty.channel.Channel;

public class TabListPing extends JavaPlugin implements Listener
{
    Config    config;
    Listeners listeners;
    TpsTask   tpsTask;
    Commands  commands;
    TinyProtocol protocol;
    
    private Class<?> OUT_KEEP_ALIVE_PACKET;
    private Class<?> IN_KEEP_ALIVE_PACKET;
    
    @Override
    public void onEnable()
    {
        // Config
        
        config = new Config(this);
        
        saveDefaultConfig();
        
        config.updateConfig();
        config.setComments();
        saveConfig();

        // Start TPS task if enabled in config
        // Period is 5 seconds because that's the time period used for the average
        
        if (config.getEnableTps())
        {
            // Start periodic task
            tpsTask = new TpsTask(this);
            tpsTask.runTaskTimer(this,    // plugin
                                 5L*20L,  // delay
                                 5L*20L); // period
        }
        
        // Start listeners after TPS task has started
        
        listeners = new Listeners(this);
        
        // Protocol hooks
        
        String bukkitVersion = getServer().getBukkitVersion().split("-")[0];
        String outClassName;
        String inClassName;
        
        // The idea here is to define the class names for old Minecraft versions,
        // handling current and future versions in the default case.
        // Use Spigot mappings.
        switch (bukkitVersion)
        {
        case "1.18":
        case "1.18.1":
        case "1.18.2":
        case "1.19":
        case "1.19.1":
        case "1.19.2":
        case "1.19.3":
        case "1.19.4":
        case "1.20":
        case "1.20.1":
            outClassName = "net.minecraft.network.protocol.game.PacketPlayOutKeepAlive";
            inClassName  = "net.minecraft.network.protocol.game.PacketPlayInKeepAlive";
            break;
        default:
            outClassName = "net.minecraft.network.protocol.common.ClientboundKeepAlivePacket";
            inClassName  = "net.minecraft.network.protocol.common.ServerboundKeepAlivePacket";
            break;
        }
        getLogger().info("Detected " + bukkitVersion + ", using " + outClassName + " & " + inClassName);
        OUT_KEEP_ALIVE_PACKET = Reflection.getClass(outClassName);
        IN_KEEP_ALIVE_PACKET  = Reflection.getClass(inClassName);
        
        this.protocol = new TinyProtocol(this)
        {
            // Server to client
            @Override
            public Object onPacketOutAsync(Player receiver, Channel channel, Object packet)
            {
                if (OUT_KEEP_ALIVE_PACKET.isInstance(packet))
                {
                    listeners.processServerToClient(receiver);
                }
                return super.onPacketOutAsync(receiver, channel, packet);
            }
        
            // Client to server
            @Override
            public Object onPacketInAsync(Player sender, Channel channel, Object packet)
            {
                if (IN_KEEP_ALIVE_PACKET.isInstance(packet))
                {
                    listeners.processClientToServer(sender);
                }
                return super.onPacketInAsync(sender, channel, packet);
            }
        };
        
        // Commands
        
        commands = new Commands(this);
        this.getCommand("tablistping").setExecutor(commands);
        this.getCommand("tablistping").setTabCompleter(commands);
        
        // Metrics
        
        if (config.getEnableMetrics())
        {
            int pluginId = 10623;
            Metrics metrics = new Metrics(this, pluginId);

            String format = config.getFormat();
            boolean name = format.contains("%name%");
            boolean displayname = format.contains("%displayname%");
            String option = "Neither";
            if (name && !displayname)
                option = "Name";
            else if (!name && displayname)
                option = "Displayname";
            else if (name && displayname)
                option = "Both";
            final String setting = option;
            metrics.addCustomChart(new SimplePie("format", () -> setting));
            metrics.addCustomChart(new SimplePie("enable_tps", () -> config.getEnableTps() ? "Yes" : "No"));
            getLogger().info("Metrics enabled if allowed by plugins/bStats/config.yml");
        }
    }
        
    @Override
    public void onDisable()
    {
        //
    }
}
