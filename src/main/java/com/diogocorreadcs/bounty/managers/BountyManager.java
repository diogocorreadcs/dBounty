package com.diogocorreadcs.bounty.managers;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.database.BountyRepository;
import com.diogocorreadcs.bounty.database.HunterStatsRepository;
import com.diogocorreadcs.bounty.models.Bounty;
import com.diogocorreadcs.bounty.models.HunterStats;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BountyManager {

    private final BountyPlugin plugin;
    private final BountyRepository bountyRepository;
    private final HunterStatsRepository statsRepository;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final AntiFarmManager antiFarmManager;

    private final Map<UUID, Bounty> activeBounties = new ConcurrentHashMap<>();
    private final Map<UUID, HunterStats> statsCache = new ConcurrentHashMap<>();

    public BountyManager(BountyPlugin plugin, BountyRepository bountyRepository,
                         HunterStatsRepository statsRepository, ConfigManager configManager) {
        this.plugin = plugin;
        this.bountyRepository = bountyRepository;
        this.statsRepository = statsRepository;
        this.configManager = configManager;
        this.messagesManager = plugin.getMessagesManager();
        this.antiFarmManager = new AntiFarmManager(plugin);
    }

    public void loadCache() {
        activeBounties.clear();
        activeBounties.putAll(bountyRepository.loadAllActiveBounties());
        Bukkit.getOnlinePlayers().forEach(p -> loadPlayerStatsAsync(p.getUniqueId(), p.getName()));
    }

    public void saveAllSync() {
        activeBounties.values().forEach(bountyRepository::saveBountySync);
        statsCache.values().forEach(statsRepository::saveHunterStatsSync);
    }

    public void placeBounty(Player sender, OfflinePlayer target, double amount) {
        if (!antiFarmManager.canPlaceBounty(sender, target) || !validateAmount(sender, target, amount)) {
            return;
        }

        double taxPercent = configManager.getTaxPercentage();
        double taxAmount = amount * (taxPercent / 100.0D);
        double totalCost = amount + taxAmount;

        if (!hasBalance(sender, totalCost)) {
            sendError(sender, messagesManager.getMessage("bounty.not-enough-money")
                    .replace("{total_cost}", NumberFormatter.format(totalCost))
                    .replace("{tax}", String.valueOf(taxPercent)));
            return;
        }

        if (!withdrawBalance(sender, totalCost)) {
            return;
        }

        String targetName = target.getName() != null ? target.getName() : "Desconhecido";
        Bounty existing = activeBounties.get(target.getUniqueId());
        boolean isStacking = existing != null;

        Bounty bounty = isStacking ? existing : new Bounty(target.getUniqueId(), targetName, 0.0D, System.currentTimeMillis());
        bounty.setTargetName(targetName);
        bounty.addContribution(sender.getUniqueId(), amount);

        activeBounties.put(target.getUniqueId(), bounty);
        bountyRepository.saveBountyAsync(bounty);

        if (plugin.getDiscordWebhookHook() != null) {
            plugin.getDiscordWebhookHook().sendBountyPlaced(sender.getName(), targetName, amount, bounty.getTotalAmount());
        }

        notifyBountyPlaced(sender, targetName, amount, taxAmount, bounty.getTotalAmount(), isStacking);
    }

    private boolean validateAmount(Player sender, OfflinePlayer target, double amount) {
        double min = configManager.getMinimumAmount();
        if (amount < min) {
            sendError(sender, messagesManager.getMessage("bounty.below-minimum")
                    .replace("{min}", NumberFormatter.format(min)));
            return false;
        }

        double max = configManager.getMaximumAmount();
        if (amount > max) {
            sendError(sender, messagesManager.getMessage("bounty.above-maximum")
                    .replace("{max}", NumberFormatter.format(max)));
            return false;
        }

        if (activeBounties.containsKey(target.getUniqueId()) && !configManager.isAllowStacking()) {
            String name = target.getName() != null ? target.getName() : "Desconhecido";
            sendError(sender, messagesManager.getMessage("bounty.already-has-bounty")
                    .replace("{target}", name));
            return false;
        }

        return true;
    }

    private void notifyBountyPlaced(Player sender, String targetName, double amount, double taxAmount, double total, boolean isStacking) {
        String fmtAmount = NumberFormatter.format(amount);
        String fmtTotal = NumberFormatter.format(total);
        String fmtTax = NumberFormatter.format(taxAmount);

        String messageKey = isStacking ? "bounty.stacked-success" : "bounty.placed-success";
        sender.sendMessage(messagesManager.getMessage(messageKey)
                .replace("{amount}", fmtAmount)
                .replace("{target}", targetName)
                .replace("{total}", fmtTotal)
                .replace("{tax_amount}", fmtTax));

        configManager.playSound(sender, "bounty-placed");

        if (configManager.isBroadcastOnPlace() && amount >= configManager.getBroadcastMinimumAmount()) {
            messagesManager.getMessageList("bounty.placed-broadcast").forEach(line ->
                    Bukkit.broadcastMessage(line
                            .replace("{sender}", sender.getName())
                            .replace("{target}", targetName)
                            .replace("{amount}", fmtAmount)
                            .replace("{total}", fmtTotal)));
        }
    }

    public boolean claimBounty(Player killer, Player victim) {
        Bounty bounty = activeBounties.get(victim.getUniqueId());
        if (bounty == null || bounty.getTotalAmount() <= 0 || !antiFarmManager.canClaimBounty(killer, victim, bounty)) {
            return false;
        }

        double reward = bounty.getTotalAmount();
        activeBounties.remove(victim.getUniqueId());
        bountyRepository.deleteBountyAsync(victim.getUniqueId());

        depositBalance(killer, reward);
        antiFarmManager.registerClaimCooldown(killer.getUniqueId());

        if (plugin.getDiscordWebhookHook() != null) {
            plugin.getDiscordWebhookHook().sendBountyClaimed(killer.getName(), victim.getName(), reward);
        }

        updateHunterStats(killer, reward);

        if (configManager.isClanSystemIntegrationEnabled() && configManager.isRewardClanActivityOnClaim() && plugin.hasClanSystem()) {
            plugin.getClanSystemHook().rewardClanActivity(killer.getUniqueId());
        }

        notifyBountyClaimed(killer, victim.getName(), reward);
        return true;
    }

    private void updateHunterStats(Player killer, double reward) {
        HunterStats stats = statsCache.computeIfAbsent(killer.getUniqueId(),
                uuid -> new HunterStats(uuid, killer.getName(), 0, 0.0D));
        stats.setPlayerName(killer.getName());
        stats.addClaim(reward);
        statsRepository.saveHunterStatsAsync(stats);
    }

    private void notifyBountyClaimed(Player killer, String victimName, double reward) {
        String fmtReward = NumberFormatter.format(reward);
        killer.sendMessage(messagesManager.getMessage("claim.success")
                .replace("{target}", victimName)
                .replace("{amount}", fmtReward));
        configManager.playSound(killer, "bounty-claimed");

        if (configManager.isBroadcastOnClaim() && reward >= configManager.getBroadcastMinimumAmount()) {
            messagesManager.getMessageList("claim.broadcast").forEach(line ->
                    Bukkit.broadcastMessage(line
                            .replace("{killer}", killer.getName())
                            .replace("{target}", victimName)
                            .replace("{amount}", fmtReward)));
        }
    }

    private void sendError(Player player, String message) {
        player.sendMessage(message);
        configManager.playSound(player, "error");
    }

    private boolean isUsingPoints() {
        return plugin.getPlayerPointsHook() != null && plugin.getPlayerPointsHook().isAvailable();
    }

    private boolean hasBalance(Player player, double amount) {
        return isUsingPoints()
                ? plugin.getPlayerPointsHook().has(player.getUniqueId(), amount)
                : plugin.getVaultHook().has(player, amount);
    }

    private boolean withdrawBalance(Player player, double amount) {
        return isUsingPoints()
                ? plugin.getPlayerPointsHook().withdraw(player.getUniqueId(), amount)
                : plugin.getVaultHook().withdraw(player, amount);
    }

    private void depositBalance(Player player, double amount) {
        if (isUsingPoints()) {
            plugin.getPlayerPointsHook().deposit(player.getUniqueId(), amount);
        } else {
            plugin.getVaultHook().deposit(player, amount);
        }
    }

    public boolean removeBounty(UUID targetUuid) {
        if (activeBounties.remove(targetUuid) != null) {
            bountyRepository.deleteBountyAsync(targetUuid);
            return true;
        }
        return false;
    }

    public void updateTargetNameIfActive(UUID targetUuid, String newName) {
        Bounty bounty = activeBounties.get(targetUuid);
        if (bounty != null && !bounty.getTargetName().equals(newName)) {
            bounty.setTargetName(newName);
            bountyRepository.saveBountyAsync(bounty);
        }
    }

    public void loadPlayerStatsAsync(UUID playerUuid, String playerName) {
        statsRepository.loadHunterStatsAsync(playerUuid, playerName).thenAccept(stats -> {
            stats.setPlayerName(playerName);
            statsCache.put(playerUuid, stats);
        });
    }

    public void unloadPlayerStatsAsync(UUID playerUuid) {
        HunterStats stats = statsCache.remove(playerUuid);
        if (stats != null) {
            statsRepository.saveHunterStatsAsync(stats);
        }
    }

    public Bounty getBounty(UUID targetUuid) {
        return activeBounties.get(targetUuid);
    }

    public List getSortedBounties() {
        List list = new ArrayList<>(activeBounties.values());
        list.sort(Comparator.comparingDouble(Bounty::getTotalAmount).reversed());
        return list;
    }

    public HunterStats getHunterStats(UUID playerUuid) {
        return statsCache.get(playerUuid);
    }
}