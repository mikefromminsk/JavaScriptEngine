package com.metabrain.djs.refactored;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class ParserTest {

    @Test
    void parse() {
        try {
            File file = new File("test/com/metabrain/djs/parserTests/JsParserScript.js");
            String scriptStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            new Parser().parse(scriptStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}