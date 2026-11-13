package org.notnightsky.companion.config.screen.widgets;

import net.minecraft.network.chat.Component;
import org.notnightsky.companion.render.Anchor;

import java.util.function.Consumer;

public class AnchorButton extends StyledButton {
    private Anchor current;
    private final Consumer<Anchor> onSelect;

    public AnchorButton(int x, int y, int width, int height, Anchor initial, Consumer<Anchor> onSelect) {
        super(x, y, width, height, Component.literal(initial.name().toLowerCase()));
        this.current = initial;
        this.onSelect = onSelect;
    }

    @Override
    protected void press() {
        Anchor[] values = Anchor.values();
        this.current = values[(this.current.ordinal() + 1) % values.length];
        setMessage(Component.literal(this.current.name().toLowerCase()));
        this.onSelect.accept(this.current);
    }
}
