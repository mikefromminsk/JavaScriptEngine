package com.metabrain.djs.refactored;

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
        if (!node.isSaved) {
            storage.addToTransaction(node);
            node.isSaved = true;
        }
        return this;
    }

    void onSave() {
        node.isSaved = false;
    }

    public Long getValue() {
        if (node.value instanceof Long)
            return (Long) node.value;
        else if (node.value instanceof Node)
            return node.id;
        return null;
    }

    public Node getValueNode() {
        if (node.value instanceof Long)
            return node;
        else if (node.value instanceof Node)
            return node;
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
}
