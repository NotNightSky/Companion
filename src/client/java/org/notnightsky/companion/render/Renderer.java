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
        Anchor anchor = config.getAnchor();
        int x = switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> config.getOffsetX();
            case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - width - config.getOffsetX();
        };
        int y = switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> config.getOffsetY();
            case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - height - config.getOffsetY();
        };
        int color = ARGB.white((int) (config.getOpacity() * 255.0));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture(frameIndex(frameTime)), x, y, 0.0F, 0.0F, width, height, width, height, color);
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
