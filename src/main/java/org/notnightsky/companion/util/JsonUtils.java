package org.notnightsky.companion.util;

import com.google.gson.JsonObject;
import org.notnightsky.companion.render.Anchor;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static Integer readInt(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Integer readPositiveInt(JsonObject obj, String key) {
        Integer value = readInt(obj, key);
        return value == null ? null : Math.max(1, value);
    }

    public static Long readPositiveLong(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return Math.max(1, obj.get(key).getAsLong());
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Double readDouble(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return obj.get(key).getAsDouble();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Boolean readBoolean(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return obj.get(key).getAsBoolean();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static String readString(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return obj.get(key).getAsString();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Anchor readAnchor(JsonObject obj, String key) {
        String value = readString(obj, key);
        return value == null ? null : Anchor.tryParse(value);
    }
}
