package com.diogocorreadcs.bounty.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BountyTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("colocar", "ver", "top", "ajuda"));
            if (sender.hasPermission("dbounty.admin")) {
                subCommands.add("remover");
                subCommands.add("reload");
            }
            return filterPrefix(subCommands, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("colocar") || sub.equals("place") || sub.equals("ver") || sub.equals("check")
                    || (sub.equals("remover") && sender.hasPermission("dbounty.admin"))) {
                List<String> players = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    players.add(online.getName());
                }
                return filterPrefix(players, args[1]);
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("colocar") || args[0].equalsIgnoreCase("place"))) {
            return Arrays.asList("1000", "5000", "10000", "50000");
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerPrefix)) {
                result.add(option);
            }
        }
        return result;
    }
}
