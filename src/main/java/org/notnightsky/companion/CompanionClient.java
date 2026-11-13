package org.notnightsky.companion;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.notnightsky.companion.config.ConfigManager;
import org.notnightsky.companion.config.screen.ConfigScreen;
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

@Mod(value = CompanionClient.MODID, dist = Dist.CLIENT)
public class CompanionClient {
    public static final String MODID = "companion";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MODID);
    public static final List<TextureSet> TEXTURE_SETS = new ArrayList<>();
    private static List<Identifier> activeTextures = List.of();
    private static int activeSetIndex = -1;
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public CompanionClient(IEventBus modEventBus, ModContainer modContainer) {
        getConfigPath();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ConfigScreen(parent));
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ConfigManager config = ConfigManager.getInstance();
            config.load();
            TEXTURE_SETS.addAll(Textures.scan());
            select(config.getSelected());
        });
    }

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
        ConfigManager config = ConfigManager.getInstance();
        config.setActiveSuggestions(Textures.parseSuggestions(TEXTURE_SETS.get(index).json()));
        config.setSelected(Textures.relativePath(TEXTURE_SETS.get(index).jsonPath()));
        config.save();
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
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
