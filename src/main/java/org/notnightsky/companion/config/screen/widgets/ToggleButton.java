package org.notnightsky.companion.config.screen.widgets;

import net.minecraft.network.chat.Component;
import org.notnightsky.companion.config.screen.ConfigScreen;

import java.util.function.Consumer;

public class ToggleButton extends StyledButton {
    private boolean on;
    private final Consumer<Boolean> onSelect;

    public ToggleButton(int x, int y, int width, int height, boolean initial, Consumer<Boolean> onSelect) {
        super(x, y, width, height, Component.literal(initial ? "on" : "off"));
        this.on = initial;
        this.onSelect = onSelect;
    }

    @Override
    protected void press() {
        this.on = !this.on;
        setMessage(Component.literal(this.on ? "on" : "off"));
        this.onSelect.accept(this.on);
    }

    @Override
    protected int textColor(boolean highlighted) {
        if (!highlighted && this.on) {
            return ConfigScreen.COLOR_STATUS_ONLINE;
        }
        return super.textColor(highlighted);
    }
}
