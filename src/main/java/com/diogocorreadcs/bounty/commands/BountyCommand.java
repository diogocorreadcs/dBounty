package com.diogocorreadcs.bounty.commands;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.BountyManager;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.diogocorreadcs.bounty.managers.MessagesManager;
import com.diogocorreadcs.bounty.models.Bounty;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

    private final BountyPlugin plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final BountyManager bountyManager;

    public BountyCommand(BountyPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messagesManager = plugin.getMessagesManager();
        this.bountyManager = plugin.getBountyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messagesManager.getMessage("general.only-players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("dbounty.use")) {
                player.sendMessage(messagesManager.getMessage("general.no-permission"));
                configManager.playSound(player, "error");
                return true;
            }
            plugin.getWantedListMenu().open(player, 1);
            configManager.playSound(player, "menu-click");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "colocar":
            case "place":
            case "add":
                handlePlace(sender, args);
                break;

            case "ver":
            case "check":
            case "info":
                handleCheck(sender, args);
                break;

            case "top":
            case "ranking":
                handleTop(sender);
                break;

            case "remover":
            case "remove":
            case "delete":
                handleRemove(sender, args);
                break;

            case "reload":
            case "recarregar":
                handleReload(sender);
                break;

            case "ajuda":
            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handlePlace(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messagesManager.getMessage("general.only-players"));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("dbounty.place")) {
            player.sendMessage(messagesManager.getMessage("general.no-permission"));
            configManager.playSound(player, "error");
            return;
        }

        if (args.length < 3) {
            sendHelp(player);
            return;
        }

        OfflinePlayer target = findOfflinePlayer(args[1]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(messagesManager.getMessage("general.player-not-found")
                    .replace("{target}", args[1]));
            configManager.playSound(player, "error");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(messagesManager.getMessage("general.invalid-amount"));
            configManager.playSound(player, "error");
            return;
        }

        bountyManager.placeBounty(player, target, amount);
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dbounty.check")) {
            sender.sendMessage(messagesManager.getMessage("general.no-permission"));
            if (sender instanceof Player) {
                configManager.playSound((Player) sender, "error");
            }
            return;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return;
        }

        OfflinePlayer target = findOfflinePlayer(args[1]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(messagesManager.getMessage("general.player-not-found")
                    .replace("{target}", args[1]));
            return;
        }

        Bounty bounty = bountyManager.getBounty(target.getUniqueId());
        String targetName = target.getName() != null ? target.getName() : args[1];

        if (bounty == null || bounty.getTotalAmount() <= 0) {
            sender.sendMessage(messagesManager.getMessage("bounty.check-none")
                    .replace("{target}", targetName));
            return;
        }

        String formattedAmount = NumberFormatter.format(bounty.getTotalAmount());
        sender.sendMessage(messagesManager.getMessage("bounty.check-has")
                .replace("{target}", targetName)
                .replace("{amount}", formattedAmount));
    }

    private void handleTop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messagesManager.getMessage("general.only-players"));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("dbounty.top")) {
            player.sendMessage(messagesManager.getMessage("general.no-permission"));
            configManager.playSound(player, "error");
            return;
        }

        plugin.getTopHuntersMenu().open(player);
        configManager.playSound(player, "menu-click");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dbounty.admin")) {
            sender.sendMessage(messagesManager.getMessage("general.no-permission"));
            if (sender instanceof Player) {
                configManager.playSound((Player) sender, "error");
            }
            return;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return;
        }

        OfflinePlayer target = findOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messagesManager.getMessage("general.player-not-found")
                    .replace("{target}", args[1]));
            return;
        }

        String targetName = target.getName() != null ? target.getName() : args[1];
        boolean removed = bountyManager.removeBounty(target.getUniqueId());

        if (removed) {
            sender.sendMessage(messagesManager.getMessage("bounty.removed-admin")
                    .replace("{target}", targetName));
        } else {
            sender.sendMessage(messagesManager.getMessage("bounty.none-to-remove")
                    .replace("{target}", targetName));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("dbounty.admin")) {
            sender.sendMessage(messagesManager.getMessage("general.no-permission"));
            if (sender instanceof Player) {
                configManager.playSound((Player) sender, "error");
            }
            return;
        }

        configManager.loadConfig();
        messagesManager.loadMessages();
        sender.sendMessage(messagesManager.getMessage("general.reloaded"));
    }

    private void sendHelp(CommandSender sender) {
        for (String line : messagesManager.getMessageList("help.lines")) {
            sender.sendMessage(line);
        }
        if (sender.hasPermission("dbounty.admin")) {
            for (String line : messagesManager.getMessageList("help.admin-lines")) {
                sender.sendMessage(line);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer findOfflinePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayer(name);
    }
}
