package com.metabrain.djs;

import com.metabrain.djs.node.*;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


class NodeBuilderTest {

    @Test
    //@RepeatedTest(100)
    // TODO solve problem with RepeatedTest(100)
    void create() {
        NodeBuilder builder = new NodeBuilder();
        Long valueNodeId = builder.create().getId();
        Long secondValueNodeId = builder.create().getId();
        Node nodeId = builder.create().setValue(valueNodeId).commit();
        Long valueId = builder.set(nodeId).getValue();
        assertEquals(valueNodeId, valueId);
        NodeStorage.getInstance().transactionCommit();
        NodeStorage.getInstance().clearCache();
        assertEquals(valueNodeId, builder.set(nodeId).getValue());
        builder.setValue(secondValueNodeId).commit();
        assertEquals(secondValueNodeId, builder.set(nodeId).getValue());
        NodeStorage.getInstance().transactionCommit();
        NodeStorage.getInstance().clearCache();
        assertEquals(secondValueNodeId, builder.set(nodeId).getValueNode().id);

        String string = "string";
        Node str1Id = builder.create(NodeType.STRING).setData(string).commit();
        String str = DataStreamReader.getString(builder.set(str1Id).getData());
        assertEquals(string, str);

        try {
            Node fileNodeId = builder.create(NodeType.STRING)
                    .setData(new FileInputStream("test_res/data_stream/testData.txt"))
                    .commit();
            DataStream dataStream = builder.set(fileNodeId).getData();

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