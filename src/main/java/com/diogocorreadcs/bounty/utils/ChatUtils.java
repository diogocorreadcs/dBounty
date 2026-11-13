package com.diogocorreadcs.bounty.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class ChatUtils {

    private ChatUtils() {
    }

    public static String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                result.add(color(line));
            }
        }
        return result;
    }
}
