package org.notnightsky.companion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.notnightsky.companion.config.ConfigManager;
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
    public static final List<TextureSet> TEXTURE_SETS = new ArrayList<>();
    private static List<Identifier> activeTextures = List.of();
    private static int activeSetIndex = -1;
    public static final Logger LOGGER = LoggerFactory.getLogger("companion");

    public static Path getConfigPath() {
        if (Files.exists(CONFIG_PATH)) {
            return CONFIG_PATH;
        }
        try {
            Files.createDirectories(CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to create or access the mod configuration directory.", e);
        }
        return CONFIG_PATH;
    }

    @Override
    public void onInitializeClient() {
        getConfigPath();
        ConfigManager config = ConfigManager.getInstance();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            config.load();
            Renderer.setVisibility(config.isVisible());
            Renderer.setOpacity(config.getOpacity());
            TEXTURE_SETS.addAll(Textures.scan());
            select(config.getSelected());
        });
    }

    public static int getActiveSetIndex() {
        return activeSetIndex;
    }

    public static void select(int index) {
        if (index < 0 || index >= TEXTURE_SETS.size() || index == activeSetIndex) {
            return;
        }
        IdentifierArray.unload(activeTextures);
        activeTextures = IdentifierArray.toTextureArrayList(TEXTURE_SETS.get(index));
        activeSetIndex = index;
        Renderer.setTextures(activeTextures);
        // applyResolvedValues(TEXTURE_SETS.get(index));
        ConfigManager config = ConfigManager.getInstance();
        config.setSelected(Textures.relativePath(TEXTURE_SETS.get(index).jsonPath()));
        config.save();
    }

    private static void applyResolvedValues(TextureSet set) {
        Textures.Suggestions suggestions = Textures.parseSuggestions(set.json());
        ConfigManager config = ConfigManager.getInstance();
        Renderer.setWidth(config.getWidth() != null ? config.getWidth() : suggestions.width() != null ? suggestions.width() : ConfigManager.DEFAULT_WIDTH);
        Renderer.setHeight(config.getHeight() != null ? config.getHeight() : suggestions.height() != null ? suggestions.height() : ConfigManager.DEFAULT_HEIGHT);
        Renderer.setFrameTime(config.getFrametime() != null ? config.getFrametime() : suggestions.frametime() != null ? suggestions.frametime() : ConfigManager.DEFAULT_FRAMETIME);
        Renderer.setAnchor(config.getAnchor() != null ? config.getAnchor() : suggestions.anchor() != null ? suggestions.anchor() : ConfigManager.DEFAULT_ANCHOR);
        Renderer.setOffset(config.getOffsetX(), config.getOffsetY());
        Renderer.setVisibility(config.isVisible());
        Renderer.setOpacity(config.getOpacity());
    }

    public static void select(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            select(0);
            return;
        }
        for (int i = 0; i < TEXTURE_SETS.size(); i++) {
            if (Textures.relativePath(TEXTURE_SETS.get(i).jsonPath()).equals(relativePath)) {
                select(i);
                return;
            }
        }
        LOGGER.warn("Configured texture set '{}' was not found, falling back to the first one.", relativePath);
        select(0);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("companion", path);
    }
}
