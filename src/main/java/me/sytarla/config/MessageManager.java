package me.sytarla.config;

import me.sytarla.SyTarla;
import me.sytarla.util.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class MessageManager {

    private final SyTarla plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private String prefix = "";

    public MessageManager(SyTarla plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveCustomResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        prefix = ColorUtils.colorize(messagesConfig.getString("prefix", "&#00FFCD&lSY TARLA &#8250; "));
    }

    public String getRawMessage(String path) {
        return messagesConfig.getString("messages." + path, "&cMessage not found: " + path);
    }

    public String getMessage(String path) {
        String msg = getRawMessage(path);
        return prefix + ColorUtils.colorize(msg);
    }

    public String getMessage(String path, String key, String value) {
        String msg = getRawMessage(path).replace("%" + key + "%", value);
        return prefix + ColorUtils.colorize(msg);
    }

    public List<String> getMessageList(String path) {
        List<String> rawList = messagesConfig.getStringList("messages." + path);
        if (rawList.isEmpty()) {
            rawList = messagesConfig.getStringList(path);
        }
        return ColorUtils.colorize(rawList);
    }

    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendMessage(CommandSender sender, String path, String key, String value) {
        sender.sendMessage(getMessage(path, key, value));
    }

    public void sendListMessage(CommandSender sender, String path) {
        for (String line : getMessageList(path)) {
            sender.sendMessage(line);
        }
    }

    public String getPrefix() {
        return prefix;
    }
}
