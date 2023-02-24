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

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TpsTask extends BukkitRunnable
{
    private TabListPing plugin;
    
    // These variables have to be kept around for when a player changes worlds
    private String header = "";
    private String footer = "";
    
    private static final MiniMessage mm = MiniMessage.miniMessage();
    
    public TpsTask(TabListPing plugin)
    {
        this.plugin = plugin;
    }
    
    // -------------------------------------------------------------------------
    
    // This task is run periodically
    @Override
    public void run()
    {
        final double tps = StrictMath.min(plugin.getServer().getTPS()[0], 20.0);
        final double mspt = plugin.getServer().getAverageTickTime();
        
        final DecimalFormat df1 = new DecimalFormat("#.0");
        
        // Colors for the numeric values. Closing tags are specified. The / will be removed when creating the opening tag.
        final String tpsColor  = (tps  >= 18.0 ? "</green>" : tps  >= 15.0 ? "</yellow>" : "</red>");
        final String msptColor = (mspt >= 50.0 ? "</red>"   : mspt >= 40.0 ? "</yellow>" : "</green>");
        
        // Build the replacement strings including the color tags
        final String tpsString = new StringBuilder(tpsColor).deleteCharAt(1).append(df1.format(tps)).append(tpsColor).toString();
        final String msptString = new StringBuilder(msptColor).deleteCharAt(1).append(df1.format(mspt)).append(msptColor).toString();
        final String loadString = new StringBuilder(msptColor).deleteCharAt(1).append(df1.format(mspt*2.0)).append(msptColor).append("%").toString();
        
        // Create the strings with the non-player-specific variables replaced
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
    // header and footer strings, and replaces the player's %world% variable.
    
    void setHeaderFooter(Player player)
    {
        if (player.hasPermission("tablistping.header"))
        {
            Component headerComponent = mm.deserialize(header.replace("%world%", player.getWorld().getName()));
            player.sendPlayerListHeader(headerComponent);
        }
        if (player.hasPermission("tablistping.footer"))
        {
            Component footerComponent = mm.deserialize(footer.replace("%world%", player.getWorld().getName()));
            player.sendPlayerListFooter(footerComponent);
        }
    }
    
    // -------------------------------------------------------------------------
    
    // Clear all headers and footers for all players, if they have the required
    // permissions.
    
    void clearAllHeadersFooters()
    {
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            if (player.hasPermission("tablistping.header"))
            {
                player.sendPlayerListHeader(mm.deserialize(""));
            }
            if (player.hasPermission("tablistping.footer"))
            {
                player.sendPlayerListFooter(mm.deserialize(""));
            }

        }

    }
}
