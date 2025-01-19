// TabListPing - Displays ping time in CraftBukkit/Spigot player list
// Copyright 2023 Bobcat00
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

public class Config
{
    private TabListPing plugin;
    
    public Config(TabListPing plugin)
    {
        this.plugin = plugin;
    }
    
    //--------------------------------------------------------------------------
    
    public String getFormat()
    {
        return plugin.getConfig().getString("format");
    }
    
    public String getFormatAfk()
    {
        return plugin.getConfig().getString("format-afk");
    }
    
    public boolean getEnableTps()
    {
        return plugin.getConfig().getBoolean("enable-tps");
    }
    
    public String getFormatHeader()
    {
        return plugin.getConfig().getString("format-header");
    }
    
    public String getFormatFooter()
    {
        return plugin.getConfig().getString("format-footer");
    }
    
    public boolean getEnableMetrics()
    {
        return plugin.getConfig().getBoolean("enable-metrics");
    }
    
    //--------------------------------------------------------------------------
    
    // Ensure all entries are in the config file, in case SnakeYAML got rid of
    // any. This can happen if the user made some bad edits.
    
    public void updateConfig()
    {
        if (!plugin.getConfig().contains("format", true))
        {
            plugin.getConfig().set("format", "%name% &7[&a%ping%ms&7]");
        }
        
        if (!plugin.getConfig().contains("format-afk", true))
        {
            plugin.getConfig().set("format-afk", plugin.getConfig().getString("format") + " &eAFK");
        }
        
        if (!plugin.getConfig().contains("enable-tps", true))
        {
            plugin.getConfig().set("enable-tps",  false);
            plugin.getConfig().set("format-header", "");
            plugin.getConfig().set("format-footer", "&7TPS: %tps%   &7MSPT: %mspt%");
        }
        
        if (!plugin.getConfig().contains("enable-metrics", true))
        {
            plugin.getConfig().set("enable-metrics",  true);
        }
    }
    
    //--------------------------------------------------------------------------
    
    // Ensure the correct comments are in the config file, just in case the user
    // messed with them.
    
    public void setComments()
    {
        plugin.getConfig().options().setHeader(null); // Remove old header
        
        plugin.getConfig().setComments("format", Arrays.asList("Tab list player name format",
                                                               "Supported variables are %name%, %displayname%, and %ping%"));
        
        plugin.getConfig().setComments("enable-tps", Arrays.asList(null, // Blank line
                                                                   "Enable header/footer display",
                                                                   "Supported variables are %name%, %displayname%, %tps%, %mspt%, %load%, and %world%",
                                                                   "Spigot does not support %tps%, %mspt%, or %load%"));
        
        plugin.getConfig().setComments("enable-metrics", Arrays.asList(null, // Blank line
                                                                       "Enable metrics (subject to bStats global config)"));
    }
    
    //--------------------------------------------------------------------------
    
    // Reload the config file. This has to account for the case where enable-tps
    // has changed.
    
    public void reloadConfig()
    {
        boolean oldEnableTps = plugin.getConfig().getBoolean("enable-tps");
        
        plugin.reloadConfig();
        updateConfig();
        setComments();
        plugin.saveConfig();
        
        boolean newEnableTps = plugin.getConfig().getBoolean("enable-tps");
        
        // Start or cancel the periodic task if the setting has changed.
        
        if (newEnableTps != oldEnableTps)
        {
            if (newEnableTps)
            {
                // Start a new periodic task
                plugin.tpsTask = new TpsTask(plugin);
                plugin.tpsTask.runTaskTimer(plugin,  // plugin
                                            0,       // delay
                                            5L*20L); // period
            }
            else if (plugin.tpsTask != null)
            {
                // Cancel periodic task
                plugin.tpsTask.cancel();
                // CLear any headers and footers we set
                plugin.tpsTask.clearAllHeadersFooters();
            }
        }
    }

}
