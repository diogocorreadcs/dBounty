package com.diogocorreadcs.bounty.listeners;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.menus.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuClickListener implements Listener {

    private final BountyPlugin plugin;

    public MenuClickListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        MenuHolder menuHolder = (MenuHolder) holder;
        int slot = event.getSlot();

        if (menuHolder.getMenuType() == MenuHolder.MenuType.WANTED_LIST) {
            plugin.getWantedListMenu().handleClick(player, slot, menuHolder.getPage());
        } else if (menuHolder.getMenuType() == MenuHolder.MenuType.TOP_HUNTERS) {
            plugin.getTopHuntersMenu().handleClick(player, slot);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
}
