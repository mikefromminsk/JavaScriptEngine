package com.metabrain.djs;

public enum LinkType {
    VALUE((byte) 0),
    SOURCE((byte) 1),
    TITLE((byte) 2),
    SET((byte) 3),
    TRUE((byte) 4),
    ELSE((byte) 5),
    EXIT((byte) 6),
    WHILE((byte) 7),
    IF((byte) 8),
    PROTOTYPE((byte) 9),
    BODY((byte) 10),
    LOCAL((byte) 10),
    PARAM((byte) 10),
    NEXT((byte) 10);

    private final byte value;

    private LinkType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
