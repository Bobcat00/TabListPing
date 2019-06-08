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
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.plugin.java.JavaPlugin;

public class TabListPing extends JavaPlugin
{
    Listeners listeners;
    
    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        FileConfigurationOptions configOptions = this.getConfig().options();
        configOptions.header("# Supported variables are %name%, %displayname%, and %ping%");
        this.saveConfig();

        listeners = new Listeners(this);
        
        // Metrics
        int pluginId = 10623;
        Metrics metrics = new Metrics(this, pluginId);
        
        String format = this.getConfig().getString("format");
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
        
        getLogger().info("You may opt-out of metrics by changing plugins/bStats/config.yml");
    }
        
    @Override
    public void onDisable()
    {
        //
    }
}
