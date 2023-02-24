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
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.lib.PaperLib;

public class TabListPing extends JavaPlugin
{
    Config    config;
    Listeners listeners;
    TpsTask   tpsTask;
    Commands  commands;
    
    @Override
    public void onEnable()
    {
        // Config
        
        config = new Config(this);
        
        saveDefaultConfig();
        
        config.updateConfig();
        config.setComments();
        saveConfig();

        // Start TPS task if enabled in config and running Paper
        // Period is 5 seconds because that's the time period used for the average
        
        if (config.getEnableTps())
        {
            if (PaperLib.isPaper())
            {
                // Start periodic task
                tpsTask = new TpsTask(this);
                tpsTask.runTaskTimer(this,    // plugin
                                     5L*20L,  // delay
                                     5L*20L); // period
            }
            else
            {
                getLogger().warning("TPS/MSPT/Load/World display requires Paper.");
            }
        }
        
        // Start listeners after TPS task has started
        
        listeners = new Listeners(this);
        
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
