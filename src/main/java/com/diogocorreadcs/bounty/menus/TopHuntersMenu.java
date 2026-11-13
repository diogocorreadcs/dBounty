package com.diogocorreadcs.bounty.menus;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.diogocorreadcs.bounty.managers.MessagesManager;
import com.diogocorreadcs.bounty.models.HunterStats;
import com.diogocorreadcs.bounty.utils.ItemBuilder;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TopHuntersMenu {

    private final BountyPlugin plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;

    public TopHuntersMenu(BountyPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messagesManager = plugin.getMessagesManager();
    }

    public void open(Player player) {
        int limit = configManager.getTopHuntersLimit();
        plugin.getHunterStatsRepository().getTopHuntersAsync(limit).thenAccept(topList ->
                Bukkit.getScheduler().runTask(plugin, () -> renderMenu(player, topList))
        );
    }

    private void renderMenu(Player player, List<HunterStats> topList) {
        if (!player.isOnline()) {
            return;
        }

        int rows = configManager.getTopHuntersMenuRows();
        int size = rows * 9;
        String title = configManager.getTopHuntersMenuTitle();

        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuHolder.MenuType.TOP_HUNTERS, 1), size, title);

        if (configManager.isFillEmptyEnabled("top-hunters")) {
            ItemStack filler = new ItemBuilder(
                    configManager.getFillEmptyMaterial("top-hunters"),
                    1,
                    configManager.getFillEmptyData("top-hunters")
            ).setName(configManager.getFillEmptyName("top-hunters")).build();

            for (int i = 0; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        List<Integer> slots = configManager.getTopHuntersSlots();
        for (int i = 0; i < topList.size() && i < slots.size(); i++) {
            int slot = slots.get(i);
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, buildHunterHead(topList.get(i), i + 1));
            }
        }

        int backSlot = configManager.getMenuItemSlot("top-hunters", "back-button", size - 5);
        String backName = configManager.getMenuItemName("top-hunters", "back-button");
        List<String> backLore = configManager.getMenuItemLore("top-hunters", "back-button");

        ItemStack backItem = new ItemBuilder(
                configManager.getMenuItemMaterial("top-hunters", "back-button", Material.ARROW),
                1,
                configManager.getMenuItemData("top-hunters", "back-button")
        ).setName(backName).setLore(backLore).build();

        inventory.setItem(backSlot, backItem);
        player.openInventory(inventory);
    }

    private ItemStack buildHunterHead(HunterStats stats, int position) {
        String clanTag = plugin.hasClanSystem() ? plugin.getClanSystemHook().getClanTag(stats.getPlayerUuid()) : "";
        String formattedTag = clanTag.isEmpty() ? "" : "&7[" + clanTag + "&7]";
        String formattedEarned = NumberFormatter.format(stats.getTotalEarned());
        String claimed = String.valueOf(stats.getBountiesClaimed());

        String name = messagesManager.getItemDisplayName("top-hunter-head")
                .replace("{position}", String.valueOf(position))
                .replace("{player}", stats.getPlayerName())
                .replace("{clan_tag}", formattedTag);

        List<String> lore = new ArrayList<>();
        for (String line : messagesManager.getItemDisplayLore("top-hunter-head")) {
            lore.add(line.replace("{position}", String.valueOf(position))
                    .replace("{player}", stats.getPlayerName())
                    .replace("{clan_tag}", formattedTag)
                    .replace("{earned}", formattedEarned)
                    .replace("{claimed}", claimed));
        }

        return new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkullOwner(stats.getPlayerName())
                .setName(name)
                .setLore(lore)
                .build();
    }

    public void handleClick(Player player, int slot) {
        int size = configManager.getTopHuntersMenuRows() * 9;
        int backSlot = configManager.getMenuItemSlot("top-hunters", "back-button", size - 5);

        if (slot == backSlot) {
            configManager.playSound(player, "menu-click");
            plugin.getWantedListMenu().open(player, 1);
        }
    }
}
