package com.metabrain.djs;

import refactored.InfinityArray;
import refactored.InfinityArrayCell;

import java.util.ArrayList;

class Node implements InfinityArrayCell {

    long id;
    byte[] data;
    NodeType type;

    Long value;
    Long source;
    Long title;
    Long set;
    Long _true;
    Long _else;
    Long exit;
    Long _while;
    Long _if;
    Long prototype;
    Long body;

    ArrayList local;
    ArrayList params;
    ArrayList next;

    private static final String storageID = "nodes";
    private static InfinityArray storage;
    private static InfinityArray getStorage(){
        if (storage == null)
            storage = new InfinityArray(storageID);
        return storage;
    }

    Node(){
    }

    Node(long node_id){
        getStorage().get(node_id, this);
    }

    void save(){
        getStorage().set(id, this);
    }

    public Node setType(NodeType type) {
        this.type = type;
        return this;
    }

    @Override
    public void setData(byte[] data) {

    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public int getSize() {
        return 0;
    }
}
