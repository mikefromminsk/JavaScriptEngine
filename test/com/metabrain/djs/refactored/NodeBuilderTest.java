package com.metabrain.djs.refactored;

import org.junit.jupiter.api.Test;

import java.io.*;

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

        String string = "string";
        Long str1Id = builder.create(NodeType.STRING).setData(string).getId();
        String str = DataStreamReader.getString(builder.get(str1Id).getData());
        assertEquals(string, str);

        try {
            Long fileNodeId = builder.create(NodeType.STRING)
                    .setData(new FileInputStream("test/com/metabrain/djs/nodeTests/testData.txt"))
                    .getId();
            DataStream dataStream = builder.get(fileNodeId).getData();

            StringBuilder stringBuilder = new StringBuilder();
            while (dataStream.hasNext())
                stringBuilder.append(dataStream.readChars());
            byte[] ss = stringBuilder.toString().getBytes();

            assertEquals(1024 * 1024 * 3, ss.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}