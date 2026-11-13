package com.diogocorreadcs.bounty.managers;

import com.diogocorreadcs.bounty.BountyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessagesManager {

    private final BountyPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessagesManager(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!this.messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(this.messagesFile);
        loadDefaults();
    }

    private void loadDefaults() {
        try (InputStream stream = plugin.getResource("messages.yml")) {
            if (stream != null) {
                YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                this.messages.setDefaults(defaultMessages);
            }
        } catch (IOException ignored) {
        }
    }

    public String getPrefix() {
        return color(messages.getString("prefix", "&6&ldBOUNTY &8» &r"));
    }

    public String getMessage(String path) {
        String raw = messages.getString(path, "&cMensagem nao encontrada: " + path);
        return color(raw.replace("{prefix}", getPrefix()));
    }

    public List<String> getMessageList(String path) {
        List<String> rawList = messages.getStringList(path);
        List<String> formatted = new ArrayList<>();
        String prefix = getPrefix();
        for (String line : rawList) {
            formatted.add(color(line.replace("{prefix}", prefix)));
        }
        return formatted;
    }

    public String getItemDisplayName(String itemKey) {
        return color(messages.getString("items." + itemKey + ".name", ""));
    }

    public List<String> getItemDisplayLore(String itemKey) {
        return colorList(messages.getStringList("items." + itemKey + ".lore"));
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

    public FileConfiguration getMessages() {
        return messages;
    }
}