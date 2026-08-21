package me.sytarla.gui;

import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TarlaEditGUI {

    public static void openGUI(Player player, TarlaRegion region) {
        String title = ColorUtils.colorize("&#00FFCD&lSyTarla Düzenle: &f" + region.getName());
        Inventory inv = Bukkit.createInventory(new TarlaEditHolder(region), 36, title);

        ItemStack fill = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, fill);
        }

        List<String> infoLore = Arrays.asList(
                ColorUtils.colorize("&7Dünya: &f" + region.getWorldName()),
                ColorUtils.colorize("&7Min: &f" + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ()),
                ColorUtils.colorize("&7Max: &f" + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ()),
                ColorUtils.colorize("&7Duyuru Durumu: " + (region.isNotifyGrowth() ? "&aAÇIK" : "&cKAPALI")),
                ColorUtils.colorize("&7Özel Düşme Şansı: &#FFFF55%" + region.getCustomDropChance())
        );
        inv.setItem(4, createItem(Material.BOOK, ColorUtils.colorize("&#00FFCD&lTarla Bilgisi: &f" + region.getName()), infoLore));

        addCropToggle(inv, 10, Material.WHEAT, "Buğday", region);
        addCropToggle(inv, 11, Material.CARROTS, "Havuç", region);
        addCropToggle(inv, 12, Material.POTATOES, "Patates", region);
        addCropToggle(inv, 13, Material.BEETROOTS, "Pancar", region);
        addCropToggle(inv, 14, Material.NETHER_WART, "Nether Wart", region);
        addCropToggle(inv, 15, Material.SUGAR_CANE, "Şeker Kamışı", region);
        addCropToggle(inv, 16, Material.PUMPKIN, "Balkabağı", region);
        addCropToggle(inv, 19, Material.MELON, "Karpuz", region);
        addCropToggle(inv, 20, Material.COCOA, "Kakao", region);

        Material msgMat = region.isNotifyGrowth() ? Material.LIME_DYE : Material.GRAY_DYE;
        String msgTitle = region.isNotifyGrowth() ? ColorUtils.colorize("&aBüyüme Duyurusu: AÇIK") : ColorUtils.colorize("&cBüyüme Duyurusu: KAPALI");
        List<String> msgLore = Arrays.asList(
                ColorUtils.colorize("&7Tıklayarak büyüme duyurularını"),
                ColorUtils.colorize("&7açıp kapatabilirsiniz.")
        );
        inv.setItem(22, createItem(msgMat, msgTitle, msgLore));

        ItemStack customDrop = region.getCustomDropItem();
        if (customDrop != null) {
            ItemMeta meta = customDrop.getItemMeta();
            String name = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : customDrop.getType().name();
            List<String> dropLore = Arrays.asList(
                    ColorUtils.colorize("&7Mevcut Ödül: &f" + name),
                    ColorUtils.colorize("&7Şans: &#FFFF55%" + region.getCustomDropChance()),
                    ColorUtils.colorize("&7Kaldırmak için &c/sytarla itemayarla &7komutunu kullanın.")
            );
            inv.setItem(24, createItem(customDrop.getType(), ColorUtils.colorize("&#FFFF55Özel Ödül Eşyası"), dropLore));
        } else {
            List<String> dropLore = Arrays.asList(
                    ColorUtils.colorize("&7Özel ödül eşyası ayarlanmamış."),
                    ColorUtils.colorize("&7Ayarlamak için elinize eşya alıp"),
                    ColorUtils.colorize("&e/sytarla itemayarla " + region.getName() + " &7yazın.")
            );
            inv.setItem(24, createItem(Material.BARRIER, ColorUtils.colorize("&cÖzel Ödül Yok"), dropLore));
        }

        inv.setItem(31, createItem(Material.BARRIER, ColorUtils.colorize("&cMenüyü Kapat"), null));

        player.openInventory(inv);
    }

    private static void addCropToggle(Inventory inv, int slot, Material cropMaterial, String displayName, TarlaRegion region) {
        boolean enabled = region.isCropEnabled(cropMaterial);
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize("&7Durum: " + (enabled ? "&aAÇIK" : "&cKAPALI")));
        lore.add(ColorUtils.colorize("&7Tıklayarak değiştirebilirsiniz."));

        Material iconMat = enabled ? cropMaterial : Material.GRAY_DYE;
        inv.setItem(slot, createItem(iconMat, ColorUtils.colorize((enabled ? "&a" : "&c") + displayName), lore));
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
