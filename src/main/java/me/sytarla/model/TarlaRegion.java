package me.sytarla.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TarlaRegion {

    private String name;
    private String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    private Set<Material> enabledCrops;
    private ItemStack customDropItem;
    private double customDropChance;
    private boolean notifyGrowth;

    // Hologram fields
    private Location hologramLocation;
    private boolean hologramTextShadow;
    private boolean hologramTransparentBg;
    private Display.Billboard hologramBillboard;
    private transient UUID hologramEntityUUID;

    public TarlaRegion(String name, Location loc1, Location loc2) {
        this.name = name;
        this.worldName = loc1.getWorld().getName();

        this.minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        this.maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        this.maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        this.enabledCrops = new HashSet<>();
        this.enabledCrops.add(Material.WHEAT);
        this.enabledCrops.add(Material.CARROTS);
        this.enabledCrops.add(Material.POTATOES);
        this.enabledCrops.add(Material.BEETROOTS);
        this.enabledCrops.add(Material.NETHER_WART);
        this.enabledCrops.add(Material.SUGAR_CANE);
        this.enabledCrops.add(Material.PUMPKIN);
        this.enabledCrops.add(Material.MELON);
        this.enabledCrops.add(Material.COCOA);

        this.customDropItem = null;
        this.customDropChance = 100.0;
        this.notifyGrowth = true;

        this.hologramLocation = null;
        this.hologramTextShadow = true;
        this.hologramTransparentBg = true;
        this.hologramBillboard = Display.Billboard.CENTER;
        this.hologramEntityUUID = null;
    }

    public TarlaRegion(String name, String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                       Set<Material> enabledCrops, ItemStack customDropItem, double customDropChance, boolean notifyGrowth,
                       Location hologramLocation, boolean hologramTextShadow, boolean hologramTransparentBg,
                       Display.Billboard hologramBillboard) {
        this.name = name;
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.enabledCrops = enabledCrops != null ? enabledCrops : new HashSet<>();
        this.customDropItem = customDropItem;
        this.customDropChance = customDropChance;
        this.notifyGrowth = notifyGrowth;
        this.hologramLocation = hologramLocation;
        this.hologramTextShadow = hologramTextShadow;
        this.hologramTransparentBg = hologramTransparentBg;
        this.hologramBillboard = hologramBillboard != null ? hologramBillboard : Display.Billboard.CENTER;
        this.hologramEntityUUID = null;
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null || !loc.getWorld().getName().equals(worldName)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorldName() { return worldName; }
    public World getWorld() { return Bukkit.getWorld(worldName); }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public Set<Material> getEnabledCrops() { return enabledCrops; }
    public void setEnabledCrops(Set<Material> enabledCrops) { this.enabledCrops = enabledCrops; }
    public boolean isCropEnabled(Material material) { return enabledCrops.contains(material); }

    public void toggleCrop(Material material) {
        if (enabledCrops.contains(material)) {
            enabledCrops.remove(material);
        } else {
            enabledCrops.add(material);
        }
    }

    public ItemStack getCustomDropItem() {
        return customDropItem != null ? customDropItem.clone() : null;
    }

    public void setCustomDropItem(ItemStack customDropItem) {
        this.customDropItem = customDropItem != null ? customDropItem.clone() : null;
    }

    public double getCustomDropChance() { return customDropChance; }
    public void setCustomDropChance(double customDropChance) {
        this.customDropChance = Math.max(0.0, Math.min(100.0, customDropChance));
    }

    public boolean isNotifyGrowth() { return notifyGrowth; }
    public void setNotifyGrowth(boolean notifyGrowth) { this.notifyGrowth = notifyGrowth; }

    public Location getHologramLocation() { return hologramLocation; }
    public void setHologramLocation(Location hologramLocation) { this.hologramLocation = hologramLocation; }

    public boolean isHologramTextShadow() { return hologramTextShadow; }
    public void setHologramTextShadow(boolean hologramTextShadow) { this.hologramTextShadow = hologramTextShadow; }

    public boolean isHologramTransparentBg() { return hologramTransparentBg; }
    public void setHologramTransparentBg(boolean hologramTransparentBg) { this.hologramTransparentBg = hologramTransparentBg; }

    public Display.Billboard getHologramBillboard() {
        return hologramBillboard != null ? hologramBillboard : Display.Billboard.CENTER;
    }

    public void setHologramBillboard(Display.Billboard hologramBillboard) { this.hologramBillboard = hologramBillboard; }

    public UUID getHologramEntityUUID() { return hologramEntityUUID; }
    public void setHologramEntityUUID(UUID hologramEntityUUID) { this.hologramEntityUUID = hologramEntityUUID; }
}
