package org.notnightsky.companion.config.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.notnightsky.companion.config.screen.ConfigScreen;

import java.util.function.LongConsumer;

public class NumberField extends EditBox {
    private final long min;
    private final long max;
    private final LongConsumer onChange;
    private long applied;
    private boolean updating;

    public NumberField(int x, int y, int width, int height, String initial, long min, long max, LongConsumer onChange) {
        super(Minecraft.getInstance().font, x, y, width, height, Component.literal("number field"));
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.applied = clamp(Long.parseLong(initial));

        setMaxLength(String.valueOf(max).length() + (min < 0 ? 1 : 0));
        setBordered(false);
        setHint(Component.literal("0"));
        setTextColor(ConfigScreen.COLOR_TEXT_PRIMARY);
        setValue(initial);
        setResponder(this::respond);
    }

    private long clamp(long value) {
        return Math.clamp(value, min, max);
    }

    private void respond(String text) {
        if (updating || text.isEmpty()) {
            return;
        }
        long parsed;
        try {
            parsed = Long.parseLong(text);
        } catch (NumberFormatException e) {
            return;
        }
        long clamped = clamp(parsed);
        if (clamped != parsed) {
            updating = true;
            setValue(String.valueOf(clamped));
            updating = false;
        }
        if (clamped != applied) {
            applied = clamped;
            onChange.accept(clamped);
        }
    }

    public void set(long value) {
        long clamped = clamp(value);
        this.updating = true;
        setValue(String.valueOf(clamped));
        this.updating = false;
        this.applied = clamped;
    }

    @Override
    public void insertText(String text) {
        String digits = text.replaceAll("\\D", "");
        if (this.min < 0 && text.contains("-")) {
            digits = "-" + digits;
        }
        if (!digits.isEmpty()) {
            super.insertText(digits);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            String normalized = String.valueOf(applied);
            if (!getValue().equals(normalized)) {
                setValue(normalized);
            }
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(getX(), getY(), getRight(), getBottom(), ConfigScreen.COLOR_SURFACE_CARD);
        graphics.outline(getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused() ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_BORDER_SUBTLE);
        graphics.pose().pushMatrix();
        graphics.pose().translate(2.0F, (getHeight() - Minecraft.getInstance().font.lineHeight + 1) / 2.0F);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
        graphics.pose().popMatrix();
    }
}
