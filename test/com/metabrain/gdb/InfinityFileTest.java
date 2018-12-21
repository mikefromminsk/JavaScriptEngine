package com.metabrain.gdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfinityFileTest {

    @Test
    void testReadFromWriteBuffer1() {
        InfinityFile file = new InfinityFile("test1");
        byte[] testData = "test".getBytes();
        long position = file.add(testData);
        byte[] readiedData = file.read(position, testData.length);
        assertEquals(new String(testData), new String(readiedData));
    }
}