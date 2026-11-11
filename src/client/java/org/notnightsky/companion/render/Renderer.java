package org.notnightsky.companion.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final Renderer INSTANCE = new Renderer();
    private final List<Identifier> textures = new ArrayList<>();
    private long frameTimeMs = 1000;
    private int sizeW;
    private int sizeH;
    private Anchor anchor = Anchor.BOTTOM_RIGHT;
    private int offsetX;
    private int offsetY;
    private boolean visible = true;
    private double opacity = 1.0;

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
        if (INSTANCE.textures.isEmpty() || !INSTANCE.visible) {
            return;
        }
        int x = switch (INSTANCE.anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> INSTANCE.offsetX;
            case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - INSTANCE.sizeW - INSTANCE.offsetX;
        };
        int y = switch (INSTANCE.anchor) {
            case TOP_LEFT, TOP_RIGHT -> INSTANCE.offsetY;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - INSTANCE.sizeH - INSTANCE.offsetY;
        };
        int color = ARGB.white((int) (INSTANCE.opacity * 255.0));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture(frameIndex()), x, y, 0.0F, 0.0F, INSTANCE.sizeW, INSTANCE.sizeH, INSTANCE.sizeW, INSTANCE.sizeH, color);
    }

    public static void setWidth(int width) {
        INSTANCE.sizeW = Math.max(1, width);
    }

    public static void setHeight(int height) {
        INSTANCE.sizeH = Math.max(1, height);
    }

    public static void setFrameTime(long frameTimeMs) {
        INSTANCE.frameTimeMs = Math.max(1, frameTimeMs);
    }

    public static void setAnchor(Anchor anchor) {
        if (anchor != null) {
            INSTANCE.anchor = anchor;
        }
    }

    public static void setOffset(int x, int y) {
        INSTANCE.offsetX = x;
        INSTANCE.offsetY = y;
    }

    public static void setVisibility(boolean visible) {
        INSTANCE.visible = visible;
    }

    public static void setOpacity(double opacity) {
        INSTANCE.opacity = Math.clamp(opacity, 0.0, 1.0);
    }
}
