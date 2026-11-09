package org.notnightsky.companion.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class renderer {
    private static final renderer INSTANCE = new renderer();
    private static final long FRAME_TIME_MS = 1000;
    private final List<Identifier> textures = new ArrayList<>();

    public static void setTextures(List<Identifier> textures) {
        INSTANCE.textures.clear();
        INSTANCE.textures.addAll(textures);
    }

    public static Identifier texture(int index) {
        if (INSTANCE.textures.isEmpty()) {
            return MissingTextureAtlasSprite.getLocation();
        }
        return INSTANCE.textures.get(Math.floorMod(index, INSTANCE.textures.size()));
    }

    private static int frameIndex() {
        return (int) (Util.getMillis() / FRAME_TIME_MS % INSTANCE.textures.size());
    }

    public static void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!INSTANCE.textures.isEmpty()) {
            int size = 100;
            int x = graphics.guiWidth() - size;
            int y = graphics.guiHeight() - size;
            graphics.blit(texture(frameIndex()), x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
        }
    }
}
