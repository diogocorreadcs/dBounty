package com.diogocorreadcs.bounty.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bounty {

    private final UUID targetUuid;
    private String targetName;
    private double totalAmount;
    private final long createdAt;
    private final Map<UUID, Double> contributors;

    public Bounty(UUID targetUuid, String targetName, double totalAmount, long createdAt) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.contributors = new HashMap<>();
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void addContribution(UUID contributorUuid, double amount) {
        this.totalAmount += amount;
        this.contributors.merge(contributorUuid, amount, Double::sum);
    }

    public void setContributorAmount(UUID contributorUuid, double amount) {
        this.contributors.put(contributorUuid, amount);
    }

    public boolean hasContributed(UUID playerUuid) {
        return this.contributors.containsKey(playerUuid);
    }

    public double getContribution(UUID playerUuid) {
        return this.contributors.getOrDefault(playerUuid, 0.0D);
    }

    public Map<UUID, Double> getContributors() {
        return Collections.unmodifiableMap(contributors);
    }
}
