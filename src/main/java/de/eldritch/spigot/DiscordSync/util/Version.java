package de.eldritch.spigot.DiscordSync.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Version(byte MAJOR, byte MINOR, short BUILD, String EXTRA) {
    public static Version parse(String raw) throws IllegalVersionException {
        String[] parts = raw.split("\\.|-");

        byte major;
        byte minor;
        short build;
        try {
            major = Byte.parseByte(parts[0]);
            minor = Byte.parseByte(parts[1]);
            build = Short.parseShort(parts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalVersionException(raw, e);
        }

        String extra = null;
        if (parts.length > 3)
            extra = parts[3];

        return new Version(major, minor, build, extra);
    }

    public static Version parseFromFileName(String fileName) throws IllegalVersionException {
        Matcher matcher = Pattern.compile("/\\d\\d\\.\\d\\d-\\d\\d\\d(-[^\\\\.]*)?/gm").matcher(fileName);
        if (matcher.find())
            return parse(matcher.group(1));
        else
            throw new IllegalVersionException("FileName '" + fileName + "' does not seem to contain a legal version.");
    }

    /**
     * Compares another {@link Version} object and returns wether
     * it represents a more recent version.
     * <p>
     *     This ignores version extras and only compares major,
     *     minor and build.
     * </p>
     * @param v The {@link Version} to compare.
     * @return true if v represents a more recent Version.
     */
    public boolean isMoreRecent(Version v) {
        if (this.MAJOR < v.MAJOR) return true;
        if (this.MINOR < v.MINOR) return true;
        return this.BUILD < v.BUILD;
    }
}