package me.sytarla.manager;

import me.sytarla.model.Selection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Map<UUID, Selection> selections = new HashMap<>();

    public Selection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public void setPos1(Player player, Location loc) {
        Selection sel = getSelection(player);
        sel.setPos1(loc);
    }

    public void setPos2(Player player, Location loc) {
        Selection sel = getSelection(player);
        sel.setPos2(loc);
    }

    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
    }
}
