package com.metabrain.gdb;

import java.io.RandomAccessFile;

public class CacheData {
    byte readCount = 1;
    int lastTime = (int) (System.currentTimeMillis() / 1000L);
    int saveTime = lastTime + readCount;
    boolean isUpdated;
    RandomAccessFile file;
    int offset;
    byte[] data;

    public CacheData(boolean isUpdated, RandomAccessFile file, int offset, byte[] data) {
        this.isUpdated = isUpdated;
        this.file = file;
        this.offset = offset;
        this.data = data;
    }
}
