package org.notnightsky.companion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.notnightsky.companion.client.CompanionClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IdentifierArray {
    public static List<Identifier> toTextureArrayList(JsonObject obj) {
        List<Identifier> identifiers = new ArrayList<>();
        JsonArray filesArray = obj.getAsJsonArray("files");
        if (filesArray == null) {
            return identifiers;
        }

        for (JsonElement element : filesArray) {
            String fileName = element.getAsString();
            Path png = CompanionClient.getConfigPath().resolve("cat/" + fileName + ".png");
            Identifier id = loadTexture(fileName, png);
            if (id != null) {
                identifiers.add(id);
            }
        }

        return identifiers;
    }

    public static Identifier loadTexture(String name, Path png) {
        try (InputStream stream = new FileInputStream(png.toFile())) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(() -> "companion/cat/" + name, image);
            Identifier id = CompanionClient.id("cat/" + name);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return id;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed to load texture " + png);
            e.printStackTrace();
        }
        return null;
    }
}
