package me.sytarla;

import me.sytarla.command.SyTarlaCommand;
import me.sytarla.config.MessageManager;
import me.sytarla.config.WebhookConfig;
import me.sytarla.database.DatabaseManager;
import me.sytarla.listener.BlockBreakListener;
import me.sytarla.listener.BlockGrowthListener;
import me.sytarla.listener.GUIListener;
import me.sytarla.manager.HologramManager;
import me.sytarla.manager.SelectionManager;
import me.sytarla.manager.TarlaManager;
import me.sytarla.util.ColorUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SyTarla extends JavaPlugin {

    private static SyTarla instance;
    private MessageManager messageManager;
    private WebhookConfig webhookConfig;
    private DatabaseManager databaseManager;
    private TarlaManager tarlaManager;
    private SelectionManager selectionManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;

        // Safely save config files
        saveCustomResource("config.yml", false);
        saveCustomResource("messages.yml", false);
        saveCustomResource("webhook.yml", false);

        // Initialize Managers
        messageManager = new MessageManager(this);
        webhookConfig = new WebhookConfig(this);
        databaseManager = new DatabaseManager(this);
        tarlaManager = new TarlaManager(this);
        selectionManager = new SelectionManager();
        hologramManager = new HologramManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockGrowthListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Register Commands
        SyTarlaCommand syTarlaCommand = new SyTarlaCommand(this);
        if (getCommand("sytarla") != null) {
            getCommand("sytarla").setExecutor(syTarlaCommand);
            getCommand("sytarla").setTabCompleter(syTarlaCommand);
        }

        getLogger().info(ColorUtils.colorize("&#00FFCD[SyTarla] Eklenti başarıyla aktif edildi! (1.20+)"));
    }

    public void saveCustomResource(String resourcePath, boolean replace) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File outFile = new File(getDataFolder(), resourcePath);
        if (!outFile.exists() || replace) {
            InputStream in = getResource(resourcePath);
            if (in == null) {
                in = getClass().getClassLoader().getResourceAsStream(resourcePath);
            }
            if (in == null) {
                in = getClass().getResourceAsStream("/" + resourcePath);
            }

            if (in != null) {
                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    getLogger().severe("Resource kaydedilemedi: " + resourcePath + " - " + e.getMessage());
                } finally {
                    try {
                        in.close();
                    } catch (IOException ignored) {}
                }
            } else {
                if (!outFile.exists()) {
                    try {
                        outFile.createNewFile();
                    } catch (IOException e) {
                        getLogger().severe("Varsayılan dosya oluşturulamadı: " + resourcePath);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.stopUpdateTask();
            hologramManager.removeAllHolograms();
        }
        if (tarlaManager != null) {
            tarlaManager.saveTarlalar();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("[SyTarla] Eklenti devre dışı bırakıldı.");
    }

    public void reloadAll() {
        reloadConfig();
        messageManager.loadMessages();
        if (webhookConfig != null) {
            webhookConfig.loadConfig();
        }
        if (databaseManager != null) {
            databaseManager.close();
            databaseManager.initDatabase();
        }
        tarlaManager.loadTarlalar();
        if (hologramManager != null) {
            hologramManager.updateAllHolograms();
        }
    }

    public static SyTarla getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public WebhookConfig getWebhookConfig() {
        return webhookConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TarlaManager getTarlaManager() {
        return tarlaManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
