package org.notnightsky.companion.config.screen.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.notnightsky.companion.CompanionClient;
import org.notnightsky.companion.config.screen.ConfigScreen;
import org.notnightsky.companion.util.Textures;

import java.util.List;

public class TextureSwitcher extends AbstractWidget {
    private final Font font = Minecraft.getInstance().font;
    private final Runnable onSelect;

    public TextureSwitcher(int x, int y, int width, int height, Runnable onSelect) {
        super(x, y, width, height, Component.literal("texture set switcher"));
        this.onSelect = onSelect;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(getX(), getY(), getRight(), getBottom(), ConfigScreen.COLOR_SURFACE_CARD);
        graphics.outline(getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused() ? ConfigScreen.COLOR_ACCENT_HOVER : ConfigScreen.COLOR_BORDER_SUBTLE);

        int leftPadding = 2;
        String name = activeSetName();
        int color = name.isEmpty() ? ConfigScreen.COLOR_TEXT_MUTED : ConfigScreen.COLOR_TEXT_PRIMARY;
        graphics.textWithWordWrap(font, name.isEmpty() ? FormattedText.of("No texture set") : FormattedText.of(name), getX() + leftPadding, getY() + (getHeight() - font.lineHeight + 1) / 2, getWidth(), color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (!isActive() || !isMouseOver(event.x(), event.y())) {
            return false;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            cycle(-1);
        } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            cycle(1);
        } else {
            return false;
        }
        playButtonClickSound(Minecraft.getInstance().getSoundManager());
        setMessage(Component.literal(activeSetName()));
        return true;
    }

    private void cycle(int direction) {
        List<Textures.TextureSet> sets = CompanionClient.TEXTURE_SETS;
        if (sets.isEmpty()) {
            return;
        }
        int index = CompanionClient.getActiveSetIndex();
        int next;

        if (index < 0) {
            if (direction > 0) {
                next = 0;
            } else {
                next = sets.size() - 1;
            }
        } else {
            next = Math.floorMod(index + direction, sets.size());
        }

        CompanionClient.select(next);
        this.onSelect.run();
    }

    // dunno how this works but it works!!
    private String activeSetName() {
        int index = CompanionClient.getActiveSetIndex();
        if (index < 0 || index >= CompanionClient.TEXTURE_SETS.size()) {
            return "";
        }
        String name = Textures.relativePath(CompanionClient.TEXTURE_SETS.get(index).jsonPath());
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        return name.endsWith(".json") ? name.substring(0, name.length() - ".json".length()) : name;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
