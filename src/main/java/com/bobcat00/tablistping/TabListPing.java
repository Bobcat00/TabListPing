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

public class TabListPing extends JavaPlugin
{
    Listeners listeners;
    
    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        try
        {
            // 1.18.1+
            this.getConfig().options().setHeader(Arrays.asList("Supported variables are %name%, %displayname%, and %ping%"));
            this.getConfig().setComments("format", null); // get rid of old comments added improperly
        }
        catch (NoSuchMethodError e)
        {
            // Older versions - This may not be necessary
            this.getConfig().options().header("# Supported variables are %name%, %displayname%, and %ping%");
        }
        if (!this.getConfig().contains("format-afk", true))
        {
            this.getConfig().set("format-afk", this.getConfig().getString("format") + " &eAFK");
        }
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
        
        getLogger().info("Metrics enabled if allowed by plugins/bStats/config.yml");
    }
        
    @Override
    public void onDisable()
    {
        //
    }
}
