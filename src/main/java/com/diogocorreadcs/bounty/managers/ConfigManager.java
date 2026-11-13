package com.diogocorreadcs.bounty.managers;

import com.diogocorreadcs.bounty.BountyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final BountyPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE");
    }

    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "dbounty");
    }

    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "");
    }

    public boolean isMysqlSsl() {
        return config.getBoolean("database.mysql.use-ssl", false);
    }

    public double getMinimumAmount() {
        return config.getDouble("settings.minimum-amount", 1000.0D);
    }

    public double getMaximumAmount() {
        return config.getDouble("settings.maximum-amount", 100000000.0D);
    }

    public double getTaxPercentage() {
        return config.getDouble("settings.tax-percentage", 10.0D);
    }

    public boolean isAllowStacking() {
        return config.getBoolean("settings.allow-stacking", true);
    }

    public boolean isBroadcastOnPlace() {
        return config.getBoolean("settings.broadcast-on-place", true);
    }

    public boolean isBroadcastOnClaim() {
        return config.getBoolean("settings.broadcast-on-claim", true);
    }

    public double getBroadcastMinimumAmount() {
        return config.getDouble("settings.broadcast-minimum-amount", 5000.0D);
    }

    public boolean isBlockSelfBounty() {
        return config.getBoolean("anti-farm.block-self-bounty", true);
    }

    public boolean isBlockContributorClaim() {
        return config.getBoolean("anti-farm.block-contributor-claim", true);
    }

    public boolean isBlockSameIpClaim() {
        return config.getBoolean("anti-farm.block-same-ip-claim", true);
    }

    public boolean isBlockSameClanBounty() {
        return config.getBoolean("anti-farm.block-same-clan-bounty", true);
    }

    public boolean isBlockSameClanClaim() {
        return config.getBoolean("anti-farm.block-same-clan-claim", true);
    }

    public int getClaimCooldownSeconds() {
        return config.getInt("anti-farm.claim-cooldown-seconds", 60);
    }

    public boolean isClanSystemIntegrationEnabled() {
        return config.getBoolean("integrations.dclansystem.enabled", true);
    }

    public boolean isRewardClanActivityOnClaim() {
        return config.getBoolean("integrations.dclansystem.reward-activity-on-claim", true);
    }

    public boolean isHeadDropEnabled() {
        return config.getBoolean("head-drop.enabled", true);
    }

    public boolean isHeadDropOnGround() {
        return config.getBoolean("head-drop.drop-on-ground", false);
    }

    public boolean isHeadGiveDirectlyToKiller() {
        return config.getBoolean("head-drop.give-directly-to-killer", true);
    }

    public String getCurrencySymbol() {
        return config.getString("formatting.currency-symbol", "$");
    }

    public boolean isUseShortNumbers() {
        return config.getBoolean("formatting.use-short-numbers", true);
    }

    public String getDateFormat() {
        return config.getString("formatting.date-format", "dd/MM/yyyy HH:mm");
    }

    public void playSound(Player player, String key) {
        if (player == null || !config.getBoolean("sounds.enabled", true)) {
            return;
        }
        String path = "sounds." + key;
        String soundName = config.getString(path + ".sound", "");
        if (soundName == null || soundName.isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) config.getDouble(path + ".volume", 1.0D);
            float pitch = (float) config.getDouble(path + ".pitch", 1.0D);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public String getWantedMenuTitle() {
        return color(config.getString("menus.wanted-list.title", "&8Procurados"));
    }

    public int getWantedMenuRows() {
        return Math.max(1, Math.min(6, config.getInt("menus.wanted-list.rows", 6)));
    }

    public String getTopHuntersMenuTitle() {
        return color(config.getString("menus.top-hunters.title", "&8Top Caçadores"));
    }

    public int getTopHuntersMenuRows() {
        return Math.max(1, Math.min(6, config.getInt("menus.top-hunters.rows", 4)));
    }

    public int getTopHuntersLimit() {
        return config.getInt("menus.top-hunters.limit", 10);
    }

    public List<Integer> getTopHuntersSlots() {
        List<Integer> slots = config.getIntegerList("menus.top-hunters.slots");
        if (slots == null || slots.isEmpty()) {
            return Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21);
        }
        return slots;
    }

    public boolean isFillEmptyEnabled(String menuKey) {
        return config.getBoolean("menus." + menuKey + ".fill-empty-slots.enabled", true);
    }

    public Material getFillEmptyMaterial(String menuKey) {
        return parseMaterial(config.getString("menus." + menuKey + ".fill-empty-slots.material", "STAINED_GLASS_PANE"), Material.STAINED_GLASS_PANE);
    }

    public short getFillEmptyData(String menuKey) {
        return (short) config.getInt("menus." + menuKey + ".fill-empty-slots.data", 7);
    }

    public String getFillEmptyName(String menuKey) {
        return color(config.getString("menus." + menuKey + ".fill-empty-slots.name", " "));
    }

    public int getMenuItemSlot(String menuKey, String itemKey, int defaultSlot) {
        return config.getInt("menus." + menuKey + ".items." + itemKey + ".slot", defaultSlot);
    }

    public Material getMenuItemMaterial(String menuKey, String itemKey, Material defaultMaterial) {
        return parseMaterial(config.getString("menus." + menuKey + ".items." + itemKey + ".material"), defaultMaterial);
    }

    public short getMenuItemData(String menuKey, String itemKey) {
        return (short) config.getInt("menus." + menuKey + ".items." + itemKey + ".data", 0);
    }

    public String getMenuItemName(String menuKey, String itemKey) {
        return color(config.getString("menus." + menuKey + ".items." + itemKey + ".name", ""));
    }

    public List<String> getMenuItemLore(String menuKey, String itemKey) {
        return colorList(config.getStringList("menus." + menuKey + ".items." + itemKey + ".lore"));
    }

    private Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        Material material = Material.matchMaterial(name.toUpperCase());
        return material != null ? material : fallback;
    }

    private String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    private List<String> colorList(List<String> list) {
        List<String> colored = new ArrayList<>();
        if (list != null) {
            for (String line : list) {
                colored.add(color(line));
            }
        }
        return colored;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}