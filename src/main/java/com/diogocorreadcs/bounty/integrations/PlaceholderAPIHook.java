package com.diogocorreadcs.bounty.integrations;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.models.Bounty;
import com.diogocorreadcs.bounty.models.HunterStats;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final BountyPlugin plugin;

    public PlaceholderAPIHook(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "dbounty";
    }

    @Override
    public String getAuthor() {
        return "diogocorreadcs";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        Bounty bounty = plugin.getBountyManager().getBounty(player.getUniqueId());
        HunterStats stats = plugin.getBountyManager().getHunterStats(player.getUniqueId());

        double amount = bounty != null ? bounty.getTotalAmount() : 0.0D;
        double earned = stats != null ? stats.getTotalEarned() : 0.0D;
        int claimed = stats != null ? stats.getBountiesClaimed() : 0;

        switch (params.toLowerCase()) {
            case "amount":
                return NumberFormatter.format(amount);
            case "amount_raw":
                return String.valueOf(amount);
            case "has_bounty":
                return String.valueOf(amount > 0);
            case "contributors":
                return bounty != null ? String.valueOf(bounty.getContributors().size()) : "0";
            case "claimed":
                return String.valueOf(claimed);
            case "kills":
                return String.valueOf(claimed);
            case "earned":
                return NumberFormatter.format(earned);
            case "earned_raw":
                return String.valueOf(earned);
            default:
                return null;
        }
    }
}