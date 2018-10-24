package com.metabrain.djs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NodeMakesTest {

    @Test
    void testPath() {
        Long id1 = new Node().loadPath("test1").commit().getId();
        Long id2 = new Node().loadPath("test1").commit().getId();
        Long id3 = new Node().loadPath("test2").commit().getId();
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }

    @Test
    void testLocal() {
        Long id1 = new Node().loadPath("test").makeLocal("local1").commit().getId();
        Long id2 = new Node().loadPath("test").makeLocal("local1").commit().getId();
        Long id3 = new Node().loadPath("test").makeLocal("local2").commit().getId();
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }
}
