package com.diogocorreadcs.bounty.menus;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.BountyManager;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.diogocorreadcs.bounty.managers.MessagesManager;
import com.diogocorreadcs.bounty.models.Bounty;
import com.diogocorreadcs.bounty.models.HunterStats;
import com.diogocorreadcs.bounty.utils.ItemBuilder;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WantedListMenu {

    private final BountyPlugin plugin;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final BountyManager bountyManager;

    public WantedListMenu(BountyPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messagesManager = plugin.getMessagesManager();
        this.bountyManager = plugin.getBountyManager();
    }

    public void open(Player player, int requestedPage) {
        int rows = configManager.getWantedMenuRows();
        int size = rows * 9;
        int itemsPerPage = Math.max(9, (rows - 1) * 9);

        List<Bounty> bounties = bountyManager.getSortedBounties();
        int maxPage = Math.max(1, (int) Math.ceil((double) bounties.size() / itemsPerPage));
        int page = Math.max(1, Math.min(requestedPage, maxPage));

        String title = configManager.getWantedMenuTitle()
                .replace("{page}", String.valueOf(page))
                .replace("{max_page}", String.valueOf(maxPage));

        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuHolder.MenuType.WANTED_LIST, page), size, title);

        if (configManager.isFillEmptyEnabled("wanted-list")) {
            ItemStack filler = new ItemBuilder(
                    configManager.getFillEmptyMaterial("wanted-list"),
                    1,
                    configManager.getFillEmptyData("wanted-list")
            ).setName(configManager.getFillEmptyName("wanted-list")).build();

            for (int i = itemsPerPage; i < size; i++) {
                inventory.setItem(i, filler);
            }
        }

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, bounties.size());
        SimpleDateFormat dateFormat = new SimpleDateFormat(configManager.getDateFormat());

        for (int i = startIndex; i < endIndex; i++) {
            Bounty bounty = bounties.get(i);
            int slot = i - startIndex;
            inventory.setItem(slot, buildWantedHead(bounty, dateFormat));
        }

        if (page > 1) {
            int prevSlot = configManager.getMenuItemSlot("wanted-list", "previous-page", size - 9);
            inventory.setItem(prevSlot, buildNavItem("previous-page", page, maxPage));
        }

        if (page < maxPage) {
            int nextSlot = configManager.getMenuItemSlot("wanted-list", "next-page", size - 1);
            inventory.setItem(nextSlot, buildNavItem("next-page", page, maxPage));
        }

        int infoSlot = configManager.getMenuItemSlot("wanted-list", "info-item", size - 5);
        inventory.setItem(infoSlot, buildInfoItem(player));

        int topSlot = configManager.getMenuItemSlot("wanted-list", "top-hunters-button", size - 4);
        inventory.setItem(topSlot, buildNavItem("top-hunters-button", page, maxPage));

        player.openInventory(inventory);
    }

    private ItemStack buildWantedHead(Bounty bounty, SimpleDateFormat dateFormat) {
        String clanTag = plugin.hasClanSystem() ? plugin.getClanSystemHook().getClanTag(bounty.getTargetUuid()) : "";
        String formattedTag = clanTag.isEmpty() ? "" : "&7[" + clanTag + "&7]";
        String formattedAmount = NumberFormatter.format(bounty.getTotalAmount());
        String formattedDate = dateFormat.format(new Date(bounty.getCreatedAt()));

        String name = messagesManager.getItemDisplayName("wanted-head")
                .replace("{target}", bounty.getTargetName())
                .replace("{clan_tag}", formattedTag)
                .replace("{amount}", formattedAmount);

        List<String> lore = new ArrayList<>();
        for (String line : messagesManager.getItemDisplayLore("wanted-head")) {
            lore.add(line.replace("{target}", bounty.getTargetName())
                    .replace("{clan_tag}", formattedTag)
                    .replace("{amount}", formattedAmount)
                    .replace("{contributors_count}", String.valueOf(bounty.getContributors().size()))
                    .replace("{date}", formattedDate));
        }

        return new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkullOwner(bounty.getTargetName())
                .setName(name)
                .setLore(lore)
                .build();
    }

    private ItemStack buildInfoItem(Player player) {
        Bounty playerBounty = bountyManager.getBounty(player.getUniqueId());
        HunterStats stats = bountyManager.getHunterStats(player.getUniqueId());

        String currentBounty = NumberFormatter.format(playerBounty != null ? playerBounty.getTotalAmount() : 0.0D);
        String totalEarned = NumberFormatter.format(stats != null ? stats.getTotalEarned() : 0.0D);
        String totalClaimed = String.valueOf(stats != null ? stats.getBountiesClaimed() : 0);

        String name = configManager.getMenuItemName("wanted-list", "info-item")
                .replace("{player_bounty}", currentBounty)
                .replace("{player_earned}", totalEarned)
                .replace("{player_claimed}", totalClaimed);

        List<String> lore = new ArrayList<>();
        for (String line : configManager.getMenuItemLore("wanted-list", "info-item")) {
            lore.add(line.replace("{player_bounty}", currentBounty)
                    .replace("{player_earned}", totalEarned)
                    .replace("{player_claimed}", totalClaimed));
        }

        return new ItemBuilder(
                configManager.getMenuItemMaterial("wanted-list", "info-item", Material.BOOK),
                1,
                configManager.getMenuItemData("wanted-list", "info-item")
        ).setName(name).setLore(lore).build();
    }

    private ItemStack buildNavItem(String itemKey, int page, int maxPage) {
        String name = configManager.getMenuItemName("wanted-list", itemKey)
                .replace("{page}", String.valueOf(page))
                .replace("{max_page}", String.valueOf(maxPage))
                .replace("{prev_page}", String.valueOf(Math.max(1, page - 1)))
                .replace("{next_page}", String.valueOf(Math.min(maxPage, page + 1)));

        List<String> lore = new ArrayList<>();
        for (String line : configManager.getMenuItemLore("wanted-list", itemKey)) {
            lore.add(line.replace("{page}", String.valueOf(page))
                    .replace("{max_page}", String.valueOf(maxPage))
                    .replace("{prev_page}", String.valueOf(Math.max(1, page - 1)))
                    .replace("{next_page}", String.valueOf(Math.min(maxPage, page + 1))));
        }

        return new ItemBuilder(
                configManager.getMenuItemMaterial("wanted-list", itemKey, Material.ARROW),
                1,
                configManager.getMenuItemData("wanted-list", itemKey)
        ).setName(name).setLore(lore).build();
    }

    public void handleClick(Player player, int slot, int page) {
        int size = configManager.getWantedMenuRows() * 9;
        int prevSlot = configManager.getMenuItemSlot("wanted-list", "previous-page", size - 9);
        int nextSlot = configManager.getMenuItemSlot("wanted-list", "next-page", size - 1);
        int topSlot = configManager.getMenuItemSlot("wanted-list", "top-hunters-button", size - 4);

        if (slot == prevSlot && page > 1) {
            configManager.playSound(player, "menu-click");
            open(player, page - 1);
        } else if (slot == nextSlot) {
            int itemsPerPage = Math.max(9, (configManager.getWantedMenuRows() - 1) * 9);
            int maxPage = Math.max(1, (int) Math.ceil((double) bountyManager.getSortedBounties().size() / itemsPerPage));
            if (page < maxPage) {
                configManager.playSound(player, "menu-click");
                open(player, page + 1);
            }
        } else if (slot == topSlot) {
            configManager.playSound(player, "menu-click");
            plugin.getTopHuntersMenu().open(player);
        }
    }
}
