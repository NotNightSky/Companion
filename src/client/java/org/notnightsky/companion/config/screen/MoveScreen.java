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
    private static final int COLOR_HANDLE_OUTLINE = 0xCC000000;
    private static final int HANDLE_SIZE = 5;
    private static final int HANDLE_HIT_RADIUS = 6;
    private static final int MIN_SIZE = 1;

    private enum Handle {
        TOP_LEFT(0, 0),
        TOP(50, 0),
        TOP_RIGHT(100, 0),
        RIGHT(100, 50),
        BOTTOM_RIGHT(100, 100),
        BOTTOM(50, 100),
        BOTTOM_LEFT(0, 100),
        LEFT(0, 50);

        private final int xPercent;
        private final int yPercent;

        Handle(int xPercent, int yPercent) {
            this.xPercent = xPercent;
            this.yPercent = yPercent;
        }

        private int x(int boxX, int boxWidth) {
            return boxX + boxWidth * this.xPercent / 100;
        }

        private int y(int boxY, int boxHeight) {
            return boxY + boxHeight * this.yPercent / 100;
        }

        private boolean movesLeft() {
            return this.xPercent == 0;
        }

        private boolean movesRight() {
            return this.xPercent == 100;
        }

        private boolean movesTop() {
            return this.yPercent == 0;
        }

        private boolean movesBottom() {
            return this.yPercent == 100;
        }
    }

    private final Screen parent;
    private boolean dragging;
    private Handle resizing;
    private double pressX;
    private double pressY;
    private int startX;
    private int startY;
    private int startWidth;
    private int startHeight;
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

    private Handle handleAt(double mouseX, double mouseY) {
        ConfigManager config = ConfigManager.getInstance();
        int boxX = boxX();
        int boxY = boxY();
        int boxW = config.getWidth();
        int boxH = config.getHeight();
        double closest = (double) HANDLE_HIT_RADIUS * HANDLE_HIT_RADIUS;
        Handle nearest = null;
        for (Handle handle : Handle.values()) {
            double dx = mouseX - handle.x(boxX, boxW);
            double dy = mouseY - handle.y(boxY, boxH);
            double distance = dx * dx + dy * dy;
            if (distance <= closest) {
                closest = distance;
                nearest = handle;
            }
        }
        return nearest;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        ConfigManager config = ConfigManager.getInstance();
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            Handle handle = handleAt(event.x(), event.y());
            if (handle != null) {
                this.resizing = handle;
                this.pressX = event.x();
                this.pressY = event.y();
                this.startX = boxX();
                this.startY = boxY();
                this.startWidth = config.getWidth();
                this.startHeight = config.getHeight();
                return true;
            }
        }
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
        if (this.resizing != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            resize(event.x(), event.y());
            return true;
        }
        if (this.dragging && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            ConfigManager config = ConfigManager.getInstance();
            int x = Math.clamp((int) Math.round(event.x() - this.grabX), 0, Math.max(0, this.width - config.getWidth()));
            int y = Math.clamp((int) Math.round(event.y() - this.grabY), 0, Math.max(0, this.height - config.getHeight()));
            setOffsetsFromPosition(config, x, y);
            return true;
        }
        return super.mouseDragged(event, shiftX, shiftY);
    }

    private void resize(double mouseX, double mouseY) {
        ConfigManager config = ConfigManager.getInstance();
        int left = this.startX;
        int top = this.startY;
        int right = this.startX + this.startWidth;
        int bottom = this.startY + this.startHeight;
        int dx = (int) Math.round(mouseX - this.pressX);
        int dy = (int) Math.round(mouseY - this.pressY);
        if (this.resizing.movesLeft()) {
            left = Math.clamp(this.startX + dx, 0, Math.max(0, right - MIN_SIZE));
        } else if (this.resizing.movesRight()) {
            right = Math.clamp(this.startX + this.startWidth + dx, left + MIN_SIZE, Math.max(left + MIN_SIZE, this.width));
        }
        if (this.resizing.movesTop()) {
            top = Math.clamp(this.startY + dy, 0, Math.max(0, bottom - MIN_SIZE));
        } else if (this.resizing.movesBottom()) {
            bottom = Math.clamp(this.startY + this.startHeight + dy, top + MIN_SIZE, Math.max(top + MIN_SIZE, this.height));
        }
        config.setWidth(right - left);
        config.setHeight(bottom - top);
        setOffsetsFromPosition(config, left, top);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging || this.resizing != null) {
            this.dragging = false;
            this.resizing = null;
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
        this.resizing = null;
        ConfigManager.getInstance().save();
        this.minecraft.gui.setScreen(this.parent);
        if (this.parent instanceof ConfigScreen configScreen) {
            configScreen.refresh();
        }
    }

    private void renderHandles(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int boxX, int boxY, int boxW, int boxH) {
        Handle hovered = handleAt(mouseX, mouseY);
        for (Handle handle : Handle.values()) {
            int centerX = handle.x(boxX, boxW);
            int centerY = handle.y(boxY, boxH);
            boolean active = handle == this.resizing || (this.resizing == null && handle == hovered);
            int color = active ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_PRIMARY_ACCENT;
            graphics.fill(centerX - HANDLE_SIZE / 2 - 1, centerY - HANDLE_SIZE / 2 - 1,
                centerX - HANDLE_SIZE / 2 + HANDLE_SIZE + 1, centerY - HANDLE_SIZE / 2 + HANDLE_SIZE + 1,
                COLOR_HANDLE_OUTLINE);
            graphics.fill(centerX - HANDLE_SIZE / 2, centerY - HANDLE_SIZE / 2,
                centerX - HANDLE_SIZE / 2 + HANDLE_SIZE, centerY - HANDLE_SIZE / 2 + HANDLE_SIZE, color);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ConfigManager config = ConfigManager.getInstance();
        int boxX = boxX();
        int boxY = boxY();
        int boxW = config.getWidth();
        int boxH = config.getHeight();
        boolean active = this.dragging || this.resizing != null;
        if (active) {
            graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, COLOR_GUIDE);
            graphics.fill(0, this.height / 2, this.width, this.height / 2 + 1, COLOR_GUIDE);
            graphics.outline(boxX - 1, boxY - 1, boxW + 2, boxH + 2, ConfigScreen.COLOR_ACCENT_HOVER);
        }
        graphics.outline(boxX, boxY, boxW, boxH, active ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_PRIMARY_ACCENT);
        renderHandles(graphics, mouseX, mouseY, boxX, boxY, boxW, boxH);
        String hint = this.resizing != null
            ? "Resizing: " + config.getWidth() + " x " + config.getHeight()
            : "Drag the box to move, drag the handles to resize";
        graphics.centeredText(this.font, hint, this.width / 2, this.height - 46, ConfigScreen.COLOR_TEXT_MUTED);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}
