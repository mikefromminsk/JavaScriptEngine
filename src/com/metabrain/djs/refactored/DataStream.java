package com.metabrain.djs.refactored;

import com.metabrain.djs.refactored.NodeStorage;

public class DataStream {

    private static final long BUFFER_SIZE = NodeStorage.MAX_STORAGE_DATA_IN_DB * 500;
    public long start;
    public long length;
    private long currentPosition;

    public DataStream(long start, long length) {
        this.start = start;
        this.length = length;
        currentPosition = 0;
    }

    void seekToStart() {
        currentPosition = 0;
    }

    boolean hasNext() {
        return currentPosition < length;
    }

    byte[] readFragment() {
        byte[] data = NodeStorage.getInstance().getSmallData(start, currentPosition, (int) Math.min(BUFFER_SIZE, length));
        currentPosition += data.length;
        return data;
    }
}
