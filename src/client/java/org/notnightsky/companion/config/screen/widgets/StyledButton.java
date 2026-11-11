package org.notnightsky.companion.config.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.notnightsky.companion.config.screen.ConfigScreen;

public abstract class StyledButton extends AbstractWidget {
    protected StyledButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        playButtonClickSound(Minecraft.getInstance().getSoundManager());
        press();
    }

    protected abstract void press();

    protected int textColor(boolean highlighted) {
        return highlighted ? ConfigScreen.COLOR_TEXT_PRIMARY : ConfigScreen.COLOR_TEXT_MUTED;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean highlighted = isHoveredOrFocused();
        graphics.fill(getX(), getY(), getRight(), getBottom(), highlighted ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_BUTTON_DEFAULT);
        graphics.centeredText(Minecraft.getInstance().font, getMessage().getString(), getX() + getWidth() / 2, getY() + (getHeight() - Minecraft.getInstance().font.lineHeight + 1) / 2, textColor(highlighted));
    }
}
