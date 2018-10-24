package com.metabrain.djs;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormatterTest {

    @Test
    public void toJson() {
        Node parseNode = new Node()
                .setTitle("string")
                .setValue(new Node(NodeType.NUMBER)
                        .setData("23".getBytes())
                        .commit().getId())
                .commit();
        System.out.println(Formatter.toJson(parseNode));
    }
}