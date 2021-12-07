package de.eldritch.spigot.DiscordSync.message;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Container implements CharSequence {
    private final String key;
    private final String[] values;

    private Container(@NotNull String key, String[] values) {
        this.key = key;
        this.values = values;
    }

    public static Container of(@NotNull String key, String... values) {
        return new Container(key, values);
    }

    public String getKey() {
        return key;
    }

    public String[] getValues() {
        return values;
    }

    @Override
    public int length() {
        return key.length();
    }

    @Override
    public char charAt(int index) {
        return key.charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return key.subSequence(start, end);
    }

    @Override
    public String toString() {
        return "Container{key=" + key + ", values=" + Arrays.toString(values) + "}";
    }
}
