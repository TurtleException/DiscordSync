package de.turtle_exception.discordsync.util.time;

public enum TurtleType {
    UNKNOWN(0),
    USER(1),
    MESSAGE(2),
    CHANNEL(3),
    ENDPOINT(4);

    private final byte b;

    TurtleType(int b) {
        this.b = (byte) b;
    }

    public byte getAsByte() {
        return b;
    }
}