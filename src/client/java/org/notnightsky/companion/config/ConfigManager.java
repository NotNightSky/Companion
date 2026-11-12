package org.notnightsky.companion.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.notnightsky.companion.CompanionClient;
import org.notnightsky.companion.render.Anchor;
import org.notnightsky.companion.util.JsonUtils;
import org.notnightsky.companion.util.Textures;

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
    private long frametime = DEFAULT_FRAMETIME;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private Anchor anchor = DEFAULT_ANCHOR;
    private Textures.Suggestions activeSuggestions = Textures.Suggestions.EMPTY;
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
            Integer schema = JsonUtils.readInt(json, "schema");
            if (schema != null && schema > SCHEMA) {
                CompanionClient.LOGGER.warn("config.json was written for a newer schema (supported: {}), resetting it.", SCHEMA);
                save();
                return;
            }
            Long parsedFrametime = JsonUtils.readPositiveLong(json, "frametime");
            frametime = parsedFrametime != null ? parsedFrametime : DEFAULT_FRAMETIME;
            Integer parsedWidth = JsonUtils.readPositiveInt(json, "width");
            width = parsedWidth != null ? parsedWidth : DEFAULT_WIDTH;
            Integer parsedHeight = JsonUtils.readPositiveInt(json, "height");
            height = parsedHeight != null ? parsedHeight : DEFAULT_HEIGHT;
            String parsedSelected = JsonUtils.readString(json, "selected");
            selected = parsedSelected != null ? parsedSelected : "";
            if (json.has("position") && json.get("position").isJsonObject()) {
                JsonObject position = json.getAsJsonObject("position");
                Anchor parsedAnchor = JsonUtils.readAnchor(position, "anchor");
                anchor = parsedAnchor != null ? parsedAnchor : DEFAULT_ANCHOR;
                JsonObject offset = position.has("offset") && position.get("offset").isJsonObject() ? position.getAsJsonObject("offset") : position;
                Integer parsedOffsetX = JsonUtils.readInt(offset, "x");
                offsetX = parsedOffsetX != null ? parsedOffsetX : 0;
                Integer parsedOffsetY = JsonUtils.readInt(offset, "y");
                offsetY = parsedOffsetY != null ? parsedOffsetY : 0;
            } else {
                anchor = DEFAULT_ANCHOR;
                offsetX = 0;
                offsetY = 0;
            }
            Boolean parsedVisible = JsonUtils.readBoolean(json, "visible");
            visible = parsedVisible == null || parsedVisible;
            Double parsedOpacity = JsonUtils.readDouble(json, "opacity");
            opacity = Math.clamp(parsedOpacity != null ? parsedOpacity : 1.0, 0.0, 1.0);
        } catch (IOException | RuntimeException e) {
            CompanionClient.LOGGER.warn("Failed to read config.json, resetting it.", e);
            resetToDefaults();
            save();
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("schema", SCHEMA);
        json.addProperty("selected", selected);
        JsonObject position = new JsonObject();
        position.addProperty("anchor", anchor.name().toLowerCase());
        JsonObject offset = new JsonObject();
        offset.addProperty("x", offsetX);
        offset.addProperty("y", offsetY);
        position.add("offset", offset);
        json.add("position", position);
        json.addProperty("width", width);
        json.addProperty("height", height);
        json.addProperty("frametime", frametime);
        json.addProperty("visible", visible);
        json.addProperty("opacity", opacity);
        try {
            Files.writeString(configFile(), new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (IOException e) {
            CompanionClient.LOGGER.error("Failed to write config.json.", e);
        }
    }

    private void resetToDefaults() {
        selected = "";
        frametime = DEFAULT_FRAMETIME;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        anchor = DEFAULT_ANCHOR;
        offsetX = 0;
        offsetY = 0;
        visible = true;
        opacity = 1.0;
    }

    private static Path configFile() {
        return CompanionClient.getConfigPath().resolve("config.json");
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected == null ? "" : selected;
    }

    public long getFrametime() {
        return frametime;
    }

    public void setFrametime(long frametime) {
        this.frametime = Math.max(1, frametime);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(1, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(1, height);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public void setActiveSuggestions(Textures.Suggestions suggestions) {
        this.activeSuggestions = suggestions == null ? Textures.Suggestions.EMPTY : suggestions;
    }

    public Integer getSuggestedWidth() {
        return activeSuggestions.width();
    }

    public Integer getSuggestedHeight() {
        return activeSuggestions.height();
    }

    public Long getSuggestedFrametime() {
        return activeSuggestions.frametime();
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
