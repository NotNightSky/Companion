package org.notnightsky.companion.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.notnightsky.companion.config.ConfigManager;
import org.notnightsky.companion.config.screen.widgets.StyledButton;
import org.notnightsky.companion.render.Anchor;
import org.notnightsky.companion.render.Renderer;

public class MoveScreen extends Screen {
    private static final int COLOR_GUIDE = 0x66FF6D5D;

    private final Screen parent;
    private boolean dragging;
    private int grabX;
    private int grabY;

    public MoveScreen(Screen parent) {
        super(Component.literal("companion position"));
        this.parent = parent;
    }

    private int boxX() {
        return Renderer.renderX(this.width);
    }

    private int boxY() {
        return Renderer.renderY(this.height);
    }

    @Override
    protected void init() {
        ConfigManager config = ConfigManager.getInstance();
        int boxW = config.getWidth();
        int boxH = config.getHeight();
        if (boxX() + boxW <= 0 || boxX() >= this.width || boxY() + boxH <= 0 || boxY() >= this.height) {
            setOffsetsFromPosition(config,
                Math.clamp(boxX(), 0, Math.max(0, this.width - boxW)),
                Math.clamp(boxY(), 0, Math.max(0, this.height - boxH)));
            config.save();
        }
        addRenderableWidget(new StyledButton(this.width / 2 - 60, this.height - 28, 120, 20, Component.literal("Done")) {
            @Override
            protected void press() {
                onClose();
            }
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        ConfigManager config = ConfigManager.getInstance();
        int boxX = boxX();
        int boxY = boxY();
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
            && event.x() >= boxX
            && event.x() < boxX + config.getWidth()
            && event.y() >= boxY
            && event.y() < boxY + config.getHeight()) {
            this.dragging = true;
            this.grabX = (int) Math.round(event.x() - boxX);
            this.grabY = (int) Math.round(event.y() - boxY);
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double shiftX, double shiftY) {
        if (this.dragging && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            ConfigManager config = ConfigManager.getInstance();
            int x = Math.clamp((int) Math.round(event.x() - this.grabX), 0, Math.max(0, this.width - config.getWidth()));
            int y = Math.clamp((int) Math.round(event.y() - this.grabY), 0, Math.max(0, this.height - config.getHeight()));
            setOffsetsFromPosition(config, x, y);
            return true;
        }
        return super.mouseDragged(event, shiftX, shiftY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging) {
            this.dragging = false;
            ConfigManager.getInstance().save();
            return true;
        }
        return super.mouseReleased(event);
    }

    private void setOffsetsFromPosition(ConfigManager config, int x, int y) {
        Anchor anchor = config.getAnchor();
        boolean right = anchor == Anchor.TOP_RIGHT || anchor == Anchor.BOTTOM_RIGHT;
        boolean bottom = anchor == Anchor.BOTTOM_LEFT || anchor == Anchor.BOTTOM_RIGHT;
        config.setOffsetX(right ? this.width - config.getWidth() - x : x);
        config.setOffsetY(bottom ? this.height - config.getHeight() - y : y);
    }

    @Override
    public void onClose() {
        this.dragging = false;
        ConfigManager.getInstance().save();
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ConfigManager config = ConfigManager.getInstance();
        int boxX = boxX();
        int boxY = boxY();
        int boxW = config.getWidth();
        int boxH = config.getHeight();
        if (this.dragging) {
            graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, COLOR_GUIDE);
            graphics.fill(0, this.height / 2, this.width, this.height / 2 + 1, COLOR_GUIDE);
            graphics.outline(boxX - 1, boxY - 1, boxW + 2, boxH + 2, ConfigScreen.COLOR_ACCENT_HOVER);
        }
        graphics.outline(boxX, boxY, boxW, boxH, this.dragging ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_PRIMARY_ACCENT);
        graphics.centeredText(this.font, "Drag the box to move the companion", this.width / 2, this.height - 46, ConfigScreen.COLOR_TEXT_MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}
