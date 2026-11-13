package com.diogocorreadcs.bounty.managers;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.models.Bounty;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiFarmManager {

    private final BountyPlugin plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final Map<UUID, Long> claimCooldowns = new ConcurrentHashMap<>();

    public AntiFarmManager(BountyPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messagesManager = plugin.getMessagesManager();
    }

    public boolean canPlaceBounty(Player sender, OfflinePlayer target) {
        if (plugin.getWorldGuardHook() != null && plugin.getWorldGuardHook().isInBlockedRegion(sender)) {
            sender.sendMessage(messagesManager.getMessage("bounty.blocked-region"));
            configManager.playSound(sender, "error");
            return false;
        }

        if (configManager.isBlockSelfBounty() && sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(messagesManager.getMessage("bounty.self-bounty"));
            configManager.playSound(sender, "error");
            return false;
        }

        if (configManager.isClanSystemIntegrationEnabled() && configManager.isBlockSameClanBounty()
                && plugin.hasClanSystem() && plugin.getClanSystemHook().areInSameClan(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(messagesManager.getMessage("bounty.same-clan-bounty"));
            configManager.playSound(sender, "error");
            return false;
        }

        return true;
    }

    public boolean canClaimBounty(Player killer, Player victim, Bounty bounty) {
        if (plugin.getWorldGuardHook() != null
                && (plugin.getWorldGuardHook().isInBlockedRegion(killer) || plugin.getWorldGuardHook().isInBlockedRegion(victim))) {
            killer.sendMessage(messagesManager.getMessage("claim.blocked-region"));
            configManager.playSound(killer, "error");
            return false;
        }

        if (configManager.isBlockContributorClaim() && bounty.hasContributed(killer.getUniqueId())) {
            killer.sendMessage(messagesManager.getMessage("claim.blocked-contributor"));
            configManager.playSound(killer, "error");
            return false;
        }

        if (configManager.isBlockSameIpClaim() && hasSameIp(killer, victim)) {
            killer.sendMessage(messagesManager.getMessage("claim.blocked-same-ip"));
            configManager.playSound(killer, "error");
            return false;
        }

        if (configManager.isClanSystemIntegrationEnabled() && configManager.isBlockSameClanClaim()
                && plugin.hasClanSystem() && plugin.getClanSystemHook().areInSameClan(killer.getUniqueId(), victim.getUniqueId())) {
            killer.sendMessage(messagesManager.getMessage("claim.blocked-same-clan"));
            configManager.playSound(killer, "error");
            return false;
        }

        int cooldownSeconds = configManager.getClaimCooldownSeconds();
        if (cooldownSeconds > 0 && claimCooldowns.containsKey(killer.getUniqueId())) {
            long expiresAt = claimCooldowns.get(killer.getUniqueId());
            long remaining = (expiresAt - System.currentTimeMillis()) / 1000L;
            if (remaining > 0) {
                killer.sendMessage(messagesManager.getMessage("claim.blocked-cooldown")
                        .replace("{time}", String.valueOf(remaining)));
                configManager.playSound(killer, "error");
                return false;
            }
        }

        return true;
    }

    public void registerClaimCooldown(UUID killerUuid) {
        int cooldownSeconds = configManager.getClaimCooldownSeconds();
        if (cooldownSeconds > 0) {
            claimCooldowns.put(killerUuid, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }
    }

    private boolean hasSameIp(Player first, Player second) {
        InetSocketAddress addr1 = first.getAddress();
        InetSocketAddress addr2 = second.getAddress();
        if (addr1 == null || addr2 == null || addr1.getAddress() == null || addr2.getAddress() == null) {
            return false;
        }
        return addr1.getAddress().getHostAddress().equals(addr2.getAddress().getHostAddress());
    }
}