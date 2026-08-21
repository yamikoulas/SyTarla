package me.sytarla.listener;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBreakListener implements Listener {

    private final SyTarla plugin;

    public BlockBreakListener(SyTarla plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        TarlaRegion region = plugin.getTarlaManager().getTarlaAt(loc);

        if (region == null) {
            return;
        }

        Player player = event.getPlayer();

        // Admin & Bypass check
        if (player.hasPermission("sytarla.admin") || player.hasPermission("sytarla.bypass") || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Permission check for regular players: must have sytarla.use
        if (!player.hasPermission("sytarla.use")) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return;
        }

        Material mat = block.getType();
        boolean isCrop = isCropMaterial(mat);

        if (!isCrop) {
            event.setCancelled(true);
            return;
        }

        if (!region.isCropEnabled(mat)) {
            event.setCancelled(true);
            return;
        }

        BlockData blockData = block.getBlockData();

        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            boolean isFullyGrown = ageable.getAge() == ageable.getMaximumAge();

            if (isFullyGrown) {
                giveReward(player, region, block);
                sendActionBar(player, region, mat);
            }

            event.setCancelled(true);
            ageable.setAge(0);
            block.setBlockData(ageable);

            if (plugin.getConfig().getBoolean("settings.play-sound-on-break", true)) {
                String soundName = plugin.getConfig().getString("settings.break-sound", "ENTITY_ITEM_PICKUP");
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.7f, 1.2f);
                } catch (Exception ignored) {}
            }

        } else if (mat == Material.SUGAR_CANE) {
            giveReward(player, region, block);
            sendActionBar(player, region, mat);
            event.setCancelled(true);
            block.setType(Material.SUGAR_CANE);

        } else if (mat == Material.PUMPKIN || mat == Material.MELON) {
            giveReward(player, region, block);
            sendActionBar(player, region, mat);
            event.setCancelled(true);
            block.setType(Material.AIR);
        } else {
            giveReward(player, region, block);
            sendActionBar(player, region, mat);
            event.setCancelled(true);
        }

        if (plugin.getHologramManager() != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getHologramManager().updateAllHolograms());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFarmlandTrample(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("settings.prevent-farmland-trample", true)) return;

        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.FARMLAND) {
                TarlaRegion region = plugin.getTarlaManager().getTarlaAt(event.getClickedBlock().getLocation());
                if (region != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!plugin.getConfig().getBoolean("settings.prevent-farmland-trample", true)) return;

        if (event.getBlock().getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
            TarlaRegion region = plugin.getTarlaManager().getTarlaAt(event.getBlock().getLocation());
            if (region != null) {
                event.setCancelled(true);
            }
        }
    }

    private void sendActionBar(Player player, TarlaRegion region, Material cropMat) {
        boolean enabled = plugin.getConfig().getBoolean("actionbar.enabled", true);
        if (!enabled) return;

        String rawMsg = plugin.getConfig().getString("actionbar.message", "&#00FFCD+1 &#FFFF55%crop% &#00FFCDHasat Edildi!");
        String cropName = getCropFriendlyName(cropMat);
        String formatted = rawMsg.replace("%crop%", cropName).replace("%tarla%", region.getName());

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ColorUtils.colorize(formatted)));
    }

    private void giveReward(Player player, TarlaRegion region, Block block) {
        ItemStack customDrop = region.getCustomDropItem();

        if (customDrop != null) {
            double chance = region.getCustomDropChance();
            boolean success = (chance >= 100.0) || (chance > 0 && ThreadLocalRandom.current().nextDouble() * 100.0 <= chance);

            if (success) {
                boolean dropToInv = plugin.getConfig().getBoolean("settings.drop-to-inventory", true);
                if (dropToInv) {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(customDrop.clone());
                    for (ItemStack item : leftover.values()) {
                        player.getWorld().dropItemNaturally(block.getLocation(), item);
                    }
                } else {
                    player.getWorld().dropItemNaturally(block.getLocation(), customDrop.clone());
                }
            }
        } else {
            Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
            for (ItemStack drop : drops) {
                player.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    private boolean isCropMaterial(Material mat) {
        return mat == Material.WHEAT ||
               mat == Material.CARROTS ||
               mat == Material.POTATOES ||
               mat == Material.BEETROOTS ||
               mat == Material.NETHER_WART ||
               mat == Material.SUGAR_CANE ||
               mat == Material.PUMPKIN ||
               mat == Material.MELON ||
               mat == Material.COCOA ||
               mat == Material.ATTACHED_MELON_STEM ||
               mat == Material.ATTACHED_PUMPKIN_STEM;
    }

    private String getCropFriendlyName(Material mat) {
        switch (mat) {
            case WHEAT: return "Buğday";
            case CARROTS: return "Havuç";
            case POTATOES: return "Patates";
            case BEETROOTS: return "Pancar";
            case NETHER_WART: return "Nether Wart";
            case SUGAR_CANE: return "Şeker Kamışı";
            case PUMPKIN: return "Balkabağı";
            case MELON: return "Karpuz";
            case COCOA: return "Kakao";
            default: return mat.name();
        }
    }
}
