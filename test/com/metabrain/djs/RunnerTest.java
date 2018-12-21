package com.metabrain.djs;

import com.metabrain.djs.node.Node;
import com.metabrain.djs.node.NodeBuilder;
import com.metabrain.djs.node.NodeType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
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
        Node module = null;
        try {
            File nodesTestsDir = new File("test_res/run/");
            File[] tests = nodesTestsDir.listFiles();
            Parser parser = new Parser();
            if (tests != null) {
                List<File> list = Arrays.asList(tests);
                Collections.reverse(list);
                for (File script : list) {
                    currentScript = script;
                    sourceCode = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
                    module = parser.parse(null, sourceCode);
                    //System.out.println(Formatter.toJson(module));
                    runThread.run(module);
                    Node testVar = builder.set(module).findLocal("test");
                    assertNotNull(testVar);

                    Node testValue = builder.set(testVar).getValueNode();
                    assertNotNull(testValue);
                    Assertions.assertEquals(NodeType.BOOL, testValue.type);
                    Boolean testData = (Boolean) builder.set(testValue).getData().getObject();
                    if (testData != null && !testData && module != null) {
                        System.out.println(currentScript.getAbsolutePath());
                        System.out.println(Formatter.toJson(module));
                    }
                    assertTrue(testData);
                }
            } else {
                fail("tests not found");
            }
        } catch (IOException e) {
            fail(e);
        }
    }
}
