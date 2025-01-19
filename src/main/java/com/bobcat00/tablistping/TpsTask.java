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

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.papermc.lib.PaperLib;

// This class primarily handles updating the tab list header and footer,
// including the TPS and load statistics.

@SuppressWarnings("deprecation")
public class TpsTask extends BukkitRunnable
{
    private TabListPing plugin;
    
    // These variables have to be kept around for when a player changes worlds
    private String header = "";
    private String footer = "";
    
    public TpsTask(TabListPing plugin)
    {
        this.plugin = plugin;
    }
    
    // -------------------------------------------------------------------------
    
    // This task is run periodically
    @Override
    public void run()
    {
        String tpsString;
        String msptString;
        String loadString;
        
        if (PaperLib.isPaper())
        {
            // Calculate TPS, MSPT, and load for Paper
            final double tps = StrictMath.min(plugin.getServer().getTPS()[0], 20.0);
            final double mspt = plugin.getServer().getAverageTickTime();

            final DecimalFormat df1 = new DecimalFormat("#.0");

            // Colors for the numeric values
            final String tpsColor  = (tps  >= 18.0 ? ChatColor.GREEN.toString() : tps  >= 15.0 ? ChatColor.YELLOW.toString() : ChatColor.RED.toString());
            final String msptColor = (mspt >= 50.0 ? ChatColor.RED.toString()   : mspt >= 40.0 ? ChatColor.YELLOW.toString() : ChatColor.GREEN.toString());

            // Build the replacement strings including the color tags
            tpsString  = tpsColor  + df1.format(tps);
            msptString = msptColor + df1.format(mspt);
            loadString = msptColor + df1.format(mspt*2.0);
        }
        else
        {
            // Use empty strings for Spigot
            tpsString = "";
            msptString = "";
            loadString = "";
        }
        
        // Create the strings with the non-player-specific variables replaced
        // These are reused if a player changes worlds
        header = plugin.config.getFormatHeader();
        header = header.replace("%tps%", tpsString).
                        replace("%mspt%", msptString).
                        replace("%load%", loadString);
        
        footer = plugin.config.getFormatFooter();
        footer = footer.replace("%tps%", tpsString).
                        replace("%mspt%", msptString).
                        replace("%load%", loadString);
        
        // Finish up by replacing the player-specific variables
        for (Player p : plugin.getServer().getOnlinePlayers())
        {
            setHeaderFooter(p);
        }
    }
    
    // -------------------------------------------------------------------------
    
    // Set this player's header and footer. This uses the previously created
    // header and footer strings, and replaces the player's variables.
    
    void setHeaderFooter(Player player)
    {
        if (player.hasPermission("tablistping.header"))
        {
            player.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&',
                       header.replace("%name%",        player.getName()).
                              replace("%displayname%", player.getDisplayName()).
                              replace("%world%",       player.getWorld().getName())));
        }
        if (player.hasPermission("tablistping.footer"))
        {
            player.setPlayerListFooter(ChatColor.translateAlternateColorCodes('&',
                       footer.replace("%name%",        player.getName()).
                              replace("%displayname%", player.getDisplayName()).
                              replace("%world%",       player.getWorld().getName())));
        }
    }
    
    // -------------------------------------------------------------------------
    
    // Clear all headers and footers for all players that have the required
    // permissions.
    
    void clearAllHeadersFooters()
    {
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            if (player.hasPermission("tablistping.header"))
            {
                player.setPlayerListHeader("");
            }
            if (player.hasPermission("tablistping.footer"))
            {
                player.setPlayerListFooter("");
            }
        }
    }
}
