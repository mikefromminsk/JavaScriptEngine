package com.metabrain.djs.node;

public class DataStreamReader {

    public static String getString(DataStream data) {
        StringBuilder builder = new StringBuilder();
        while (data.hasNext())
            builder.append(data.readChars());
        return builder.toString();
    }

}
