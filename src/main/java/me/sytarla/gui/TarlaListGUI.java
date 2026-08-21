package me.sytarla.gui;

import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TarlaListGUI {

    public static final String LIST_GUI_TITLE = ColorUtils.colorize("&#00FFCD&lSyTarla Listesi");

    public static void openGUI(Player player, Collection<TarlaRegion> tarlalar) {
        Inventory inv = Bukkit.createInventory(new TarlaListHolder(), 54, LIST_GUI_TITLE);

        ItemStack fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillMeta = fill.getItemMeta();
        if (fillMeta != null) {
            fillMeta.setDisplayName(" ");
            fill.setItemMeta(fillMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, fill);
        }

        int slot = 10;
        for (TarlaRegion region : tarlalar) {
            if (slot >= 44) break;
            if ((slot + 1) % 9 == 0) slot += 2;

            List<String> lore = Arrays.asList(
                    ColorUtils.colorize("&7Dünya: &f" + region.getWorldName()),
                    ColorUtils.colorize("&7Etkin Ekin Sayısı: &a" + region.getEnabledCrops().size()),
                    ColorUtils.colorize("&7Özel Düşme Şansı: &#FFFF55%" + region.getCustomDropChance()),
                    ColorUtils.colorize("&7Büyüme Duyurusu: " + (region.isNotifyGrowth() ? "&aAÇIK" : "&cKAPALI")),
                    "",
                    ColorUtils.colorize("&eDüzenlemek için tıklayın!")
            );

            ItemStack item = new ItemStack(Material.FARMLAND);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtils.colorize("&#00FFCD&l" + region.getName()));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ColorUtils.colorize("&cKapat"));
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        player.openInventory(inv);
    }
}
