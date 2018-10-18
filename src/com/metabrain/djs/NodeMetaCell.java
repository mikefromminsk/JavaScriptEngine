package com.metabrain.djs;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.MetaCell;

public class NodeMetaCell extends MetaCell {

    // todo change to byte
    public long type;

    @Override
    public void parse(byte[] data) {
        super.parse(data);
        long[] metaOfIndex = Bytes.toLongArray(data);
        start = metaOfIndex[0];
        length = metaOfIndex[1];
        accessKey = metaOfIndex[2];
        type = metaOfIndex[3];
    }

    @Override
    public byte[] build() {
        long[] data = new long[4];
        data[0] = start;
        data[1] = length;
        data[2] = accessKey;
        data[3] = type;
        return Bytes.fromLongArray(data);
    }

    @Override
    public int getSize() {
        return 4 * Long.BYTES;
    }
}
