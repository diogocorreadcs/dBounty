package com.diogocorreadcs.bounty.models;

import java.util.UUID;

public class HunterStats {

    private final UUID playerUuid;
    private String playerName;
    private int bountiesClaimed;
    private double totalEarned;

    public HunterStats(UUID playerUuid, String playerName, int bountiesClaimed, double totalEarned) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.bountiesClaimed = bountiesClaimed;
        this.totalEarned = totalEarned;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getBountiesClaimed() {
        return bountiesClaimed;
    }

    public void setBountiesClaimed(int bountiesClaimed) {
        this.bountiesClaimed = bountiesClaimed;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public void addClaim(double reward) {
        this.bountiesClaimed++;
        this.totalEarned += reward;
    }
}
