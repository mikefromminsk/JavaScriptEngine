package com.metabrain.gdb;

import java.nio.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Bytes {

    public static byte[] fromInt(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] fromLong(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static long toLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] fromChar(char value) {
        byte[] result = new byte[2];
        result[0] = (byte) ((value & 0xFF00) >> 8);
        result[1] = (byte) (value & 0x00FF);
        return result;
    }

    public static char toChar(byte[] bytes) {
        return (char) (((bytes[0] & 0x00FF) << 8) + (bytes[1] & 0x00FF));
    }

    public static int[] toIntArray(byte[] bytes) {
        IntBuffer intBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
        int result[] = new int[intBuffer.capacity()];
        intBuffer.get(result);
        return result;
    }

    public static byte[] fromIntArray(int[] value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(value.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(value);
        return byteBuffer.array();
    }

    public static long[] toLongArray(byte[] bytes) {
        LongBuffer longBuffer = ByteBuffer.wrap(bytes).asLongBuffer();
        long result[] = new long[longBuffer.capacity()];
        longBuffer.get(result);
        return result;
    }

    public static byte[] fromLongArray(long[] arr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.length * Long.BYTES);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.put(arr);
        return byteBuffer.array();
    }

    public static byte[] fromLongList(ArrayList<Long> arr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.size() * Long.BYTES);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        for (int i = 0; i < arr.size(); i++)
            longBuffer.put(i, arr.get(i));
        return byteBuffer.array();
    }

    public static byte[] fromString(String mask) {
        return mask.getBytes();
    }

    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] fromCharArray(char[]  chars) {
        return Charset.forName("UTF-8").encode(CharBuffer.wrap(chars)).array();
    }

    public static char[] toCharArray(byte[] bytes) {
        String str = new String(bytes);
        return str.toCharArray();
    }

    public static byte[] append(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
