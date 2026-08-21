package me.sytarla.gui;

import me.sytarla.model.TarlaRegion;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TarlaEditHolder implements InventoryHolder {

    private final TarlaRegion region;

    public TarlaEditHolder(TarlaRegion region) {
        this.region = region;
    }

    public TarlaRegion getRegion() {
        return region;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
