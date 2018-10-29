package com.metabrain.djs.refactored;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeBuilderTest {

    @Test
    void create() {
        NodeBuilder builder = new NodeBuilder();
        builder.create()
                .setValue(123L)
                .getId();
        NodeStorage.getInstance().transactionCommit();
    }
}