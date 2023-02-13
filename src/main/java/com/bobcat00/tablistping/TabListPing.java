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

import java.util.Arrays;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.lib.PaperLib;

public class TabListPing extends JavaPlugin
{
    Listeners listeners;
    TpsTask tpsTask;
    
    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        // Do some updating
        getConfig().options().setHeader(null); // Remove old header
        getConfig().setComments("format", Arrays.asList("Tab list player name format",
                                                        "Use Minecraft color codes here",
                                                        "Supported variables are %name%, %displayname%, and %ping%"));
        // AFK string
        if (!getConfig().contains("format-afk", true))
        {
            getConfig().set("format-afk", getConfig().getString("format") + " &eAFK");
        }
        // TPS/MSPT/Load section
        if (!getConfig().contains("enable-tps", true))
        {
            getConfig().set("enable-tps",  false);
            getConfig().set("format-header", "");
            getConfig().set("format-footer", "<gray>TPS: %tps%   <gray>MSPT: %mspt%");
            getConfig().setComments("enable-tps", Arrays.asList(null, // Blank line
                                                                "Enable TPS/MSPT/Load/World display. Requires Paper.",
                                                                "This section uses MiniMessage tags such as <blue> and <newline>",
                                                                "Supported variables are %tps%, %mspt%, %load%, and %world%"));
        }
        // Enable metrics
        if (!getConfig().contains("enable-metrics", true))
        {
            getConfig().set("enable-metrics",  true);
            getConfig().setComments("enable-metrics", Arrays.asList(null, // Blank line
                                                                    "Enable metrics (subject to bStats global config)"));
        }
        // Finally save the config
        saveConfig();

        // Start TPS task if enabled in config and running Paper
        // Period is 5 seconds because that's the time period used for the average
        if (getConfig().getBoolean("enable-tps"))
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
                getLogger().warning("TPS/MSPT/Load display requires Paper.");
            }
        }
        
        // Start listeners after TPS task has started
        listeners = new Listeners(this);
        
        // Metrics
        if (getConfig().getBoolean("enable-metrics"))
        {
            int pluginId = 10623;
            Metrics metrics = new Metrics(this, pluginId);

            String format = getConfig().getString("format");
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
            metrics.addCustomChart(new SimplePie("enable_tps", () -> getConfig().getBoolean("enable-tps") ? "Yes" : "No"));
            getLogger().info("Metrics enabled if allowed by plugins/bStats/config.yml");
        }
    }
        
    @Override
    public void onDisable()
    {
        //
    }
}
