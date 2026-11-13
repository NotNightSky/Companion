package org.notnightsky.companion.config.screen.widgets;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ActionButton extends StyledButton {
    private final Runnable action;

    public ActionButton(int x, int y, int width, int height, Runnable action) {
        super(x, y, width, height, Component.literal("suggested"));
        setTooltip(Tooltip.create(Component.literal("apply the suggested value")));
        this.action = action;
    }

    @Override
    protected void press() {
        this.action.run();
    }
}
