package com.diogocorreadcs.bounty.integrations;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WorldGuardHook {

    private final BountyPlugin plugin;
    private WorldGuardPlugin worldGuard;

    public WorldGuardHook(BountyPlugin plugin) {
        this.plugin = plugin;
        Plugin target = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (target instanceof WorldGuardPlugin) {
            this.worldGuard = (WorldGuardPlugin) target;
        }
    }

    public boolean isAvailable() {
        return worldGuard != null && worldGuard.isEnabled()
                && plugin.getConfig().getBoolean("integrations.worldguard.enabled", true);
    }

    public boolean isInBlockedRegion(Player player) {
        if (!isAvailable() || player == null) {
            return false;
        }

        List<String> blockedRegions = plugin.getConfig().getStringList("integrations.worldguard.blocked-regions");
        if (blockedRegions == null || blockedRegions.isEmpty()) {
            return false;
        }

        Location loc = player.getLocation();
        RegionManager regionManager = worldGuard.getRegionManager(loc.getWorld());
        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(loc);
        for (ProtectedRegion region : regions) {
            for (String blocked : blockedRegions) {
                if (region.getId().equalsIgnoreCase(blocked)) {
                    return true;
                }
            }
        }
        return false;
    }
}