package org.notnightsky.companion.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.notnightsky.companion.client.CompanionClient;

import java.io.FileReader;
import java.io.IOException;

public class Textures {
    public static JsonObject load() {
        try (FileReader reader = new FileReader(CompanionClient.getConfigPath().resolve("cat/cat.json").toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            System.err.println("Failed to read cat.json, no textures will be loaded!");
            e.printStackTrace();
        }
        return new JsonObject();
    }
}
