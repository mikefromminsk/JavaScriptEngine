package com.metabrain.djs.refactored;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NodeBuilder {

    NodeStorage storage = NodeStorage.getInstance();
    Node node;

    NodeBuilder create() {
        return create(NodeType.VAR);
    }

    NodeBuilder create(byte type) {
        node = new Node();
        node.type = type;
        return this;
    }

    NodeBuilder get(Long id) {
        node = storage.get(id);
        return this;
    }

    NodeBuilder commit() {
        if (node.id == null)
            storage.add(node);
        else
            storage.set(node.id, node);
        if (!node.isSaved)
            storage.addToTransaction(node);
        return this;
    }

    public Long getValue() {
        if (node.value instanceof Long)
            return (Long) node.value;
        else if (node.value instanceof Node)
            return node.id;
        return null;
    }

    public Node getValueNode() {
        if (node.value instanceof Node)
            return node;
        else if (node.value instanceof Long)
            return (Node) (node.value = storage.get((Long) node.value));
        return null;
    }

    public NodeBuilder setValue(Long value) {
        node.value = value;
        return this;
    }

    public Long getId() {
        if (node.id == null)
            commit();
        return node.id;
    }

    public DataStream getData() {
        return node.data;
    }

    public NodeBuilder setData(String string) {
        setData(string.getBytes());
        return this;
    }

    public NodeBuilder setData(byte[] data) {
        setData(new ByteArrayInputStream(data));
        return this;
    }

    public NodeBuilder setData(InputStream stream) {
        node.externalData = stream;
        return this;
    }
}
