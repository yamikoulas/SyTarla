package me.sytarla.command;

import me.sytarla.SyTarla;
import me.sytarla.gui.TarlaEditGUI;
import me.sytarla.gui.TarlaListGUI;
import me.sytarla.model.Selection;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SyTarlaCommand implements CommandExecutor, TabCompleter {

    private final SyTarla plugin;

    public SyTarlaCommand(SyTarla plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&#FF4B4BBu komut sadece oyuncular tarafından kullanılabilir!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("sytarla.admin")) {
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getMessageManager().sendListMessage(player, "help");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "pos1":
                handlePos1(player);
                break;
            case "pos2":
                handlePos2(player);
                break;
            case "oluştur":
            case "olustur":
            case "create":
                handleCreate(player, args);
                break;
            case "sil":
            case "delete":
                handleDelete(player, args);
                break;
            case "list":
            case "liste":
                handleList(player);
                break;
            case "edit":
            case "düzenle":
            case "duzenle":
                handleEdit(player, args);
                break;
            case "mesaj":
            case "msg":
                handleMessage(player, args);
                break;
            case "itemayarla":
            case "setitem":
                handleItemSet(player, args);
                break;
            case "şansayarla":
            case "sansayarla":
            case "setchance":
                handleChanceSet(player, args);
                break;
            case "büyüt":
            case "buyut":
            case "grow":
                handleGrow(player, args);
                break;
            case "hologram":
            case "holo":
                handleHologram(player, args);
                break;
            case "yenile":
            case "reload":
                handleReload(player);
                break;
            default:
                plugin.getMessageManager().sendListMessage(player, "help");
                break;
        }

        return true;
    }

    private void handlePos1(Player player) {
        Location loc = player.getLocation();
        plugin.getSelectionManager().setPos1(player, loc);
        String msg = plugin.getMessageManager().getRawMessage("pos1-set")
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
    }

    private void handlePos2(Player player) {
        Location loc = player.getLocation();
        plugin.getSelectionManager().setPos2(player, loc);
        String msg = plugin.getMessageManager().getRawMessage("pos2-set")
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla oluştur <ad>"));
            return;
        }

        Selection sel = plugin.getSelectionManager().getSelection(player);
        if (!sel.isComplete()) {
            plugin.getMessageManager().sendMessage(player, "no-selection");
            return;
        }

        String name = args[1];
        if (plugin.getTarlaManager().getTarla(name) != null) {
            plugin.getMessageManager().sendMessage(player, "already-exists", "name", name);
            return;
        }

        plugin.getTarlaManager().createTarla(name, sel.getPos1(), sel.getPos2());
        plugin.getSelectionManager().clearSelection(player);
        plugin.getMessageManager().sendMessage(player, "created", "name", name);
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla sil <ad>"));
            return;
        }

        String name = args[1];
        if (plugin.getTarlaManager().deleteTarla(name)) {
            plugin.getMessageManager().sendMessage(player, "deleted", "name", name);
        } else {
            plugin.getMessageManager().sendMessage(player, "not-found", "name", name);
        }
    }

    private void handleList(Player player) {
        if (plugin.getTarlaManager().getAllTarlalar().isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FFFF55Henüz hiç tarla oluşturulmamış."));
            return;
        }
        TarlaListGUI.openGUI(player, plugin.getTarlaManager().getAllTarlalar());
    }

    private void handleEdit(Player player, String[] args) {
        TarlaRegion region = resolveRegion(player, args, 1);
        if (region == null) return;
        TarlaEditGUI.openGUI(player, region);
    }

    private void handleMessage(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla mesaj <aç|kapat> [ad]"));
            return;
        }

        String toggle = args[1].toLowerCase();
        boolean enable;
        if (toggle.equals("aç") || toggle.equals("ac") || toggle.equals("on")) {
            enable = true;
        } else if (toggle.equals("kapat") || toggle.equals("off")) {
            enable = false;
        } else {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla mesaj <aç|kapat> [ad]"));
            return;
        }

        TarlaRegion region = resolveRegion(player, args, 2);
        if (region == null) return;

        region.setNotifyGrowth(enable);
        plugin.getTarlaManager().saveSingleTarla(region);

        if (enable) {
            plugin.getMessageManager().sendMessage(player, "msg-toggle-on", "name", region.getName());
        } else {
            plugin.getMessageManager().sendMessage(player, "msg-toggle-off", "name", region.getName());
        }
    }

    private void handleItemSet(Player player, String[] args) {
        TarlaRegion region = resolveRegion(player, args, 1);
        if (region == null) return;

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            region.setCustomDropItem(null);
            plugin.getTarlaManager().saveSingleTarla(region);
            plugin.getMessageManager().sendMessage(player, "item-cleared");
            return;
        }

        region.setCustomDropItem(inHand.clone());
        plugin.getTarlaManager().saveSingleTarla(region);
        plugin.getMessageManager().sendMessage(player, "item-set");
    }

    private void handleChanceSet(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla şansayarla <0-100> [ad]"));
            return;
        }

        double chance;
        try {
            chance = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(player, "invalid-number");
            return;
        }

        if (chance < 0 || chance > 100) {
            plugin.getMessageManager().sendMessage(player, "invalid-chance");
            return;
        }

        TarlaRegion region = resolveRegion(player, args, 2);
        if (region == null) return;

        region.setCustomDropChance(chance);
        plugin.getTarlaManager().saveSingleTarla(region);

        String msg = plugin.getMessageManager().getRawMessage("chance-set")
                .replace("%name%", region.getName())
                .replace("%chance%", String.valueOf(chance));
        player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
    }

    private void handleGrow(Player player, String[] args) {
        TarlaRegion region = resolveRegion(player, args, 1);
        if (region == null) return;

        int count = plugin.getTarlaManager().growAllCrops(region);
        String msg = plugin.getMessageManager().getRawMessage("grown-success")
                .replace("%name%", region.getName())
                .replace("%count%", String.valueOf(count));
        player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
    }

    private void handleHologram(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageManager().sendListMessage(player, "help");
            return;
        }

        String holoSub = args[1].toLowerCase();

        switch (holoSub) {
            case "oluştur":
            case "olustur":
            case "create": {
                TarlaRegion region = resolveRegion(player, args, 2);
                if (region == null) return;

                Location loc = player.getLocation().clone();
                region.setHologramLocation(loc);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getHologramManager().getOrCreateHologram(region);
                plugin.getHologramManager().updateAllHolograms();
                plugin.getMessageManager().sendMessage(player, "hologram-created", "name", region.getName());
                break;
            }
            case "sil":
            case "delete": {
                TarlaRegion region = resolveRegion(player, args, 2);
                if (region == null) return;

                if (region.getHologramLocation() == null) {
                    plugin.getMessageManager().sendMessage(player, "hologram-not-found");
                    return;
                }

                plugin.getHologramManager().removeHologram(region);
                region.setHologramLocation(null);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getMessageManager().sendMessage(player, "hologram-deleted", "name", region.getName());
                break;
            }
            case "taşı":
            case "tasi":
            case "move": {
                TarlaRegion region = resolveRegion(player, args, 2);
                if (region == null) return;

                if (region.getHologramLocation() == null) {
                    plugin.getMessageManager().sendMessage(player, "hologram-not-found");
                    return;
                }

                plugin.getHologramManager().removeHologram(region);
                Location newLoc = player.getLocation().clone();
                region.setHologramLocation(newLoc);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getHologramManager().getOrCreateHologram(region);
                plugin.getHologramManager().updateAllHolograms();
                plugin.getMessageManager().sendMessage(player, "hologram-moved", "name", region.getName());
                break;
            }
            case "textshadow": {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla hologram textshadow <aç|kapat> [ad]"));
                    return;
                }
                boolean shadow = args[2].equalsIgnoreCase("aç") || args[2].equalsIgnoreCase("ac") || args[2].equalsIgnoreCase("on");

                TarlaRegion region = resolveRegion(player, args, 3);
                if (region == null) return;

                region.setHologramTextShadow(shadow);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getHologramManager().updateAllHolograms();

                if (shadow) {
                    plugin.getMessageManager().sendMessage(player, "hologram-shadow-on", "name", region.getName());
                } else {
                    plugin.getMessageManager().sendMessage(player, "hologram-shadow-off", "name", region.getName());
                }
                break;
            }
            case "background":
            case "bg": {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla hologram background <şeffaf|normal> [ad]"));
                    return;
                }
                boolean transparent = args[2].equalsIgnoreCase("şeffaf") || args[2].equalsIgnoreCase("seffaf") || args[2].equalsIgnoreCase("transparent");

                TarlaRegion region = resolveRegion(player, args, 3);
                if (region == null) return;

                region.setHologramTransparentBg(transparent);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getHologramManager().updateAllHolograms();

                if (transparent) {
                    plugin.getMessageManager().sendMessage(player, "hologram-bg-transparent", "name", region.getName());
                } else {
                    plugin.getMessageManager().sendMessage(player, "hologram-bg-normal", "name", region.getName());
                }
                break;
            }
            case "billboard": {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla hologram billboard <center|fixed|vertical|horizontal> [ad]"));
                    return;
                }

                Display.Billboard billboard;
                try {
                    billboard = Display.Billboard.valueOf(args[2].toUpperCase());
                } catch (Exception e) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BGeçersiz billboard modu. Geçerli: center, fixed, vertical, horizontal"));
                    return;
                }

                TarlaRegion region = resolveRegion(player, args, 3);
                if (region == null) return;

                region.setHologramBillboard(billboard);
                plugin.getTarlaManager().saveSingleTarla(region);
                plugin.getHologramManager().updateAllHolograms();

                String msg = plugin.getMessageManager().getRawMessage("hologram-billboard-set")
                        .replace("%name%", region.getName())
                        .replace("%mode%", billboard.name());
                player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
                break;
            }
            case "rotate": {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla hologram rotate <açı> [ad]"));
                    return;
                }

                float angle;
                try {
                    angle = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "invalid-number");
                    return;
                }

                TarlaRegion region = resolveRegion(player, args, 3);
                if (region == null) return;

                if (region.getHologramLocation() == null) {
                    plugin.getMessageManager().sendMessage(player, "hologram-not-found");
                    return;
                }

                plugin.getHologramManager().rotateHologramYaw(region, angle, false);
                plugin.getTarlaManager().saveSingleTarla(region);

                String msg = plugin.getMessageManager().getRawMessage("hologram-rotated-yaw")
                        .replace("%name%", region.getName())
                        .replace("%angle%", String.valueOf(angle));
                player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
                break;
            }
            case "rotatepitch": {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize("&#FF4B4BKullanım: /sytarla hologram rotatepitch <açı> [ad]"));
                    return;
                }

                float pitchAngle;
                try {
                    pitchAngle = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "invalid-number");
                    return;
                }

                TarlaRegion region = resolveRegion(player, args, 3);
                if (region == null) return;

                if (region.getHologramLocation() == null) {
                    plugin.getMessageManager().sendMessage(player, "hologram-not-found");
                    return;
                }

                plugin.getHologramManager().rotateHologramPitch(region, pitchAngle, false);
                plugin.getTarlaManager().saveSingleTarla(region);

                String msg = plugin.getMessageManager().getRawMessage("hologram-rotated-pitch")
                        .replace("%name%", region.getName())
                        .replace("%angle%", String.valueOf(pitchAngle));
                player.sendMessage(plugin.getMessageManager().getPrefix() + ColorUtils.colorize(msg));
                break;
            }
            default:
                plugin.getMessageManager().sendListMessage(player, "help");
                break;
        }
    }

    private void handleReload(Player player) {
        plugin.reloadAll();
        plugin.getMessageManager().sendMessage(player, "reloaded");
    }

    private TarlaRegion resolveRegion(Player player, String[] args, int nameArgIndex) {
        if (args.length > nameArgIndex) {
            String name = args[nameArgIndex];
            TarlaRegion region = plugin.getTarlaManager().getTarla(name);
            if (region == null) {
                plugin.getMessageManager().sendMessage(player, "not-found", "name", name);
                return null;
            }
            return region;
        }

        // Try to find the region the player is standing in
        TarlaRegion region = plugin.getTarlaManager().getTarlaAt(player.getLocation());
        if (region != null) {
            return region;
        }

        // Check default tarla from config
        String defaultTarla = plugin.getConfig().getString("settings.default-tarla", "");
        if (!defaultTarla.isEmpty()) {
            region = plugin.getTarlaManager().getTarla(defaultTarla);
            if (region != null) {
                return region;
            }
        }

        plugin.getMessageManager().sendMessage(player, "not-in-region");
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("pos1", "pos2", "oluştur", "sil", "list", "edit", "mesaj",
                    "itemayarla", "şansayarla", "büyüt", "hologram", "yenile"));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "sil":
                case "delete":
                case "edit":
                case "düzenle":
                case "duzenle":
                case "itemayarla":
                case "setitem":
                case "büyüt":
                case "buyut":
                case "grow":
                    for (TarlaRegion r : plugin.getTarlaManager().getAllTarlalar()) {
                        completions.add(r.getName());
                    }
                    break;
                case "mesaj":
                case "msg":
                    completions.addAll(Arrays.asList("aç", "kapat"));
                    break;
                case "şansayarla":
                case "sansayarla":
                case "setchance":
                    completions.addAll(Arrays.asList("10", "25", "50", "75", "100"));
                    break;
                case "hologram":
                case "holo":
                    completions.addAll(Arrays.asList("oluştur", "sil", "taşı", "textshadow", "background", "billboard", "rotate", "rotatepitch"));
                    break;
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("hologram") || sub.equals("holo")) {
                String holoSub = args[1].toLowerCase();
                switch (holoSub) {
                    case "oluştur":
                    case "olustur":
                    case "create":
                    case "sil":
                    case "delete":
                    case "taşı":
                    case "tasi":
                    case "move":
                        for (TarlaRegion r : plugin.getTarlaManager().getAllTarlalar()) {
                            completions.add(r.getName());
                        }
                        break;
                    case "textshadow":
                        completions.addAll(Arrays.asList("aç", "kapat"));
                        break;
                    case "background":
                    case "bg":
                        completions.addAll(Arrays.asList("şeffaf", "normal"));
                        break;
                    case "billboard":
                        completions.addAll(Arrays.asList("center", "fixed", "vertical", "horizontal"));
                        break;
                    case "rotate":
                    case "rotatepitch":
                        completions.addAll(Arrays.asList("0", "45", "90", "180", "270"));
                        break;
                }
            } else if (sub.equals("mesaj") || sub.equals("msg")) {
                for (TarlaRegion r : plugin.getTarlaManager().getAllTarlalar()) {
                    completions.add(r.getName());
                }
            } else if (sub.equals("şansayarla") || sub.equals("sansayarla") || sub.equals("setchance")) {
                for (TarlaRegion r : plugin.getTarlaManager().getAllTarlalar()) {
                    completions.add(r.getName());
                }
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("hologram") || sub.equals("holo")) {
                String holoSub = args[1].toLowerCase();
                if (holoSub.equals("textshadow") || holoSub.equals("background") || holoSub.equals("bg") ||
                        holoSub.equals("billboard") || holoSub.equals("rotate") || holoSub.equals("rotatepitch")) {
                    for (TarlaRegion r : plugin.getTarlaManager().getAllTarlalar()) {
                        completions.add(r.getName());
                    }
                }
            }
        }

        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());
    }
}
