package com.metabrain.gdb;

public class LongCell implements InfinityConstArrayCell {

    long value = 0;

    @Override
    public void parse(byte[] data) {
        value = Bytes.toLong(data);
    }

    public void setData(long data) {
        value = data;
    }

    @Override
    public byte[] build() {
        return Bytes.fromLong(value);
    }

    @Override
    public int getSize() {
        return Long.BYTES;
    }
}
