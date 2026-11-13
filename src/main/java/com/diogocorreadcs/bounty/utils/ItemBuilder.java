package com.diogocorreadcs.bounty.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1, (short) 0);
    }

    public ItemBuilder(Material material, int amount, short data) {
        this.item = new ItemStack(material, amount, data);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(ChatUtils.color(name));
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (meta != null && lore != null) {
            meta.setLore(ChatUtils.color(Arrays.asList(lore)));
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (meta != null && lore != null) {
            meta.setLore(ChatUtils.color(lore));
        }
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta && owner != null && !owner.isEmpty()) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (meta != null && glow) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
