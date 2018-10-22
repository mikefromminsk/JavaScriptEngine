package com.metabrain.djs;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class NodeTest {

    @Test
    void testPath() {
        Long id1 = new Node().loadPath("test").commit().getId();
        Long id2 = new Node().loadPath("test").commit().getId();
        assertEquals(id1, id2);
    }

    @Test
    void testLocal() {
        Long id1 = new Node().loadPath("test").makeLocal("local1").commit().getId();
        Long id2 = new Node().loadPath("test").makeLocal("local1").commit().getId();
        assertEquals(id1, id2);
    }


/*
    @Test
    void testNode() {
        File scriptsDir = new File(NodeTest.class.getPackage().getName().replace('.', '/') + "scripts");
        File[] scripts = scriptsDir.listFiles((dir, name) -> name.endsWith(".js"));
        if (scripts != null)
            for (File script : scripts) {
                try {
                    String source = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
                    Node node = new Node().commit();
                    JsParser.parse(source, node);
                    Runner.run(node);
                    for (Long node_id : node.getLocal()){

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        assertEquals(1, 1);
    }*/
}