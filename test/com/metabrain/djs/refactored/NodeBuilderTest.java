package com.metabrain.djs.refactored;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;


class NodeBuilderTest {

    @Test
    //@RepeatedTest(100)
    void create() {
        NodeBuilder builder = new NodeBuilder();
        Long valueNodeId = builder.create().getId();
        Long secondValueNodeId = builder.create().getId();
        Long nodeId = builder.create().setValue(valueNodeId).getId();
        Long valueId = builder.get(nodeId).getValue();
        assertEquals(valueNodeId, valueId);
        NodeStorage.getInstance().transactionCommit();
        NodeStorage.getInstance().clearCache();
        assertEquals(valueNodeId, builder.get(nodeId).getValue());
        builder.setValue(secondValueNodeId).commit();
        assertEquals(secondValueNodeId, builder.get(nodeId).getValue());
        NodeStorage.getInstance().transactionCommit();
        NodeStorage.getInstance().clearCache();
        assertEquals(secondValueNodeId, builder.get(nodeId).getValueNode().id);

        builder.create(NodeType.STRING).setData("string").commit();
    }

}