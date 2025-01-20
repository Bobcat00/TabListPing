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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class Commands implements CommandExecutor, TabCompleter
{
    private TabListPing plugin;
    
    public Commands(TabListPing plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player && !sender.hasPermission("tablistping.command"))
        {
            sender.sendMessage("You do not have permission for this command");
            return true;
        }
        
        // Subcommands
        
        if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
        {
            if (sender instanceof Player && !sender.hasPermission("tablistping.command.reload"))
            {
                sender.sendMessage("You do not have permission for the reload command");
                return true;
            }
            plugin.config.reloadConfig();
            sender.sendMessage(ChatColor.AQUA + "TabListPing config reloaded");
            return true; // Normal return
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("report"))
        {
            if (sender instanceof Player && !sender.hasPermission("tablistping.command.report"))
            {
                sender.sendMessage("You do not have permission for the report command");
                return true;
            }
            Map<String, Integer> map = plugin.listeners.getPingReport();
            if (map.isEmpty())
            {
                sender.sendMessage(ChatColor.AQUA + "No players online");
            }
            else
            {
                sender.sendMessage(ChatColor.AQUA + "Ping report:");
                for (String name : map.keySet())
                {
                    Integer ping = map.get(name);
                    sender.sendMessage(ChatColor.AQUA + name + ":  " + ping + " msec");
                }
            }
            return true; // Normal return
        }
        else
        {
            sender.sendMessage(ChatColor.AQUA + "TabListPing version " + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.AQUA + "Reload - Reload config file");
            sender.sendMessage(ChatColor.AQUA + "Report - Output ping report");
            return true; // Normal return
        }
        
        // return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
    {
        List<String> argList = new ArrayList<>();
        if (args.length == 1)
        {
            argList.add("reload");
            argList.add("report");
            return argList.stream().filter(a -> a.startsWith(args[0])).collect(Collectors.toList());
        }
        return argList; // returns an empty list
    }

}
