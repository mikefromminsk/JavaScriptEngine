package com.metabrain.djs.refactored;

import com.metabrain.djs.refactored.node.Node;
import com.metabrain.djs.refactored.node.NodeBuilder;
import com.metabrain.djs.refactored.node.NodeType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RunnerTest {

    @Test
    void run() {
        File currentScript = null;
        String sourceCode = null;
        NodeBuilder builder = new NodeBuilder();
        Runner runThread = new Runner();
        Node module;
        try {
            File nodesTestsDir = new File("test/com/metabrain/djs/nodesTests/");
            File[] tests = nodesTestsDir.listFiles();
            Parser parser = new Parser();
            if (tests != null) {
                List<File> list = Arrays.asList(tests);
                Collections.reverse(list);
                for (File script : list) {
                    currentScript = script;
                    sourceCode = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
                    module = parser.parse(sourceCode);
                    //System.out.println(Formatter.toJson(module));
                    runThread.run(module);
                    Node testVar = builder.set(module).findLocal("test");
                    assertNotNull(testVar);
                    System.out.println(Formatter.toJson(module));

                    Node testValue = builder.set(testVar).getValueNode();
                    assertNotNull(testValue);
                    assertEquals(NodeType.BOOL, testValue.type);
                    assertTrue((Boolean) builder.set(testValue).getData().getObject());
                }
            } else {
                fail("tests not found");
            }
        } catch (IOException e) {
            if (currentScript != null)
                System.out.println(currentScript.getAbsolutePath());
            if (sourceCode != null)
                System.out.println(sourceCode);
            fail(e);
        }
    }
}
