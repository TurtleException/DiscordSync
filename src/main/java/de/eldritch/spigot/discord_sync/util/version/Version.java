package de.eldritch.spigot.discord_sync.util.version;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final record Version(byte major, byte minor, short build, String extra) {
    /**
     * Parses a Version object from a String.
     * @param raw String representation of the version.
     * @return Version object built from the provided String.
     * @throws IllegalVersionException if the provided String does not represent a valid Version.
     */
    public static Version parse(String raw) throws IllegalVersionException {
        if (raw == null)
            throw new IllegalVersionException("String is null");

        String[] parts = raw.split("[.\\-_]");

        byte  major;
        byte  minor;
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

    /**
     * Compares another {@link Version} object and returns whether
     * it represents a more recent version.
     * <p>
     *     This ignores version extras and only compares major,
     *     minor and build.
     * </p>
     * @param v The {@link Version} to compare.
     * @return true if v represents a more recent Version.
     */
    public boolean isMoreRecent(Version v) {
        if (this.major < v.major) return true;
        if (this.minor < v.minor) return true;
        return this.build < v.build;
    }

    /**
     * Returns a string representation of the version.
     * <p>
     *     Schema: (depending on whether <code>extra</code> is defined)
     *     <li><code>major.minor-build</code></li>
     *     <li><code>major.minor-build_extra</code></li>
     * </p>
     * @return a string representation of the object.
     */
    @Override
    public @NotNull String toString() {
        return new DecimalFormat("00").format(major)
                + "." + new DecimalFormat("00").format(minor)
                + "-" + new DecimalFormat("000").format(build)
                + ((extra != null && !extra.equals("")) ? "_" + extra : "");
    }
}