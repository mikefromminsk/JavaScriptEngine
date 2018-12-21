package com.metabrain.gdb;

public class MetaCell implements InfinityConstArrayCell {

    public long start;
    public long length;
    public long accessKey;

    @Override
    public void parse(byte[] data) {
        long[] metaOfIndex = Bytes.toLongArray(data);
        start = metaOfIndex[0];
        length = metaOfIndex[1];
        accessKey = metaOfIndex[2];
    }

    @Override
    public byte[] build() {
        long[] data = new long[3];
        data[0] = start;
        data[1] = length;
        data[2] = accessKey;
        return Bytes.fromLongArray(data);
    }

    @Override
    public int getSize() {
        return 3 * Long.BYTES;
    }
}