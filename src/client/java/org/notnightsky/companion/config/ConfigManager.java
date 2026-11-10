package org.notnightsky.companion.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.notnightsky.companion.CompanionClient;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final int SCHEMA = 1;
    private static final long DEFAULT_FRAMETIME = 1000;
    private static final int DEFAULT_WIDTH = 32;
    private static final int DEFAULT_HEIGHT = 16;

    private static ConfigManager instance;

    private String selected = "";
    private long frametime = DEFAULT_FRAMETIME;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;

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
            frametime = json.has("frametime") ? Math.max(1, json.get("frametime").getAsLong()) : DEFAULT_FRAMETIME;
            width = json.has("width") ? Math.max(1, json.get("width").getAsInt()) : DEFAULT_WIDTH;
            height = json.has("height") ? Math.max(1, json.get("height").getAsInt()) : DEFAULT_HEIGHT;
            selected = json.has("selected") && json.get("selected").isJsonPrimitive() ? json.get("selected").getAsString() : "";
        } catch (IOException | IllegalStateException | JsonParseException e) {
            CompanionClient.LOGGER.warn("Failed to read config.json, resetting it.", e);
            save();
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("schema", SCHEMA);
        json.addProperty("selected", selected);
        json.addProperty("frametime", frametime);
        json.addProperty("width", width);
        json.addProperty("height", height);
        try {
            Files.writeString(configFile(), new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (IOException e) {
            CompanionClient.LOGGER.error("Failed to write config.json.", e);
        }
    }

    private static Path configFile() {
        return CompanionClient.getConfigPath().resolve("config.json");
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
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
}
