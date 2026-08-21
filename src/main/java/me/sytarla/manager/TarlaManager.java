package me.sytarla.manager;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class TarlaManager {

    private final SyTarla plugin;
    private final Map<String, TarlaRegion> tarlalar = new HashMap<>();

    public TarlaManager(SyTarla plugin) {
        this.plugin = plugin;
        loadTarlalar();
    }

    public void loadTarlalar() {
        tarlalar.clear();

        if (plugin.getDatabaseManager() != null) {
            Map<String, TarlaRegion> dbRegions = plugin.getDatabaseManager().loadAllRegions();
            if (!dbRegions.isEmpty()) {
                tarlalar.putAll(dbRegions);
                return;
            }
        }

        File tarlalarFile = new File(plugin.getDataFolder(), "tarlalar.yml");
        if (tarlalarFile.exists()) {
            FileConfiguration tarlalarConfig = YamlConfiguration.loadConfiguration(tarlalarFile);
            ConfigurationSection section = tarlalarConfig.getConfigurationSection("tarlalar");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String name = section.getString(key + ".name", key);
                    String worldName = section.getString(key + ".world");
                    int minX = section.getInt(key + ".minX");
                    int minY = section.getInt(key + ".minY");
                    int minZ = section.getInt(key + ".minZ");
                    int maxX = section.getInt(key + ".maxX");
                    int maxY = section.getInt(key + ".maxY");
                    int maxZ = section.getInt(key + ".maxZ");

                    boolean notifyGrowth = section.getBoolean(key + ".notifyGrowth", true);
                    ItemStack customDrop = section.getItemStack(key + ".customDrop");
                    double customDropChance = section.getDouble(key + ".customDropChance", 100.0);

                    List<String> cropList = section.getStringList(key + ".enabledCrops");
                    Set<Material> enabledCrops = new HashSet<>();
                    for (String cName : cropList) {
                        try {
                            Material mat = Material.valueOf(cName);
                            enabledCrops.add(mat);
                        } catch (Exception ignored) {}
                    }

                    Location holoLoc = null;
                    if (section.contains(key + ".hologram.world")) {
                        String hWorldName = section.getString(key + ".hologram.world");
                        World hWorld = Bukkit.getWorld(hWorldName);
                        if (hWorld != null) {
                            double hX = section.getDouble(key + ".hologram.x");
                            double hY = section.getDouble(key + ".hologram.y");
                            double hZ = section.getDouble(key + ".hologram.z");
                            float hYaw = (float) section.getDouble(key + ".hologram.yaw", 0.0);
                            float hPitch = (float) section.getDouble(key + ".hologram.pitch", 0.0);
                            holoLoc = new Location(hWorld, hX, hY, hZ, hYaw, hPitch);
                        }
                    }

                    boolean holoShadow = section.getBoolean(key + ".hologram.textShadow", true);
                    boolean holoTransparent = section.getBoolean(key + ".hologram.transparentBg", true);
                    String billboardName = section.getString(key + ".hologram.billboard", "CENTER");
                    Display.Billboard holoBillboard;
                    try {
                        holoBillboard = Display.Billboard.valueOf(billboardName.toUpperCase());
                    } catch (Exception e) {
                        holoBillboard = Display.Billboard.CENTER;
                    }

                    TarlaRegion region = new TarlaRegion(name, worldName, minX, minY, minZ, maxX, maxY, maxZ,
                            enabledCrops, customDrop, customDropChance, notifyGrowth, holoLoc, holoShadow, holoTransparent, holoBillboard);
                    tarlalar.put(name.toLowerCase(), region);

                    if (plugin.getDatabaseManager() != null) {
                        plugin.getDatabaseManager().saveRegion(region);
                    }
                }
            }
        }
    }

    public void saveTarlalar() {
        if (plugin.getDatabaseManager() != null) {
            for (TarlaRegion region : tarlalar.values()) {
                plugin.getDatabaseManager().saveRegion(region);
            }
        }
    }

    public void saveSingleTarla(TarlaRegion region) {
        if (plugin.getDatabaseManager() != null) {
            plugin.getDatabaseManager().saveRegion(region);
        }
    }

    public int growAllCrops(TarlaRegion region) {
        World world = region.getWorld();
        if (world == null) return 0;

        int grownCount = 0;
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material mat = block.getType();

                    if (region.isCropEnabled(mat)) {
                        BlockData bd = block.getBlockData();
                        if (bd instanceof Ageable) {
                            Ageable ageable = (Ageable) bd;
                            if (ageable.getAge() < ageable.getMaximumAge()) {
                                ageable.setAge(ageable.getMaximumAge());
                                block.setBlockData(ageable);
                                grownCount++;
                            }
                        }
                    }
                }
            }
        }

        if (plugin.getHologramManager() != null) {
            plugin.getHologramManager().updateAllHolograms();
        }

        return grownCount;
    }

    public boolean createTarla(String name, Location loc1, Location loc2) {
        if (getTarla(name) != null) {
            return false;
        }
        TarlaRegion region = new TarlaRegion(name, loc1, loc2);
        tarlalar.put(name.toLowerCase(), region);
        saveSingleTarla(region);
        return true;
    }

    public boolean deleteTarla(String name) {
        TarlaRegion removed = tarlalar.remove(name.toLowerCase());
        if (removed != null) {
            if (plugin.getHologramManager() != null) {
                plugin.getHologramManager().removeHologram(removed);
            }
            if (plugin.getDatabaseManager() != null) {
                plugin.getDatabaseManager().deleteRegion(removed.getName());
            }
            return true;
        }
        return false;
    }

    public TarlaRegion getTarla(String name) {
        return tarlalar.get(name.toLowerCase());
    }

    public TarlaRegion getTarlaAt(Location loc) {
        for (TarlaRegion region : tarlalar.values()) {
            if (region.contains(loc)) {
                return region;
            }
        }
        return null;
    }

    public Collection<TarlaRegion> getAllTarlalar() {
        return tarlalar.values();
    }
}
