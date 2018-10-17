package com.metabrain.djs;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class NodeTest {

    @Test
    void testNode() {
        System.out.println(
                new File("test.js\\com\\metabrain\\djs\\test.js").getAbsolutePath()
        );
        System.out.println(
                new File("test.js\\com\\metabrain\\djs\\test.js").isFile()
        );
        assertEquals(1, 1);
    }
}