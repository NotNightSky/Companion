package org.notnightsky.companion.config.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.notnightsky.companion.config.ConfigManager;
import org.notnightsky.companion.config.screen.widgets.NumberField;
import org.notnightsky.companion.config.screen.widgets.TextureSwitcher;
import org.notnightsky.companion.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public static final int COLOR_PRIMARY_ACCENT = 0xFFFF5544;
    public static final int COLOR_ACCENT_HOVER   = 0xFFFF6D5D;

    public static final int COLOR_BG_BASE        = 0xFF0F1115;
    public static final int COLOR_SURFACE_PANEL  = 0xFF181A20;
    public static final int COLOR_SURFACE_CARD   = 0xFF21242C;
    public static final int COLOR_BUTTON_DEFAULT = 0xFF323846;

    public static final int COLOR_BORDER_SUBTLE  = 0xFF2D323E;

    public static final int COLOR_TEXT_PRIMARY   = 0xFFF3F4F6;
    public static final int COLOR_TEXT_MUTED     = 0xFF9CA3AF;

    public static final int COLOR_STATUS_ONLINE  = 0xFF22C55E;

    public ConfigScreen(Screen parent) {
        super(Component.literal("companion"));
        this.parent = parent;
    }

    private static final int FIELD_WIDTH = 120;
    private static final int FIELD_HEIGHT = 20;
    private static final int LABEL_GAP = 4;
    private static final int FIELD_GAP = 12;

    private static final int SWITCHER_HEIGHT = 20;
    private static final int LAYOUT_PADDING = 20;
    private static final int SWITCHER_GAP = 16;

    private final List<FieldLabel> fieldLabels = new ArrayList<>();

    private record FieldLabel(String text, int x, int y) {
    }

    private record BackgroundRect(int centerX, int centerY, int width, int height) {
    }

    private int contentHeight() {
        return LAYOUT_PADDING
            + SWITCHER_HEIGHT
            + SWITCHER_GAP
            + 3 * (this.font.lineHeight + LABEL_GAP + FIELD_HEIGHT)
            + 2 * FIELD_GAP
            + LAYOUT_PADDING;
    }

    private BackgroundRect backgroundRect() {
        int margin = Math.clamp(
            Math.min(60, Math.min((this.width - 280) / 2, (this.height - 320) / 2)),
            8,
            60
        );
        int fitWidth = this.width - margin * 2;
        int fitHeight = this.height - margin * 2;
        int width = Math.min(fitWidth, fitHeight * 16 / 9);
        int height = width * 9 / 16;
        int minContentHeight = contentHeight();
        if (height < minContentHeight) {
            height = minContentHeight;
            width = Math.clamp(fitWidth, width, height * 16 / 9);
        }
        return new BackgroundRect(this.width / 2, this.height / 2, width, height);
    }

    @Override
    protected void init() {
        BackgroundRect bg = backgroundRect();
        int switcherWidth = Math.min(240, bg.width() - 20);
        fieldLabels.clear();
        int startY = bg.centerY() - bg.height() / 2 + LAYOUT_PADDING;
        addRenderableWidget(new TextureSwitcher(
            bg.centerX() - switcherWidth / 2,
            startY,
            switcherWidth,
            SWITCHER_HEIGHT
        ));

        ConfigManager config = ConfigManager.getInstance();
        int fieldX = bg.centerX() - FIELD_WIDTH / 2;
        int y = startY + SWITCHER_HEIGHT + SWITCHER_GAP;
        y = addNumberField(fieldX, y, "Frametime", config.getFrametime(), 1, 10000, value -> {
            config.setFrametime(value);
            Renderer.setFrameTime(value);
            config.save();
        });
        y = addNumberField(fieldX, y, "Width", config.getWidth(), 1, 512, value -> {
            config.setWidth((int) value);
            Renderer.setWidth((int) value);
            config.save();
        });
        addNumberField(fieldX, y, "Height", config.getHeight(), 1, 512, value -> {
            config.setHeight((int) value);
            Renderer.setHeight((int) value);
            config.save();
        });
    }

    private int addNumberField(int x, int y, String label, long initial, long min, long max, LongConsumer apply) {
        fieldLabels.add(new FieldLabel(label, x + FIELD_WIDTH / 2, y));
        addRenderableWidget(new NumberField(x, y + this.font.lineHeight + LABEL_GAP, FIELD_WIDTH, FIELD_HEIGHT, String.valueOf(initial), min, max, apply));
        return y + this.font.lineHeight + LABEL_GAP + FIELD_HEIGHT + FIELD_GAP;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
        ConfigManager.getInstance().save();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        BackgroundRect bg = backgroundRect();
        graphics.fill(
            bg.centerX() - bg.width() / 2,
            bg.centerY() - bg.height() / 2,
            bg.centerX() + bg.width() / 2,
            bg.centerY() + bg.height() / 2, COLOR_BG_BASE);
        for (FieldLabel label : fieldLabels) {
            graphics.centeredText(font, label.text(), label.x(), label.y(), COLOR_TEXT_MUTED);
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
