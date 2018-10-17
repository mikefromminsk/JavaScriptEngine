package com.metabrain.djs;

import refactored.Bytes;
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

    private Node valueNode;
    private Node sourceNode;
    private Node titleNode;
    private Node setNode;
    private Node trueNode;
    private Node elseNode;
    private Node exitNode;
    private Node whileNode;
    private Node ifNode;
    private Node prototypeNode;
    private Node bodyNode;

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

    private void appendDataBody(ArrayList<Long> links, Long nodeId, byte type) {
        if (nodeId != null && links != null) {
            // TODO change type of convert
            byte[] bytes = Bytes.fromLong(nodeId);
            bytes[0] = type;
            long dataLink = Bytes.toLong(bytes);
            links.add(dataLink);
        }
    }

    private void appendArrayBody(ArrayList<Long> links, ArrayList<Long> nodeIdList, byte type) {
        if (nodeIdList != null && nodeIdList.size() > 0)
            for (Long nodeId : nodeIdList)
                appendDataBody(links, nodeId, type);
    }

    @Override
    public byte[] build() {
        ArrayList<Long> links = new ArrayList<>();
        appendDataBody(links, value, LinkType.VALUE);
        appendDataBody(links, source, LinkType.SOURCE);
        appendDataBody(links, title, LinkType.TITLE);
        appendDataBody(links, set, LinkType.SET);
        appendDataBody(links, _true, LinkType.TRUE);
        appendDataBody(links, _else, LinkType.ELSE);
        appendDataBody(links, exit, LinkType.EXIT);
        appendDataBody(links, _while, LinkType.WHILE);
        appendDataBody(links, _if, LinkType.IF);
        appendDataBody(links, prototype, LinkType.PROTOTYPE);
        appendDataBody(links, body, LinkType.BODY);
        appendArrayBody(links, local, LinkType.LOCAL);
        appendArrayBody(links, param, LinkType.PARAM);
        appendArrayBody(links, next, LinkType.NEXT);
        return Bytes.fromLongList(links);
    }

    @Override
    public void parse(byte[] data) {
        long[] links = Bytes.toLongArray(data);
        for (long dataLink : links) {
            byte[] bytes = Bytes.fromLong(dataLink);
            dataLink = Bytes.toLong(bytes);
            switch (bytes[0]){
                case LinkType.VALUE: value = dataLink; break;
                case LinkType.SOURCE: source = dataLink; break;
                case LinkType.TITLE: title = dataLink; break;
                case LinkType.SET: set = dataLink; break;
                case LinkType.TRUE: _true = dataLink; break;
                case LinkType.ELSE: _else = dataLink; break;
                case LinkType.EXIT: exit = dataLink; break;
                case LinkType.WHILE: _while = dataLink; break;
                case LinkType.IF: _if = dataLink; break;
                case LinkType.PROTOTYPE: prototype = dataLink; break;
                case LinkType.BODY: body= dataLink; break;
                case LinkType.LOCAL: local.add(dataLink); break;
                case LinkType.PARAM: param.add(dataLink); break;
                case LinkType.NEXT: next.add(dataLink); break;
            }
        }
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
        this.valueNode = null;
        return this;
    }

    public Long getSource() {
        return source;
    }

    public Node setSource(Long source) {
        this.source = source;
        this.sourceNode = null;
        return this;
    }

    public Long getTitle() {
        return title;
    }

    public Node setTitle(Long title) {
        this.title = title;
        this.titleNode = null;
        return this;
    }

    public Long getSet() {
        return set;
    }

    public Node setSet(Long set) {
        this.set = set;
        this.setNode = null;
        return this;
    }

    public Long getTrue() {
        return _true;
    }

    public Node setTrue(Long _true) {
        this._true = _true;
        this.trueNode = null;
        return this;
    }

    public Long getElse() {
        return _else;
    }

    public Node setElse(Long _else) {
        this._else = _else;
        this.elseNode = null;
        return this;
    }

    public Long getExit() {
        return exit;
    }

    public Node setExit(Long exit) {
        this.exit = exit;
        this.exitNode = null;
        return this;
    }

    public Long getWhile() {
        return _while;
    }

    public Node setWhile(Long _while) {
        this._while = _while;
        this.whileNode = null;
        return this;
    }

    public Long getIf() {
        return _if;
    }

    public Node setIf(Long _if) {
        this._if = _if;
        this.trueNode = null;
        return this;
    }

    public Long getPrototype() {
        return prototype;
    }

    public Node setPrototype(Long prototype) {
        this.prototype = prototype;
        this.prototypeNode = null;
        return this;
    }

    public Long getBody() {
        return body;
    }

    public Node setBody(Long body) {
        this.body = body;
        this.bodyNode = null;
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

    private Node getLinkNode(Long nodeId) {
        return nodeId == null ? null : (Node) getStorage().get(nodeId, new Node());
    }

    public Node getValueNode() {
        return valueNode = getLinkNode(value);
    }

    public Node getSourceNode() {
        return sourceNode = getLinkNode(source);
    }

    public Node getTitleNode() {
        return titleNode = getLinkNode(title);
    }

    public Node getSetNode() {
        return setNode = getLinkNode(set);
    }

    public Node getTrueNode() {
        return trueNode = getLinkNode(_true);
    }

    public Node getElseNode() {
        return elseNode = getLinkNode(_else);
    }

    public Node getExitNode() {
        return exitNode = getLinkNode(exit);
    }

    public Node getWhileNode() {
        return whileNode = getLinkNode(_while);
    }

    public Node getIfNode() {
        return ifNode = getLinkNode(_if);
    }

    public Node getPrototypeNode() {
        return prototypeNode = getLinkNode(prototype);
    }

    public Node getBodyNode() {
        return bodyNode = getLinkNode(body);
    }
}
