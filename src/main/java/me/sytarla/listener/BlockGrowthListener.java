package me.sytarla.listener;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockGrowthListener implements Listener {

    private final SyTarla plugin;
    private final Map<String, Long> lastAnnouncementTime = new HashMap<>();

    public BlockGrowthListener(SyTarla plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Location loc = event.getBlock().getLocation();
        TarlaRegion region = plugin.getTarlaManager().getTarlaAt(loc);

        if (region == null) {
            return;
        }

        BlockState newState = event.getNewState();
        BlockData blockData = newState.getBlockData();

        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            if (ageable.getAge() == ageable.getMaximumAge()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    int totalCrops = countAllCropsAndCheckFullGrowth(region);
                    if (totalCrops > 0) {
                        long now = System.currentTimeMillis();
                        long lastTime = lastAnnouncementTime.getOrDefault(region.getName().toLowerCase(), 0L);
                        if (now - lastTime > 30000) {
                            lastAnnouncementTime.put(region.getName().toLowerCase(), now);

                            if (region.isNotifyGrowth()) {
                                sendGrowthAnnouncement(region);
                            }

                            if (plugin.getWebhookConfig() != null) {
                                plugin.getWebhookConfig().sendGrowthNotification(region, totalCrops);
                            }
                        }
                    }
                }, 2L);
            }
        }
    }

    private int countAllCropsAndCheckFullGrowth(TarlaRegion region) {
        World world = region.getWorld();
        if (world == null) return -1;

        int totalCrops = 0;

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
                            if (ageable.getAge() < ageable.getMaximumAge()) {
                                return -1;
                            }
                        } else if (mat == Material.SUGAR_CANE || mat == Material.PUMPKIN || mat == Material.MELON) {
                            totalCrops++;
                        }
                    }
                }
            }
        }

        return totalCrops;
    }

    private void sendGrowthAnnouncement(TarlaRegion region) {
        List<String> rawMessages = plugin.getMessageManager().getMessageList("growth-announcement");
        if (rawMessages.isEmpty()) return;

        for (String line : rawMessages) {
            String formatted = line.replace("%tarla%", region.getName());
            String colored = ColorUtils.colorize(formatted);

            if (region.getWorld() != null) {
                for (Player p : region.getWorld().getPlayers()) {
                    p.sendMessage(colored);
                }
            } else {
                Bukkit.broadcastMessage(colored);
            }
        }
    }
}
