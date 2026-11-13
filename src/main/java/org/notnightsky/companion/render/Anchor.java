package org.notnightsky.companion.render;

public enum Anchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    public static Anchor tryParse(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "top_left" -> TOP_LEFT;
            case "top_right" -> TOP_RIGHT;
            case "bottom_left" -> BOTTOM_LEFT;
            case "bottom_right" -> BOTTOM_RIGHT;
            default -> null;
        };
    }
}
