package org.notnightsky.companion.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.notnightsky.companion.render.renderer;
import org.notnightsky.companion.util.IdentifierArray;
import org.notnightsky.companion.util.Textures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CompanionClient implements ClientModInitializer {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("companion");

    public static Path getConfigPath() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return CONFIG_PATH;
            }
            Files.createDirectories(CONFIG_PATH);
        } catch (IOException e) {
            System.err.println("Failed to create or access the mod configuration directory.");
            e.printStackTrace();
        }
        return CONFIG_PATH;
    }

    @Override
    public void onInitializeClient() {
        getConfigPath();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            List<Identifier> textures = IdentifierArray.toTextureArrayList(Textures.load());
            renderer.setTextures(textures);
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("companion", path);
    }
}
