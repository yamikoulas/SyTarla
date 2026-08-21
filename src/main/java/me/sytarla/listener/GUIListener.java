package me.sytarla.listener;

import me.sytarla.SyTarla;
import me.sytarla.gui.TarlaEditGUI;
import me.sytarla.gui.TarlaEditHolder;
import me.sytarla.gui.TarlaListHolder;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final SyTarla plugin;

    public GUIListener(SyTarla plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        // Handle TarlaEditGUI
        if (inv.getHolder() instanceof TarlaEditHolder) {
            event.setCancelled(true);
            TarlaEditHolder holder = (TarlaEditHolder) inv.getHolder();
            TarlaRegion region = holder.getRegion();

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getRawSlot();

            // Close button
            if (slot == 31 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            // Notify Growth toggle
            if (slot == 22) {
                region.setNotifyGrowth(!region.isNotifyGrowth());
                plugin.getTarlaManager().saveSingleTarla(region);
                TarlaEditGUI.openGUI(player, region);
                return;
            }

            // Crop toggles
            Material cropMat = getCropMaterialForSlot(slot);
            if (cropMat != null) {
                region.toggleCrop(cropMat);
                plugin.getTarlaManager().saveSingleTarla(region);
                TarlaEditGUI.openGUI(player, region);
            }
        }

        // Handle TarlaListGUI
        if (inv.getHolder() instanceof TarlaListHolder) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (clicked.getType() == Material.FARMLAND) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String rawName = meta.getDisplayName();
                    String strippedName = org.bukkit.ChatColor.stripColor(ColorUtils.colorize(rawName));
                    TarlaRegion region = plugin.getTarlaManager().getTarla(strippedName);
                    if (region != null) {
                        TarlaEditGUI.openGUI(player, region);
                    }
                }
            }
        }
    }

    private Material getCropMaterialForSlot(int slot) {
        switch (slot) {
            case 10: return Material.WHEAT;
            case 11: return Material.CARROTS;
            case 12: return Material.POTATOES;
            case 13: return Material.BEETROOTS;
            case 14: return Material.NETHER_WART;
            case 15: return Material.SUGAR_CANE;
            case 16: return Material.PUMPKIN;
            case 19: return Material.MELON;
            case 20: return Material.COCOA;
            default: return null;
        }
    }
}
