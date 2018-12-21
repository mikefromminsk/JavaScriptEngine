package com.metabrain.djs;

import jdk.nashorn.internal.runtime.ParserException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {

    @Test
    void parse() throws IOException {
        File file = new File("test_res/parse/JsParserScript.js");
        String scriptStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        Parser parser = new Parser();
        try {
            parser.parse(null, scriptStr);
        } catch (ParserException e) {
            fail("parse exception");
        }
    }

    @Test
    void parseSimple() {
        Parser parser = new Parser();
        try {
            parser.parse(null, "var wef = 1;");
        } catch (ParserException e) {
            fail("parse exception");
        }
    }


    /*@Test
    void parseException() {
        Parser parser = new Parser();
        Boolean parseError;
        try {
            parser.parse(null, "1d1d");
            parseError = false;
        } catch (ParserException e) {
            parseError = true;
        }
        assertTrue(parseError);
    }*/

}