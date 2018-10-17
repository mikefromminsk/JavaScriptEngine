package com.metabrain.djs;

import refactored.InfinityArray;
import refactored.InfinityArrayCell;

import java.util.ArrayList;

class Node implements InfinityArrayCell {

    private Long id;
    private byte[] data;
    private NodeType type;

    private Long value;
    private Long source;
    private Long title;
    private Long set;
    private Long _true;
    private Long _else;
    private Long exit;
    private Long _while;
    private Long _if;
    private Long prototype;
    private Long body;

    private ArrayList<Long> local = new ArrayList<>();
    private ArrayList<Long> param = new ArrayList<>();
    private ArrayList<Long> next = new ArrayList<>();

    private static final String storageID = "nodes";
    private static InfinityArray storage;

    private static InfinityArray getStorage() {
        if (storage == null)
            storage = new InfinityArray(storageID);
        return storage;
    }

    Node() {
    }

    Node(long node_id) {
        getStorage().get(node_id, this);
    }

    public Node commit() {
        getStorage().set(id, this);
        return this;
    }
    
    @Override
    public void parse(byte[] data) {

    }

    @Override
    public byte[] build() {
        return new byte[0];
    }

    public Long getId() {
        return id;
    }

    public Node setId(Long id) {
        this.id = id;
        return this;
    }

    public NodeType getType() {
        return type;
    }

    public Node setType(NodeType type) {
        this.type = type;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public Node setData(byte[] data) {
        this.data = data;
        return this;
    }

    public Long getValue() {
        return value;
    }

    public Node setValue(Long value) {
        this.value = value;
        return this;
    }

    public Long getSource() {
        return source;
    }

    public Node setSource(Long source) {
        this.source = source;
        return this;
    }

    public Long getTitle() {
        return title;
    }

    public Node setTitle(Long title) {
        this.title = title;
        return this;
    }

    public Long getSet() {
        return set;
    }

    public Node setSet(Long set) {
        this.set = set;
        return this;
    }

    public Long getTrue() {
        return _true;
    }

    public Node setTrue(Long _true) {
        this._true = _true;
        return this;
    }

    public Long getElse() {
        return _else;
    }

    public Node setElse(Long _else) {
        this._else = _else;
        return this;
    }

    public Long getExit() {
        return exit;
    }

    public Node setExit(Long exit) {
        this.exit = exit;
        return this;
    }

    public Long getWhile() {
        return _while;
    }

    public Node setWhile(Long _while) {
        this._while = _while;
        return this;
    }

    public Long getIf() {
        return _if;
    }

    public Node setIf(Long _if) {
        this._if = _if;
        return this;
    }

    public Long getPrototype() {
        return prototype;
    }

    public Node setPrototype(Long prototype) {
        this.prototype = prototype;
        return this;
    }

    public Long getBody() {
        return body;
    }

    public Node setBody(Long body) {
        this.body = body;
        return this;
    }

    public ArrayList<Long> getLocal() {
        return local;
    }

    public Node setLocal(ArrayList<Long> local) {
        this.local = local;
        return this;
    }

    public ArrayList<Long> getParam() {
        return param;
    }

    public Node setParam(ArrayList<Long> param) {
        this.param = param;
        return this;
    }

    public ArrayList<Long> getNext() {
        return next;
    }

    public Node setNext(ArrayList<Long> next) {
        this.next = next;
        return this;
    }
}
