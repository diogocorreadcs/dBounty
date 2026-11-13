package com.diogocorreadcs.bounty.integrations;

import com.diogocorreadcs.bounty.BountyPlugin;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class PlayerPointsHook {

    private final BountyPlugin plugin;
    private PlayerPointsAPI pointsApi;

    public PlayerPointsHook(BountyPlugin plugin) {
        this.plugin = plugin;
        Plugin target = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (target instanceof PlayerPoints) {
            this.pointsApi = ((PlayerPoints) target).getAPI();
        }
    }

    public boolean isAvailable() {
        return pointsApi != null
                && plugin.getConfig().getBoolean("integrations.playerpoints.enabled", false);
    }

    public boolean has(UUID playerUuid, double amount) {
        return isAvailable() && pointsApi.look(playerUuid) >= (int) Math.ceil(amount);
    }

    public boolean withdraw(UUID playerUuid, double amount) {
        return isAvailable() && pointsApi.take(playerUuid, (int) Math.ceil(amount));
    }

    public boolean deposit(UUID playerUuid, double amount) {
        return isAvailable() && pointsApi.give(playerUuid, (int) Math.floor(amount));
    }

    public int getBalance(UUID playerUuid) {
        return isAvailable() ? pointsApi.look(playerUuid) : 0;
    }
}
