package com.diogocorreadcs.bounty.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    public enum MenuType {
        WANTED_LIST,
        TOP_HUNTERS
    }

    private final MenuType menuType;
    private final int page;

    public MenuHolder(MenuType menuType, int page) {
        this.menuType = menuType;
        this.page = page;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
