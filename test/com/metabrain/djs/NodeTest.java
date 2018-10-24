package com.metabrain.djs;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @Test
    void testNode() {
        File currentScript = null;
        String sourceCode = null;
        RunThread runThread = new RunThread();
        try {
            File nodesTestsDir = new File("test/com/metabrain/djs/nodesTests/");
            for (File script : nodesTestsDir.listFiles()) {
                currentScript = script;
                sourceCode = FileUtils.readFileToString(script, StandardCharsets.UTF_8);
                Node module = new JsParser().parse(sourceCode);
                runThread.run(module.getId());
                Node testFunction = module.findLocal("test");
                assertNotNull(testFunction);
                assertNotNull(testFunction.getValue());
                Node testResult = new Node(testFunction.getValue());
                assertNotNull(testResult);
                Node resultData = new Node(testResult.getValue());
                assertNotNull(resultData);
                assertEquals(resultData.getType(), NodeType.BOOL);
                assertEquals(resultData.getData().readString(), "true");
            }
        } catch (Exception e) {
            if (currentScript != null)
                System.out.println(currentScript.getAbsolutePath());
            if (sourceCode != null)
                System.out.println(sourceCode);
            e.printStackTrace();
        }
    }
}