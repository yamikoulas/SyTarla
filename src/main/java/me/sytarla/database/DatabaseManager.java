package me.sytarla.database;

import me.sytarla.SyTarla;
import me.sytarla.model.TarlaRegion;
import me.sytarla.util.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final SyTarla plugin;
    private Connection connection;
    private String dbType;

    public DatabaseManager(SyTarla plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    public void initDatabase() {
        this.dbType = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();

        try {
            if (dbType.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("database.mysql.port", 3306);
                String database = plugin.getConfig().getString("database.mysql.database", "sytarla");
                String username = plugin.getConfig().getString("database.mysql.username", "root");
                String password = plugin.getConfig().getString("database.mysql.password", "");
                boolean ssl = plugin.getConfig().getBoolean("database.mysql.use-ssl", false);

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&autoReconnect=true&characterEncoding=utf8";
                connection = DriverManager.getConnection(url, username, password);
                plugin.getLogger().info("[SyTarla] MySQL veritabanına başarıyla bağlanıldı!");
            } else {
                File dbFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.sqlite.file-name", "database.db"));
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
                plugin.getLogger().info("[SyTarla] SQLite veritabanına başarıyla bağlanıldı!");
            }

            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("[SyTarla] Veritabanı bağlantısı kurulamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        String query = "CREATE TABLE IF NOT EXISTS sytarla_regions (" +
                "name VARCHAR(64) PRIMARY KEY, " +
                "world VARCHAR(64) NOT NULL, " +
                "minX INT NOT NULL, " +
                "minY INT NOT NULL, " +
                "minZ INT NOT NULL, " +
                "maxX INT NOT NULL, " +
                "maxY INT NOT NULL, " +
                "maxZ INT NOT NULL, " +
                "enabled_crops TEXT, " +
                "custom_drop TEXT, " +
                "custom_drop_chance DOUBLE DEFAULT 100.0, " +
                "notify_growth BOOLEAN DEFAULT 1, " +
                "holo_world VARCHAR(64), " +
                "holo_x DOUBLE, " +
                "holo_y DOUBLE, " +
                "holo_z DOUBLE, " +
                "holo_yaw FLOAT, " +
                "holo_pitch FLOAT, " +
                "holo_text_shadow BOOLEAN DEFAULT 1, " +
                "holo_transparent_bg BOOLEAN DEFAULT 1, " +
                "holo_billboard VARCHAR(32) DEFAULT 'CENTER'" +
                ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            plugin.getLogger().severe("[SyTarla] Tablo oluşturulurken hata: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initDatabase();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public Map<String, TarlaRegion> loadAllRegions() {
        Map<String, TarlaRegion> regions = new HashMap<>();
        String sql = "SELECT * FROM sytarla_regions";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String world = rs.getString("world");
                int minX = rs.getInt("minX");
                int minY = rs.getInt("minY");
                int minZ = rs.getInt("minZ");
                int maxX = rs.getInt("maxX");
                int maxY = rs.getInt("maxY");
                int maxZ = rs.getInt("maxZ");

                String cropsStr = rs.getString("enabled_crops");
                Set<Material> enabledCrops = new HashSet<>();
                if (cropsStr != null && !cropsStr.isEmpty()) {
                    for (String c : cropsStr.split(",")) {
                        try {
                            enabledCrops.add(Material.valueOf(c.trim()));
                        } catch (Exception ignored) {}
                    }
                }

                String customDropBase64 = rs.getString("custom_drop");
                ItemStack customDrop = ItemSerializer.fromBase64(customDropBase64);
                double dropChance = rs.getDouble("custom_drop_chance");
                boolean notifyGrowth = rs.getBoolean("notify_growth");

                String holoWorld = rs.getString("holo_world");
                Location holoLoc = null;
                if (holoWorld != null && !holoWorld.isEmpty() && Bukkit.getWorld(holoWorld) != null) {
                    double hx = rs.getDouble("holo_x");
                    double hy = rs.getDouble("holo_y");
                    double hz = rs.getDouble("holo_z");
                    float hyaw = rs.getFloat("holo_yaw");
                    float hpitch = rs.getFloat("holo_pitch");
                    holoLoc = new Location(Bukkit.getWorld(holoWorld), hx, hy, hz, hyaw, hpitch);
                }

                boolean textShadow = rs.getBoolean("holo_text_shadow");
                boolean transparentBg = rs.getBoolean("holo_transparent_bg");
                String billboardStr = rs.getString("holo_billboard");
                Display.Billboard billboard = Display.Billboard.CENTER;
                if (billboardStr != null) {
                    try {
                        billboard = Display.Billboard.valueOf(billboardStr.toUpperCase());
                    } catch (Exception ignored) {}
                }

                TarlaRegion region = new TarlaRegion(name, world, minX, minY, minZ, maxX, maxY, maxZ,
                        enabledCrops, customDrop, dropChance, notifyGrowth, holoLoc, textShadow, transparentBg, billboard);
                regions.put(name.toLowerCase(), region);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[SyTarla] Veritabanından tarlalar yüklenirken hata: " + e.getMessage());
        }

        return regions;
    }

    public void saveRegion(TarlaRegion region) {
        String sql = (dbType.equals("MYSQL"))
                ? "INSERT INTO sytarla_regions (name, world, minX, minY, minZ, maxX, maxY, maxZ, enabled_crops, custom_drop, custom_drop_chance, notify_growth, holo_world, holo_x, holo_y, holo_z, holo_yaw, holo_pitch, holo_text_shadow, holo_transparent_bg, holo_billboard) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE world=?, minX=?, minY=?, minZ=?, maxX=?, maxY=?, maxZ=?, enabled_crops=?, custom_drop=?, custom_drop_chance=?, notify_growth=?, holo_world=?, holo_x=?, holo_y=?, holo_z=?, holo_yaw=?, holo_pitch=?, holo_text_shadow=?, holo_transparent_bg=?, holo_billboard=?"
                : "INSERT OR REPLACE INTO sytarla_regions (name, world, minX, minY, minZ, maxX, maxY, maxZ, enabled_crops, custom_drop, custom_drop_chance, notify_growth, holo_world, holo_x, holo_y, holo_z, holo_yaw, holo_pitch, holo_text_shadow, holo_transparent_bg, holo_billboard) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            StringBuilder cropsSb = new StringBuilder();
            for (Material m : region.getEnabledCrops()) {
                if (cropsSb.length() > 0) cropsSb.append(",");
                cropsSb.append(m.name());
            }

            String customDropBase64 = ItemSerializer.toBase64(region.getCustomDropItem());

            Location hl = region.getHologramLocation();
            String holoWorld = hl != null && hl.getWorld() != null ? hl.getWorld().getName() : null;
            Double hx = hl != null ? hl.getX() : null;
            Double hy = hl != null ? hl.getY() : null;
            Double hz = hl != null ? hl.getZ() : null;
            Float hyaw = hl != null ? hl.getYaw() : null;
            Float hpitch = hl != null ? hl.getPitch() : null;

            ps.setString(1, region.getName());
            ps.setString(2, region.getWorldName());
            ps.setInt(3, region.getMinX());
            ps.setInt(4, region.getMinY());
            ps.setInt(5, region.getMinZ());
            ps.setInt(6, region.getMaxX());
            ps.setInt(7, region.getMaxY());
            ps.setInt(8, region.getMaxZ());
            ps.setString(9, cropsSb.toString());
            ps.setString(10, customDropBase64);
            ps.setDouble(11, region.getCustomDropChance());
            ps.setBoolean(12, region.isNotifyGrowth());
            ps.setString(13, holoWorld);
            if (hx != null) ps.setDouble(14, hx); else ps.setNull(14, Types.DOUBLE);
            if (hy != null) ps.setDouble(15, hy); else ps.setNull(15, Types.DOUBLE);
            if (hz != null) ps.setDouble(16, hz); else ps.setNull(16, Types.DOUBLE);
            if (hyaw != null) ps.setFloat(17, hyaw); else ps.setNull(17, Types.FLOAT);
            if (hpitch != null) ps.setFloat(18, hpitch); else ps.setNull(18, Types.FLOAT);
            ps.setBoolean(19, region.isHologramTextShadow());
            ps.setBoolean(20, region.isHologramTransparentBg());
            ps.setString(21, region.getHologramBillboard().name());

            if (dbType.equals("MYSQL")) {
                ps.setString(22, region.getWorldName());
                ps.setInt(23, region.getMinX());
                ps.setInt(24, region.getMinY());
                ps.setInt(25, region.getMinZ());
                ps.setInt(26, region.getMaxX());
                ps.setInt(27, region.getMaxY());
                ps.setInt(28, region.getMaxZ());
                ps.setString(29, cropsSb.toString());
                ps.setString(30, customDropBase64);
                ps.setDouble(31, region.getCustomDropChance());
                ps.setBoolean(32, region.isNotifyGrowth());
                ps.setString(33, holoWorld);
                if (hx != null) ps.setDouble(34, hx); else ps.setNull(34, Types.DOUBLE);
                if (hy != null) ps.setDouble(35, hy); else ps.setNull(35, Types.DOUBLE);
                if (hz != null) ps.setDouble(36, hz); else ps.setNull(36, Types.DOUBLE);
                if (hyaw != null) ps.setFloat(37, hyaw); else ps.setNull(37, Types.FLOAT);
                if (hpitch != null) ps.setFloat(38, hpitch); else ps.setNull(38, Types.FLOAT);
                ps.setBoolean(39, region.isHologramTextShadow());
                ps.setBoolean(40, region.isHologramTransparentBg());
                ps.setString(41, region.getHologramBillboard().name());
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[SyTarla] Tarla kaydedilirken veritabanı hatası: " + e.getMessage());
        }
    }

    public void deleteRegion(String name) {
        String sql = "DELETE FROM sytarla_regions WHERE name = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[SyTarla] Tarla silinirken veritabanı hatası: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
