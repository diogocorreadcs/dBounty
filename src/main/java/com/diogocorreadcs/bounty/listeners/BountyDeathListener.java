package com.diogocorreadcs.bounty.listeners;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.BountyManager;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.diogocorreadcs.bounty.managers.MessagesManager;
import com.diogocorreadcs.bounty.models.Bounty;
import com.diogocorreadcs.bounty.utils.ItemBuilder;
import com.diogocorreadcs.bounty.utils.NumberFormatter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BountyDeathListener implements Listener {

    private final BountyPlugin plugin;
    private final BountyManager bountyManager;
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;

    public BountyDeathListener(BountyPlugin plugin) {
        this.plugin = plugin;
        this.bountyManager = plugin.getBountyManager();
        this.configManager = plugin.getConfigManager();
        this.messagesManager = plugin.getMessagesManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        Bounty bounty = bountyManager.getBounty(victim.getUniqueId());
        if (bounty == null || bounty.getTotalAmount() <= 0) {
            return;
        }

        double reward = bounty.getTotalAmount();
        boolean claimed = bountyManager.claimBounty(killer, victim);

        if (claimed && configManager.isHeadDropEnabled()) {
            handleHeadDrop(event, killer, victim, reward);
        }
    }

    private void handleHeadDrop(PlayerDeathEvent event, Player killer, Player victim, double reward) {
        String formattedAmount = NumberFormatter.format(reward);
        String formattedDate = new SimpleDateFormat(configManager.getDateFormat()).format(new Date());

        String displayName = messagesManager.getItemDisplayName("dropped-head")
                .replace("{target}", victim.getName())
                .replace("{killer}", killer.getName())
                .replace("{amount}", formattedAmount)
                .replace("{date}", formattedDate);

        List<String> lore = new ArrayList<>();
        for (String line : messagesManager.getItemDisplayLore("dropped-head")) {
            lore.add(line.replace("{target}", victim.getName())
                    .replace("{killer}", killer.getName())
                    .replace("{amount}", formattedAmount)
                    .replace("{date}", formattedDate));
        }

        ItemStack skull = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkullOwner(victim.getName())
                .setName(displayName)
                .setLore(lore)
                .build();

        if (configManager.isHeadGiveDirectlyToKiller() && killer.getInventory().firstEmpty() != -1) {
            killer.getInventory().addItem(skull);
            return;
        }

        if (configManager.isHeadDropOnGround() || configManager.isHeadGiveDirectlyToKiller()) {
            event.getDrops().add(skull);
        }
    }
}
