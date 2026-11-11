package org.notnightsky.companion.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.notnightsky.companion.CompanionClient;
import org.notnightsky.companion.render.Anchor;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static final int SCHEMA = 1;
    public static final long DEFAULT_FRAMETIME = 1000;
    public static final int DEFAULT_WIDTH = 32;
    public static final int DEFAULT_HEIGHT = 16;
    public static final Anchor DEFAULT_ANCHOR = Anchor.BOTTOM_RIGHT;

    private static ConfigManager instance;

    private String selected = "";
    private Long frametime;
    private Integer width;
    private Integer height;
    private Anchor anchor;
    private int offsetX;
    private int offsetY;
    private boolean visible = true;
    private double opacity = 1.0;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void load() {
        Path file = configFile();
        if (!Files.exists(file)) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(file.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("schema") && json.get("schema").getAsInt() > SCHEMA) {
                CompanionClient.LOGGER.warn("config.json was written for schema {} (supported: {}), resetting it.", json.get("schema").getAsInt(), SCHEMA);
                save();
                return;
            }
            frametime = readPositiveLong(json, "frametime");
            width = readPositiveInt(json, "width");
            height = readPositiveInt(json, "height");
            selected = json.has("selected") && json.get("selected").isJsonPrimitive() ? json.get("selected").getAsString() : "";
            if (json.has("position") && json.get("position").isJsonObject()) {
                JsonObject position = json.getAsJsonObject("position");
                if (position.has("anchor") && position.get("anchor").isJsonPrimitive()) {
                    String rawAnchor = position.get("anchor").getAsString();
                    Anchor parsed = Anchor.tryParse(rawAnchor);
                    if (parsed == null) {
                        CompanionClient.LOGGER.warn("Unknown anchor '{}' in config.json, ignoring it.", rawAnchor);
                    } else {
                        anchor = parsed;
                    }
                }
                JsonObject offset = position.has("offset") && position.get("offset").isJsonObject() ? position.getAsJsonObject("offset") : position;
                offsetX = readInt(offset, "x", 0);
                offsetY = readInt(offset, "y", 0);
            }
            visible = !json.has("visible") || json.get("visible").getAsBoolean();
            opacity = json.has("opacity") ? Math.clamp(json.get("opacity").getAsDouble(), 0.0, 1.0) : 1.0;
        } catch (IOException | IllegalStateException | JsonParseException e) {
            CompanionClient.LOGGER.warn("Failed to read config.json, resetting it.", e);
            save();
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("schema", SCHEMA);
        json.addProperty("selected", selected);
        JsonObject position = new JsonObject();
        if (anchor != null) {
            position.addProperty("anchor", anchor.name().toLowerCase());
        }
        JsonObject offset = new JsonObject();
        offset.addProperty("x", offsetX);
        offset.addProperty("y", offsetY);
        position.add("offset", offset);
        json.add("position", position);
        if (width != null) {
            json.addProperty("width", width);
        }
        if (height != null) {
            json.addProperty("height", height);
        }
        if (frametime != null) {
            json.addProperty("frametime", frametime);
        }
        json.addProperty("visible", visible);
        json.addProperty("opacity", opacity);
        try {
            Files.writeString(configFile(), new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (IOException e) {
            CompanionClient.LOGGER.error("Failed to write config.json.", e);
        }
    }

    private static Path configFile() {
        return CompanionClient.getConfigPath().resolve("config.json");
    }

    private static Integer readPositiveInt(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return Math.max(1, obj.get(key).getAsInt());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long readPositiveLong(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return Math.max(1, obj.get(key).getAsLong());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static int readInt(JsonObject obj, String key, int fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return fallback;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Long getFrametime() {
        return frametime;
    }

    public void setFrametime(Long frametime) {
        this.frametime = frametime == null ? null : Math.max(1, frametime);
    }

    public void clearFrametime() {
        this.frametime = null;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width == null ? null : Math.max(1, width);
    }

    public void clearWidth() {
        this.width = null;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height == null ? null : Math.max(1, height);
    }

    public void clearHeight() {
        this.height = null;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public void clearAnchor() {
        this.anchor = null;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = Math.clamp(opacity, 0.0, 1.0);
    }
}
