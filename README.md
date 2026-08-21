# 🌾 SYTarla — Advanced Skyblock Farming System

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20%2B-brightgreen)](#)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](#)
[![License](https://img.shields.io/badge/License-MIT-blue)](#)

[English](#english) | [Türkçe](#türkçe)

---

<a name="english"></a>
## 🇬🇧 English

**SYTarla** is a high-performance Minecraft farming plugin designed for Skyblock servers. It features automatic replanting, customizable reward drop rates, real-time TextDisplay holograms, and full database integration.

### 🌟 Key Features
* **Smart Crop Protection:** Crops automatically replant upon harvest. Farmland never reverts to dirt, even if players jump on it.
* **Custom Reward System:** Assign any custom item (ItemsAdder, CustomModelData, custom NBT tags) with customizable drop chances (0% - 100%).
* **Live TextDisplay Holograms:** Utilizes native Minecraft 1.20+ `TextDisplay` entities for real-time progress bars and farm status updates.
* **Database Support:** Built-in SQLite support by default, with optional MySQL and MariaDB integration.
* **Discord Webhook:** Automatically dispatches customizable embed notifications when crops reach 100% growth.
* **Full RGB Color Support:** Supports `&#RRGGBB` hex color codes across all message and hologram configurations.

### 🛠️ Requirements & Installation
* **Server Software:** Paper / Spigot 1.20 or higher
* **Java Version:** Java 17+
* **Dependencies:** None (Fully standalone)

1. Download the compiled `.jar` file.
2. Place it into your server's `plugins` folder.
3. Restart or reload your server.

---

<a name="türkçe"></a>
## 🇹🇷 Türkçe

**SYTarla**, Skyblock sunucuları için özel olarak geliştirilmiş; otomatik ekim, özelleştirilebilir ödül düşme oranları, gelişmiş canlı hologramlar ve veritabanı entegrasyonu sunan performans odaklı bir Minecraft çiftçilik eklentisidir.

### 🌟 Öne Çıkan Özellikler
* **Akıllı Tarla Koruması:** Tarladaki ekinler kırıldığında otomatik olarak yeniden ekilir. Toprağa zıplansa dahi toprak asla bozulmaz (`dirt` olmaz).
* **Gelişmiş Ödül Sistemi:** Ekin kırıldığında ItemsAdder, CustomModelData veya özel NBT etiketli eşyaların düşme şansı (%0 - %100) ayarlanabilir.
* **Canlı TextDisplay Hologramlar:** Minecraft 1.20+ `TextDisplay` varlıkları ile tarla durumu, büyüme yüzdesi ve ilerleme çubuğu anlık olarak güncellenir.
* **Veritabanı Desteği:** SQLite (dahili) veya MySQL / MariaDB altyapısı.
* **Discord Webhook Entegrasyonu:** Tarladaki ekinler %100 olgunlaştığında belirlenen Discord kanalına renkli Embed bildirimi gönderir.
* **Full RGB Desteği:** Mesajlarda ve hologramlarda `&#RRGGBB` hex renk kodları kullanılabilir.

### 🛠️ Kurulum & Gereksinimler
* **Sunucu Çekirdeği:** Paper / Spigot 1.20 veya üzeri
* **Java Sürümü:** Java 17+
* **Bağımlılık:** Bulunmuyor (Tamamen bağımsız çalışır)

1. Projenin derlenmiş `.jar` dosyasını indirin.
2. Sunucunuzun `plugins` klasörüne atın.
3. Sunucuyu yeniden başlatın veya `/reload` atın.

---

## 📜 Commands & Permissions / Komutlar ve Yetkiler

| Command / Komut | Description / Açıklama |
| :--- | :--- |
| `/sytarla pos1 / pos2` | Selects farm corner boundaries / Tarla alanının köşelerini seçer. |
| `/sytarla create <name>` | Creates a new farm area / Belirtilen alanda yeni bir tarla oluşturur. |
| `/sytarla delete <name>` | Deletes an existing farm / Mevcut tarlayı siler. |
| `/sytarla list` | Displays all farms in a GUI menu / Tüm tarlaları GUI menüsünde gösterir. |
| `/sytarla edit [name]` | Opens the farm editor GUI / Tarla düzenleme menüsünü açar. |
| `/sytarla grow [name]` | Instantly fully grows all crops / Tarladaki ekinleri anında büyütür. |
| `/sytarla setitem [name]` | Sets held item as drop reward / Eldeki eşyayı tarlanın özel ödülü yapar. |
| `/sytarla setchance <0-100>` | Adjusts custom reward drop chance / Özel ödülün düşme şansını ayarlar. |
| `/sytarla hologram create` | Creates a live TextDisplay hologram / Tarlaya canlı hologram ekler. |
| `/sytarla reload` | Reloads all configuration files / Bütün konfigürasyon dosyalarını yeniler. |

### Permissions / Yetkiler
* `sytarla.use` — Grants access to interact with farms and harvest crops / Oyuncuların tarlayı kullanmasına izin verir.
* `sytarla.admin` — Grants full administrative access / Tüm yönetimsel komutlara erişim sağlar.

---

## 📄 License / Lisans

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details. / Bu proje **MIT License** altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakabilirsiniz.
