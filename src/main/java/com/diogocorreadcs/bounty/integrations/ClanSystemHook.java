package com.diogocorreadcs.bounty.integrations;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.clansystem.ClanSystem;
import com.diogocorreadcs.clansystem.models.Clan;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ClanSystemHook {

    private final BountyPlugin plugin;
    private ClanSystem clanSystem;

    public ClanSystemHook(BountyPlugin plugin) {
        this.plugin = plugin;
        Plugin target = Bukkit.getPluginManager().getPlugin("dClanSystem");
        if (target instanceof ClanSystem) {
            this.clanSystem = (ClanSystem) target;
        }
    }

    public boolean isAvailable() {
        return this.clanSystem != null && this.clanSystem.isEnabled();
    }

    public boolean areInSameClan(UUID firstPlayer, UUID secondPlayer) {
        if (!isAvailable() || firstPlayer == null || secondPlayer == null) {
            return false;
        }
        Clan firstClan = this.clanSystem.getClanManager().getPlayerClan(firstPlayer);
        if (firstClan == null) {
            return false;
        }
        return firstClan.getMembers().contains(secondPlayer);
    }

    public String getClanTag(UUID playerUuid) {
        if (!isAvailable() || playerUuid == null) {
            return "";
        }
        Clan clan = this.clanSystem.getClanManager().getPlayerClan(playerUuid);
        return clan != null ? clan.getTag() : "";
    }

    public String getClanName(UUID playerUuid) {
        if (!isAvailable() || playerUuid == null) {
            return "";
        }
        Clan clan = this.clanSystem.getClanManager().getPlayerClan(playerUuid);
        return clan != null ? clan.getName() : "";
    }

    public void rewardClanActivity(UUID killerUuid) {
        if (!isAvailable() || killerUuid == null) {
            return;
        }
        this.clanSystem.getClanManager().recordActivity(killerUuid, "bounty-claim");
    }
}