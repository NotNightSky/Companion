package org.notnightsky.companion.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.notnightsky.companion.client.CompanionClient;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Textures {
    public record TextureSet(Path jsonPath, JsonObject json) {}

    public static List<TextureSet> scan() {
        try (Stream<Path> stream = Files.walk(CompanionClient.getConfigPath())) {
            return stream.filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .map(path -> new TextureSet(path, load(path)))
                    .filter(set -> set.json() != null && set.json().has("files"))
                    .toList();
        } catch (IOException e) {
            CompanionClient.LOGGER.error("Failed to scan the mod configuration folder for texture sets.", e);
        }
        return List.of();
    }

    public static JsonObject load(Path jsonPath) {
        try (FileReader reader = new FileReader(jsonPath.toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | IllegalStateException | JsonParseException e) {
            CompanionClient.LOGGER.warn("Failed to parse {}, skipping it.", jsonPath, e);
        }
        return null;
    }
}
