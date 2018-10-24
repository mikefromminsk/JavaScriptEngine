package com.metabrain.djs;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JsParserTest {

    @Test
    void parse() {
        try {
            String scriptStr = FileUtils.readFileToString(new File("test/com/metabrain/djs/parserTests/JsParserScript.js"));
            new JsParser().parse(scriptStr);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}