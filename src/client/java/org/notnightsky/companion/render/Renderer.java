package org.notnightsky.companion.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.notnightsky.companion.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final List<Identifier> textures = new ArrayList<>();

    public static void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (textures.isEmpty()) {
            return;
        }
        ConfigManager config = ConfigManager.getInstance();
        if (!config.isVisible()) {
            return;
        }
        int width = config.getWidth();
        int height = config.getHeight();
        long frameTime = config.getFrametime();
        int x = renderX(graphics.guiWidth());
        int y = renderY(graphics.guiHeight());
        int color = ARGB.white((int) (config.getOpacity() * 255.0));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture(frameIndex(frameTime)), x, y, 0.0F, 0.0F, width, height, width, height, color);
    }

    public static int renderX(int guiWidth) {
        ConfigManager config = ConfigManager.getInstance();
        return switch (config.getAnchor()) {
            case TOP_LEFT, BOTTOM_LEFT -> config.getOffsetX();
            case TOP_RIGHT, BOTTOM_RIGHT -> guiWidth - config.getWidth() - config.getOffsetX();
        };
    }

    public static int renderY(int guiHeight) {
        ConfigManager config = ConfigManager.getInstance();
        return switch (config.getAnchor()) {
            case TOP_LEFT, TOP_RIGHT -> config.getOffsetY();
            case BOTTOM_LEFT, BOTTOM_RIGHT -> guiHeight - config.getHeight() - config.getOffsetY();
        };
    }

    private static int frameIndex(long frameTimeMs) {
        return (int) (Util.getMillis() / frameTimeMs % textures.size());
    }

    private static Identifier texture(int index) {
        if (textures.isEmpty()) {
            return MissingTextureAtlasSprite.getLocation();
        }
        return textures.get(Math.floorMod(index, textures.size()));
    }

    public static void setTextures(List<Identifier> ids) {
        textures.clear();
        textures.addAll(ids);
    }
}
