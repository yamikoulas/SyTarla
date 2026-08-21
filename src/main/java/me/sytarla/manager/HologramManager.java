package me.sytarla.manager;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public class HologramManager {

    private final SyTarla plugin;
    private final NamespacedKey HOLO_KEY;
    private BukkitTask updateTask;

    public HologramManager(SyTarla plugin) {
        this.plugin = plugin;
        this.HOLO_KEY = new NamespacedKey(plugin, "sytarla_hologram");
        startUpdateTask();
    }

    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllHolograms, 20L, 20L);
    }

    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public TextDisplay getOrCreateHologram(TarlaRegion region) {
        Location loc = region.getHologramLocation();
        if (loc == null || loc.getWorld() == null) {
            return null;
        }

        UUID uuid = region.getHologramEntityUUID();
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof TextDisplay && entity.isValid()) {
                TextDisplay td = (TextDisplay) entity;
                td.setBillboard(region.getHologramBillboard());
                return td;
            }
        }

        for (Entity e : loc.getWorld().getNearbyEntities(loc, 3.0, 3.0, 3.0)) {
            if (e instanceof TextDisplay) {
                String tarlaTag = e.getPersistentDataContainer().get(HOLO_KEY, PersistentDataType.STRING);
                if (region.getName().equalsIgnoreCase(tarlaTag)) {
                    region.setHologramEntityUUID(e.getUniqueId());
                    TextDisplay td = (TextDisplay) e;
                    td.setBillboard(region.getHologramBillboard());
                    return td;
                }
            }
        }

        try {
            TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, textDisplay -> {
                textDisplay.setBillboard(region.getHologramBillboard());
                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                textDisplay.setShadowed(region.isHologramTextShadow());
                if (region.isHologramTransparentBg()) {
                    textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                } else {
                    textDisplay.setBackgroundColor(Color.fromARGB(0x40, 0, 0, 0));
                }
                textDisplay.getPersistentDataContainer().set(HOLO_KEY, PersistentDataType.STRING, region.getName().toLowerCase());
                textDisplay.setText(ColorUtils.colorize("&#00FFCD&l" + region.getName() + " Yükleniyor..."));
            });

            region.setHologramEntityUUID(display.getUniqueId());
            return display;
        } catch (Exception e) {
            plugin.getLogger().warning("Hologram oluşturulurken hata oluştu: " + e.getMessage());
            return null;
        }
    }

    public void removeHologram(TarlaRegion region) {
        UUID uuid = region.getHologramEntityUUID();
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }

        Location loc = region.getHologramLocation();
        if (loc != null && loc.getWorld() != null) {
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 4.0, 4.0, 4.0)) {
                if (e instanceof TextDisplay) {
                    String tag = e.getPersistentDataContainer().get(HOLO_KEY, PersistentDataType.STRING);
                    if (region.getName().equalsIgnoreCase(tag)) {
                        e.remove();
                    }
                }
            }
        }

        region.setHologramEntityUUID(null);
    }

    public void updateAllHolograms() {
        for (TarlaRegion region : plugin.getTarlaManager().getAllTarlalar()) {
            if (region.getHologramLocation() == null) continue;

            TextDisplay display = getOrCreateHologram(region);
            if (display == null || !display.isValid()) continue;

            display.setBillboard(region.getHologramBillboard());
            display.setShadowed(region.isHologramTextShadow());
            if (region.isHologramTransparentBg()) {
                display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            } else {
                display.setBackgroundColor(Color.fromARGB(0x40, 0, 0, 0));
            }

            Location loc = region.getHologramLocation();
            if (loc != null && (display.getLocation().getYaw() != loc.getYaw() || display.getLocation().getPitch() != loc.getPitch())) {
                display.teleport(loc);
            }

            CropStats stats = calculateCropStats(region);
            String formattedText = buildHologramText(region, stats);
            display.setText(formattedText);
        }
    }

    public void rotateHologramYaw(TarlaRegion region, float angle, boolean relative) {
        Location loc = region.getHologramLocation();
        if (loc == null) return;

        float newYaw = relative ? (loc.getYaw() + angle) : angle;
        loc.setYaw(newYaw);
        region.setHologramLocation(loc);

        TextDisplay display = getOrCreateHologram(region);
        if (display != null && display.isValid()) {
            display.teleport(loc);
        }
    }

    public void rotateHologramPitch(TarlaRegion region, float angle, boolean relative) {
        Location loc = region.getHologramLocation();
        if (loc == null) return;

        float newPitch = relative ? (loc.getPitch() + angle) : angle;
        loc.setPitch(newPitch);
        region.setHologramLocation(loc);

        TextDisplay display = getOrCreateHologram(region);
        if (display != null && display.isValid()) {
            display.teleport(loc);
        }
    }

    private CropStats calculateCropStats(TarlaRegion region) {
        World world = region.getWorld();
        if (world == null) {
            return new CropStats(0, 0, 0);
        }

        int totalCrops = 0;
        int matureCrops = 0;
        int totalAgeSum = 0;
        int maxAgeSum = 0;

        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material mat = block.getType();

                    if (region.isCropEnabled(mat)) {
                        BlockData bd = block.getBlockData();
                        if (bd instanceof Ageable) {
                            Ageable ageable = (Ageable) bd;
                            totalCrops++;
                            totalAgeSum += ageable.getAge();
                            maxAgeSum += ageable.getMaximumAge();

                            if (ageable.getAge() >= ageable.getMaximumAge()) {
                                matureCrops++;
                            }
                        } else if (mat == Material.SUGAR_CANE || mat == Material.PUMPKIN || mat == Material.MELON) {
                            totalCrops++;
                            matureCrops++;
                        }
                    }
                }
            }
        }

        int percent = 0;
        if (maxAgeSum > 0) {
            percent = (int) Math.round(((double) totalAgeSum / maxAgeSum) * 100.0);
        } else if (totalCrops > 0 && matureCrops == totalCrops) {
            percent = 100;
        }

        return new CropStats(totalCrops, matureCrops, percent);
    }

    private String buildHologramText(TarlaRegion region, CropStats stats) {
        List<String> lines = plugin.getMessageManager().getMessageList("hologram.lines");
        if (lines.isEmpty()) {
            lines = List.of(
                    "&#00FFCD&l❖ %tarla% TARLASI ❖",
                    "&7Durum: %durum%",
                    "&7Büyüme: %buyume_bar% &e(%buyume_yuzde%%)",
                    "&7Olgun Ekin: &#55FF55%olgun_ekin%&7/&#FFFFFF%toplam_ekin%"
            );
        }

        String statusReady = plugin.getMessageManager().getRawMessage("hologram.status-ready");
        if (statusReady.startsWith("&cMessage not found")) statusReady = "&#55FF55● Hasada Hazır!";
        String statusGrowing = plugin.getMessageManager().getRawMessage("hologram.status-growing");
        if (statusGrowing.startsWith("&cMessage not found")) statusGrowing = "&#FFFF55● Büyüyor...";
        String statusEmpty = plugin.getMessageManager().getRawMessage("hologram.status-empty");
        if (statusEmpty.startsWith("&cMessage not found")) statusEmpty = "&#AAAAAA● Ekin Yok";

        String durum;
        if (stats.totalCrops == 0) {
            durum = ColorUtils.colorize(statusEmpty);
        } else if (stats.matureCrops == stats.totalCrops && stats.totalCrops > 0) {
            durum = ColorUtils.colorize(statusReady);
        } else {
            durum = ColorUtils.colorize(statusGrowing);
        }

        String bar = createProgressBar(stats.percentage, 10, "&#55FF55", "&#555555", "■");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String formatted = line
                    .replace("%tarla%", region.getName())
                    .replace("%durum%", durum)
                    .replace("%buyume_yuzde%", String.valueOf(stats.percentage))
                    .replace("%buyume_bar%", bar)
                    .replace("%olgun_ekin%", String.valueOf(stats.matureCrops))
                    .replace("%toplam_ekin%", String.valueOf(stats.totalCrops));

            sb.append(ColorUtils.colorize(formatted));
            if (i < lines.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String createProgressBar(int currentPercent, int totalBars, String completedColor, String remainingColor, String symbol) {
        int completedBars = (int) Math.round((currentPercent / 100.0) * totalBars);
        if (completedBars > totalBars) completedBars = totalBars;
        if (completedBars < 0) completedBars = 0;

        StringBuilder bar = new StringBuilder();
        bar.append(ColorUtils.colorize(completedColor));
        for (int i = 0; i < completedBars; i++) {
            bar.append(symbol);
        }
        bar.append(ColorUtils.colorize(remainingColor));
        for (int i = completedBars; i < totalBars; i++) {
            bar.append(symbol);
        }
        return bar.toString();
    }

    public void removeAllHolograms() {
        for (TarlaRegion region : plugin.getTarlaManager().getAllTarlalar()) {
            removeHologram(region);
        }
    }

    private static class CropStats {
        final int totalCrops;
        final int matureCrops;
        final int percentage;

        CropStats(int totalCrops, int matureCrops, int percentage) {
            this.totalCrops = totalCrops;
            this.matureCrops = matureCrops;
            this.percentage = percentage;
        }
    }
}
