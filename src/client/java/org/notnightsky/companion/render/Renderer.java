package org.notnightsky.companion.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final Renderer INSTANCE = new Renderer();
    private final List<Identifier> textures = new ArrayList<>();
    private long frameTimeMs = 1000;
    private int sizeW;
    private int sizeH;

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
        return (int) (Util.getMillis() / INSTANCE.frameTimeMs % INSTANCE.textures.size());
    }

    public static void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!INSTANCE.textures.isEmpty()) {
            int x = graphics.guiWidth() - INSTANCE.sizeW;
            int y = graphics.guiHeight() - INSTANCE.sizeH;
            graphics.blit(texture(frameIndex()), x, y, x + INSTANCE.sizeW, y + INSTANCE.sizeH, 0.0F, 1.0F, 0.0F, 1.0F);
        }
    }

    public static void setWidth(int width) {
        INSTANCE.sizeW = width;
    }

    public static void setHeight(int height) {
        INSTANCE.sizeH = height;
    }

    public static void setFrameTime(long frameTimeMs) {
        INSTANCE.frameTimeMs = Math.max(1, frameTimeMs);
    }
}
