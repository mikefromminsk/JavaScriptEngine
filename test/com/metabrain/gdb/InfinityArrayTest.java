package com.metabrain.gdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class InfinityArrayTest {

    @Test
    void testEncodingDecoding() {
        String startData = "start data";
        byte[] data = startData.getBytes();
        long accessKey = InfinityArray.encodeData(data);
        assertFalse(new String(data).equals(startData));
        InfinityArray.decodeData(data, accessKey);
        assertEquals(startData, new String(data));
    }

    @Test
    void add() {
        InfinityArray testArray = new InfinityArray("testArray");
        long index = testArray.add("tests");
        testArray.set(index, "bests");
        String results = testArray.getString(index);
        assertEquals("bests", results);
    }

    @Test
    void testAddToGarbage() throws Exception {
        InfinityArray arr = new InfinityArray("testArrayGarbage");
        long index = arr.add("test");
        arr.set(index, "testss");
        assertEquals("testss", arr.getString(index));

        InfinityConstArray garbage4 = arr.garbageCollector.get(4L);

        long garbage4Size = garbage4.getLong(0);
        long lastValue = garbage4.getLong(garbage4Size);
        assertEquals(index, lastValue);

        long ssIndex = arr.add("ss");
        arr.set(ssIndex, "ssdd");
        InfinityConstArray garbage2 = arr.garbageCollector.get(2L);
        long garbage2Size = garbage2.getLong(0);
        lastValue = garbage2.getLong(garbage2Size);
        assertEquals(ssIndex, lastValue);

        long garbage4NewSize = garbage4.getLong(0);
        assertEquals(garbage4Size - 1, garbage4NewSize);
    }
}