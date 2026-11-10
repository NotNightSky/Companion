package org.notnightsky.companion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    public static List<Identifier> toTextureArrayList(Textures.TextureSet set) {
        List<Identifier> identifiers = new ArrayList<>();
        Path baseDir = set.jsonPath().getParent();
        String prefix = relativeDir(CompanionClient.getConfigPath(), baseDir);
        JsonArray filesArray = set.json().getAsJsonArray("files");
        if (filesArray == null) {
            return identifiers;
        }

        for (JsonElement element : filesArray) {
            String fileName = element.getAsString();
            Identifier id = loadTexture(prefix + fileName, baseDir.resolve(fileName + ".png"));
            if (id != null) {
                identifiers.add(id);
            }
        }

        return identifiers;
    }

    public static Identifier loadTexture(String name, Path png) {
        try (InputStream stream = new FileInputStream(png.toFile())) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(() -> "companion/" + name, image);
            Identifier id = CompanionClient.id(name);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return id;
        } catch (IOException | RuntimeException e) {
            CompanionClient.LOGGER.error("Failed to load texture {}", png, e);
        }
        return null;
    }

    public static void unload(List<Identifier> identifiers) {
        for (Identifier id : identifiers) {
            Minecraft.getInstance().getTextureManager().release(id);
        }
    }

    private static String relativeDir(Path configRoot, Path dir) {
        Path relative = configRoot.relativize(dir);
        if (relative.getNameCount() == 0) {
            return "";
        }
        return relative.toString().replace('\\', '/') + "/";
    }
}
