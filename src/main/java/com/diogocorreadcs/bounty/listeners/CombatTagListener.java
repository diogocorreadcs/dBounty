package com.diogocorreadcs.bounty.listeners;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.models.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagListener implements Listener {

    private final BountyPlugin plugin;
    private final Map<UUID, CombatRecord> lastAttackers = new ConcurrentHashMap<>();

    public CombatTagListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = resolveAttacker(event);

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        Bounty bounty = plugin.getBountyManager().getBounty(victim.getUniqueId());
        if (bounty != null && bounty.getTotalAmount() > 0) {
            int tagSeconds = plugin.getConfig().getInt("anti-farm.combat-tag-seconds", 15);
            lastAttackers.put(victim.getUniqueId(), new CombatRecord(attacker.getUniqueId(), System.currentTimeMillis() + (tagSeconds * 1000L)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player victim = event.getPlayer();
        CombatRecord record = lastAttackers.remove(victim.getUniqueId());

        if (record == null || System.currentTimeMillis() > record.expiresAt) {
            return;
        }

        if (!plugin.getConfig().getBoolean("anti-farm.claim-on-combat-quit", true)) {
            return;
        }

        Player killer = Bukkit.getPlayer(record.attackerUuid);
        if (killer != null && killer.isOnline()) {
            plugin.getBountyManager().claimBounty(killer, victim);
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }

    private static class CombatRecord {
        private final UUID attackerUuid;
        private final long expiresAt;

        private CombatRecord(UUID attackerUuid, long expiresAt) {
            this.attackerUuid = attackerUuid;
            this.expiresAt = expiresAt;
        }
    }
}