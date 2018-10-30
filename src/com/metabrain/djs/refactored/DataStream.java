package com.metabrain.djs.refactored;

public class DataStream {

    private int BUFFER_SIZE = NodeStorage.MAX_STORAGE_DATA_IN_DB * 500;
    public long start;
    public long length;
    long currentPosition;

    public DataStream(long start, long length) {
        this.start = start;
        this.length = length;
        currentPosition = 0;
    }

    void seekToStart() {
        currentPosition = 0;
    }

    boolean hasNext() {
        return currentPosition >= length;
    }

    byte[] readFragment() {
        byte[] data = NodeStorage.getInstance().getSmallData(start, currentPosition, BUFFER_SIZE);
        currentPosition += data.length;
        return data;
    }
}
