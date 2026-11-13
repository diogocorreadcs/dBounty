package com.diogocorreadcs.bounty.integrations;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.utils.NumberFormatter;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhookHook {

    private final BountyPlugin plugin;

    public DiscordWebhookHook(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        String url = plugin.getConfig().getString("integrations.discord.webhook-url", "");
        return plugin.getConfig().getBoolean("integrations.discord.enabled", false)
                && url != null && !url.isEmpty() && !url.contains("SEU_WEBHOOK");
    }

    public void sendBountyPlaced(String sender, String target, double amount, double total) {
        if (!isEnabled() || !plugin.getConfig().getBoolean("integrations.discord.notify-on-place", true)) {
            return;
        }

        String title = plugin.getConfig().getString("integrations.discord.place-embed.title", "Nova Recompensa!");
        String description = plugin.getConfig().getString("integrations.discord.place-embed.description",
                        "**{sender}** colocou **{amount}** pela cabeca de **{target}**!\nTotal acumulado: **{total}**")
                .replace("{sender}", sender)
                .replace("{target}", target)
                .replace("{amount}", NumberFormatter.format(amount))
                .replace("{total}", NumberFormatter.format(total));
        int color = plugin.getConfig().getInt("integrations.discord.place-embed.color", 16753920);
        String avatarUrl = "https://cravatar.eu" + target + "/64.png";

        sendEmbedAsync(title, description, color, avatarUrl);
    }

    public void sendBountyClaimed(String killer, String target, double reward) {
        if (!isEnabled() || !plugin.getConfig().getBoolean("integrations.discord.notify-on-claim", true)) {
            return;
        }

        String title = plugin.getConfig().getString("integrations.discord.claim-embed.title", "Recompensa Resgatada!");
        String description = plugin.getConfig().getString("integrations.discord.claim-embed.description",
                        "**{killer}** executou **{target}** e resgatou **{amount}**!")
                .replace("{killer}", killer)
                .replace("{target}", target)
                .replace("{amount}", NumberFormatter.format(reward));
        int color = plugin.getConfig().getInt("integrations.discord.claim-embed.color", 5763719);
        String avatarUrl = "https://cravatar.eu" + killer + "/64.png";

        sendEmbedAsync(title, description, color, avatarUrl);
    }

    private void sendEmbedAsync(String title, String description, int color, String thumbnailUrl) {
        String webhookUrl = plugin.getConfig().getString("integrations.discord.webhook-url", "");
        String botName = plugin.getConfig().getString("integrations.discord.bot-name", "dBounty");
        String botAvatar = plugin.getConfig().getString("integrations.discord.bot-avatar", "");

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("User-Agent", "dBounty-Plugin");
                connection.setDoOutput(true);

                String json = "{"
                        + "\"username\":\"" + escapeJson(botName) + "\","
                        + "\"avatar_url\":\"" + escapeJson(botAvatar) + "\","
                        + "\"embeds\":[{"
                        + "\"title\":\"" + escapeJson(title) + "\","
                        + "\"description\":\"" + escapeJson(description) + "\","
                        + "\"color\":" + color + ","
                        + "\"thumbnail\":{\"url\":\"" + escapeJson(thumbnailUrl) + "\"}"
                        + "}]"
                        + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception ignored) {
            }
        });
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}