package com.metabrain.djs.refactored;

import com.metabrain.gdb.Bytes;

public class DataStreamReader {

    public static String getString(DataStream data) {
        data.seekToStart();
        StringBuilder builder = new StringBuilder();
        while (data.hasNext())
            builder.append(Bytes.toCharAray(data.readFragment()));
        return builder.toString();
    }

}
