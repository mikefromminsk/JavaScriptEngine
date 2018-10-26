package com.metabrain.djs.refactored;

public class Node extends BaseNode {

    boolean isSaved = false;

    static Node create() {
        return create(NodeType.VAR);
    }


    static Node create(byte nodeType) {

    }

    static Node get(Long id) {
        return null;
    }

    Node commit() {
        if (!isSaved)
            NodeStorage.getInstance().addCommitNode(this);
        isSaved = true;
        return this;
    }

    void onSave() {
        isSaved = false;
    }

}
