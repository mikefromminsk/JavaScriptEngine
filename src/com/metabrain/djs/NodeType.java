package com.metabrain.djs;

public enum NodeType {
    VAR((byte) 0),
    BOOL((byte) 1),
    NUMBER((byte) 2),
    STRING((byte) 3),
    ARRAY((byte) 4),
    OBJECT((byte) 5);

    private final byte value;

    private NodeType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
