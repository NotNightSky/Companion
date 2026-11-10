package org.notnightsky.companion.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.notnightsky.companion.render.Renderer;
import org.notnightsky.companion.util.IdentifierArray;
import org.notnightsky.companion.util.Textures;
import org.notnightsky.companion.util.Textures.TextureSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CompanionClient implements ClientModInitializer {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("companion");
    private static final List<TextureSet> TEXTURE_SETS = new ArrayList<>();
    private static List<Identifier> activeTextures = List.of();
    private static int activeSetIndex = -1;
    private static final int TEMP_COMPANION_INDEX = 1;
    public static final Logger LOGGER = LoggerFactory.getLogger("companion");

    public static Path getConfigPath() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return CONFIG_PATH;
            }
            Files.createDirectories(CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to create or access the mod configuration directory.", e);
        }
        return CONFIG_PATH;
    }

    @Override
    public void onInitializeClient() {
        getConfigPath();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            TEXTURE_SETS.addAll(Textures.scan());
            select(TEMP_COMPANION_INDEX);
        });
        Renderer.setHeight(16);
        Renderer.setWidth(32);
    }

    public static void select(int index) {
        if (index < 0 || index >= TEXTURE_SETS.size() || index == activeSetIndex) {
            return;
        }
        IdentifierArray.unload(activeTextures);
        activeTextures = IdentifierArray.toTextureArrayList(TEXTURE_SETS.get(index));
        activeSetIndex = index;
        Renderer.setTextures(activeTextures);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("companion", path);
    }
}
