package com.diogocorreadcs.bounty.listeners;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.BountyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final BountyManager bountyManager;

    public PlayerConnectionListener(BountyPlugin plugin) {
        this.bountyManager = plugin.getBountyManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        bountyManager.loadPlayerStatsAsync(player.getUniqueId(), player.getName());
        bountyManager.updateTargetNameIfActive(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        bountyManager.unloadPlayerStatsAsync(event.getPlayer().getUniqueId());
    }
}
