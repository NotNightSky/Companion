package org.notnightsky.companion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.notnightsky.companion.CompanionClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IdentifierArray {

    public static List<Identifier> toTextureArrayList(Textures.TextureSet set) {
        List<Identifier> identifiers = new ArrayList<>();
        Path baseDir = set.jsonPath().getParent();
        String dir = Textures.relativePath(baseDir);
        String prefix = dir.isEmpty() ? "" : dir + "/";
        if (!set.json().has("textures") || !set.json().get("textures").isJsonArray()) {
            CompanionClient.LOGGER.warn("'textures' is missing or not an array in {}, loading nothing.", set.jsonPath());
            return identifiers;
        }
        JsonArray filesArray = set.json().getAsJsonArray("textures");

        for (JsonElement element : filesArray) {
            if (!element.isJsonPrimitive()) {
                CompanionClient.LOGGER.warn("Ignoring non-string texture entry '{}' in {}.", element, set.jsonPath());
                continue;
            }
            String rawName = element.getAsString();
            String cleanName = rawName.replaceFirst("\\.(png|gif)$", "");
            Path targetPath = Textures.resolveTexturePath(baseDir, rawName, cleanName);

            if (targetPath == null) {
                CompanionClient.LOGGER.warn("Texture file not found for '{}' in {}", rawName, baseDir);
                continue;
            }

            String fileNameLower = targetPath.getFileName().toString().toLowerCase();
            if (fileNameLower.endsWith(".gif")) {
                identifiers.addAll(loadGifTextures(prefix + cleanName, targetPath));
            } else {
                Identifier id = loadTexture(prefix + cleanName, targetPath);
                if (id != null) {
                    identifiers.add(id);
                }
            }
        }

        return identifiers;
    }

    private static Identifier loadTexture(String name, Path png) {
        try (InputStream stream = new FileInputStream(png.toFile())) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(() -> "companion/" + name, image);
            Identifier id = CompanionClient.id(name);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return id;
        } catch (IOException | RuntimeException e) {
            CompanionClient.LOGGER.error("Failed to load PNG texture {}", png, e);
        }
        return null;
    }

    private static List<Identifier> loadGifTextures(String baseName, Path gif) {
        List<Identifier> frameIds = new ArrayList<>();
        try {
            List<Textures.GifFrame> frames = Textures.decodeGif(gif);
            for (int i = 0; i < frames.size(); i++) {
                String frameName = baseName + "_frame_" + i;
                DynamicTexture texture = new DynamicTexture(() -> "companion/" + frameName, frames.get(i).image());
                Identifier id = CompanionClient.id(frameName);
                Minecraft.getInstance().getTextureManager().register(id, texture);
                frameIds.add(id);
            }
        } catch (IOException | RuntimeException e) {
            CompanionClient.LOGGER.error("Failed to load GIF texture {}", gif, e);
        }
        return frameIds;
    }

    public static void unload(List<Identifier> identifiers) {
        for (Identifier id : identifiers) {
            Minecraft.getInstance().getTextureManager().release(id);
        }
    }
}