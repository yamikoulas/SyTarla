package me.sytarla.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscordWebhook {

    private final String url;
    private String username;
    private String avatarUrl;
    private String content;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void executeAsync() {
        if (url == null || url.trim().isEmpty()) return;

        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("username", this.username);
                json.put("avatar_url", this.avatarUrl);
                json.put("content", this.content);

                if (!this.embeds.isEmpty()) {
                    List<JSONObject> embedObjects = new ArrayList<>();
                    for (EmbedObject embed : this.embeds) {
                        JSONObject jsonEmbed = new JSONObject();
                        jsonEmbed.put("title", embed.getTitle());
                        jsonEmbed.put("description", embed.getDescription());
                        jsonEmbed.put("color", embed.getColor());

                        if (embed.getFooterText() != null) {
                            JSONObject jsonFooter = new JSONObject();
                            jsonFooter.put("text", embed.getFooterText());
                            jsonFooter.put("icon_url", embed.getFooterIconUrl());
                            jsonEmbed.put("footer", jsonFooter);
                        }

                        if (embed.isTimestamp()) {
                            jsonEmbed.put("timestamp", java.time.Instant.now().toString());
                        }

                        embedObjects.add(jsonEmbed);
                    }
                    json.put("embeds", embedObjects.toArray());
                }

                URL url = new URL(this.url);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "SyTarla-DiscordBot");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                }

                connection.getInputStream().close();
                connection.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private int color;
        private String footerText;
        private String footerIconUrl;
        private boolean timestamp;

        public String getTitle() { return title; }
        public EmbedObject setTitle(String title) { this.title = title; return this; }
        public String getDescription() { return description; }
        public EmbedObject setDescription(String description) { this.description = description; return this; }
        public int getColor() { return color; }
        public EmbedObject setColor(int color) { this.color = color; return this; }
        public String getFooterText() { return footerText; }
        public String getFooterIconUrl() { return footerIconUrl; }
        public EmbedObject setFooter(String text, String iconUrl) { this.footerText = text; this.footerIconUrl = iconUrl; return this; }
        public boolean isTimestamp() { return timestamp; }
        public EmbedObject setTimestamp(boolean timestamp) { this.timestamp = timestamp; return this; }
    }

    private static class JSONObject {
        private final StringBuilder builder = new StringBuilder("{");
        private boolean first = true;

        public void put(String key, Object value) {
            if (value == null) return;
            if (!first) builder.append(",");
            first = false;

            builder.append("\"").append(escape(key)).append("\":");
            if (value instanceof String) {
                builder.append("\"").append(escape((String) value)).append("\"");
            } else if (value instanceof Integer || value instanceof Double || value instanceof Float || value instanceof Long || value instanceof Boolean) {
                builder.append(value);
            } else if (value instanceof Object[]) {
                builder.append("[");
                Object[] arr = (Object[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) builder.append(",");
                    builder.append(arr[i].toString());
                }
                builder.append("]");
            } else if (value instanceof JSONObject) {
                builder.append(value.toString());
            }
        }

        private String escape(String s) {
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }

        @Override
        public String toString() {
            return builder.toString() + "}";
        }
    }
}
