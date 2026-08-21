package me.sytarla.config;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.DiscordWebhook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class WebhookConfig {

    private final SyTarla plugin;
    private File webhookFile;
    private FileConfiguration webhookConfig;

    public WebhookConfig(SyTarla plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        webhookFile = new File(plugin.getDataFolder(), "webhook.yml");
        if (!webhookFile.exists()) {
            plugin.saveCustomResource("webhook.yml", false);
        }

        webhookConfig = YamlConfiguration.loadConfiguration(webhookFile);
    }

    public void sendGrowthNotification(TarlaRegion region, int totalCrops) {
        if (!webhookConfig.getBoolean("enabled", false)) {
            return;
        }

        String url = webhookConfig.getString("url", "");
        if (url.isEmpty() || url.contains("your_webhook_url_here")) {
            return;
        }

        try {
            DiscordWebhook webhook = new DiscordWebhook(url);
            webhook.setUsername(webhookConfig.getString("username", "SyTarla"));
            webhook.setAvatarUrl(webhookConfig.getString("avatar-url", ""));

            String title = webhookConfig.getString("embed.title", "🌾 Tarla Hasada Hazır!")
                    .replace("%tarla%", region.getName())
                    .replace("%dunya%", region.getWorldName())
                    .replace("%toplam_ekin%", String.valueOf(totalCrops))
                    .replace("%sans%", String.valueOf(region.getCustomDropChance()));

            String desc = webhookConfig.getString("embed.description", "**%tarla%** tarlasındaki tüm ekinler tamamen büyüdü!")
                    .replace("%tarla%", region.getName())
                    .replace("%dunya%", region.getWorldName())
                    .replace("%toplam_ekin%", String.valueOf(totalCrops))
                    .replace("%sans%", String.valueOf(region.getCustomDropChance()));

            int color = parseColor(webhookConfig.getString("embed.color", "#00FFCD"));

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle(title)
                    .setDescription(desc)
                    .setColor(color)
                    .setFooter(
                            webhookConfig.getString("embed.footer-text", "SyTarla • Skyblock"),
                            webhookConfig.getString("embed.footer-icon-url", "")
                    )
                    .setTimestamp(webhookConfig.getBoolean("embed.timestamp", true));

            webhook.addEmbed(embed);
            webhook.executeAsync();
        } catch (Exception e) {
            plugin.getLogger().warning("[SyTarla] Discord Webhook gönderilirken hata: " + e.getMessage());
        }
    }

    private int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return 0x00FFCD;
        try {
            if (colorStr.startsWith("#")) {
                return Integer.parseInt(colorStr.substring(1), 16);
            }
            return Integer.parseInt(colorStr);
        } catch (Exception e) {
            return 0x00FFCD;
        }
    }
}
