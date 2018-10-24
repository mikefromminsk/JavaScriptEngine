package com.metabrain.djs;

import com.google.gson.Gson;
import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityArrayCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Node implements InfinityArrayCell {

    // TODO delete string from Node and NodeStorage

    private Long id;
    private DataStream data;
    private byte type;
    private Integer functionId;

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
    private ArrayList<Long> cell = new ArrayList<>();
    private ArrayList<Long> properties = new ArrayList<>();

    Node() {
        setType(NodeType.VAR);
    }

    Node(byte nodeType) {
        setType(nodeType);
    }

    Node(long node_id) {
        NodeStorage.getInstance().get(node_id, this);
    }

    Node(byte[] data) {
        setType(NodeType.STRING);
        setData(data);
        commit();
    }

    public Node commit() {
        if (id == null)
            NodeStorage.getInstance().add(this);
        else
            NodeStorage.getInstance().set(id, this);
        return this;
    }

    public Node makeLocal(String title) {
        return makeLocal(title.getBytes());
    }

    public Node makeLocal(byte[] title) {
        if (id == null) commit();
        Long titleId = NodeStorage.getInstance().getData(title);
        if (titleId != null) {
            for (Long localNodeId : getLocal()) {
                Node local = new Node(localNodeId);
                if (titleId.equals(local.title))
                    return local;
            }
        } else {
            titleId = new Node(title).getId();
        }
        Node newLocalNode = new Node().setTitle(titleId).commit();
        addLocal(newLocalNode.getId()).commit();
        return newLocalNode;
    }

    public Node findLocal(byte[] title) {
        Long titleId = NodeStorage.getInstance().getData(title);
        if (titleId != null) {
            for (Long localNodeId : getLocal()) {
                Node local = new Node(localNodeId);
                if (titleId.equals(local.title))
                    return local;
            }
        }
        return null;
    }

    // TODO change path to byte[][]
    public Node loadPath(String path) {
        // TODO add path parents
        NodeStorage storage = NodeStorage.getInstance();
        // TODO create root node at startup
        String key = (id == null ? NodeStorage.ROOT_NODE_ID : id) + path;
        Long rootNodeId = storage.getKey(key);
        if (id == null) commit(); // create id if it is null
        if (rootNodeId == null)
            storage.putKey(key, id);
        else
            id = rootNodeId;
        return this;
    }

    @Override
    public byte[] build() {
        ArrayList<Long> links = new ArrayList<>();
        listLinks((linkType, linkId, singleValue) -> {
            // TODO change type of convert
            byte[] bytes = Bytes.fromLong(linkId);
            bytes[0] = type;
            long dataLink = Bytes.toLong(bytes);
            links.add(dataLink);
        });
        return Bytes.fromLongList(links);
    }

    @Override
    public void parse(byte[] data) {
        long[] links = Bytes.toLongArray(data);
        for (long dataLink : links) {
            byte[] bytes = Bytes.fromLong(dataLink);
            dataLink = Bytes.toLong(bytes);
            switch (bytes[0]) {
                case LinkType.VALUE:
                    value = dataLink;
                    break;
                case LinkType.SOURCE:
                    source = dataLink;
                    break;
                case LinkType.TITLE:
                    title = dataLink;
                    break;
                case LinkType.SET:
                    set = dataLink;
                    break;
                case LinkType.TRUE:
                    _true = dataLink;
                    break;
                case LinkType.ELSE:
                    _else = dataLink;
                    break;
                case LinkType.EXIT:
                    exit = dataLink;
                    break;
                case LinkType.WHILE:
                    _while = dataLink;
                    break;
                case LinkType.IF:
                    _if = dataLink;
                    break;
                case LinkType.PROTOTYPE:
                    prototype = dataLink;
                    break;
                case LinkType.BODY:
                    body = dataLink;
                    break;
                case LinkType.LOCAL:
                    local.add(dataLink);
                    break;
                case LinkType.PARAM:
                    param.add(dataLink);
                    break;
                case LinkType.NEXT:
                    next.add(dataLink);
                    break;
            }
        }
    }

    interface LinkListener {
        void get(byte linkType, Long linkId, boolean singleValue);
    }

    public void listLinks(LinkListener linkListener) {
        if (linkListener == null)
            return;
        if (value != null)
            linkListener.get(LinkType.VALUE, value, true);
        if (source != null)
            linkListener.get(LinkType.SOURCE, source, true);
        if (title != null)
            linkListener.get(LinkType.TITLE, title, true);
        if (set != null)
            linkListener.get(LinkType.SET, set, true);
        if (_true != null)
            linkListener.get(LinkType.TRUE, _true, true);
        if (_else != null)
            linkListener.get(LinkType.ELSE, _else, true);
        if (exit != null)
            linkListener.get(LinkType.EXIT, exit, true);
        if (_while != null)
            linkListener.get(LinkType.WHILE, _while, true);
        if (_if != null)
            linkListener.get(LinkType.IF, _if, true);
        if (prototype != null)
            linkListener.get(LinkType.PROTOTYPE, prototype, true);
        if (body != null)
            linkListener.get(LinkType.BODY, body, true);
        for (Long item : local)
            linkListener.get(LinkType.LOCAL, item, true);
        for (Long item : param)
            linkListener.get(LinkType.PARAM, item, true);
        for (Long item : next)
            linkListener.get(LinkType.NEXT, item, true);
        for (Long item : properties)
            linkListener.get(LinkType.PROTOTYPE, item, true);
    }

    public String toJson() {
        return Formatter.toJson(this);
    }

    public Long getId() {
        return id;
    }

    public Node setId(Long id) {
        this.id = id;
        return this;
    }

    public byte getType() {
        return type;
    }

    public Node setType(byte type) {
        this.type = type;
        return this;
    }

    public DataStream getData() {
        return data;
    }

    public Node setData(byte[] data) {
        NodeStorage storage = NodeStorage.getInstance();
        Long lastDataId = storage.getData(data);
        if (lastDataId != null) {
            setId(lastDataId);
        } else {
            long position = storage.add(data);
            NodeMetaCell nodeMetaCell = new NodeMetaCell();
            nodeMetaCell.type = getType();
            nodeMetaCell.start = position;
            nodeMetaCell.length = data.length;
            nodeMetaCell.accessKey = 0;
            // TODO add generate access key
            long id = storage.addMeta(nodeMetaCell);
            setId(id);
            storage.putData(data, id);
        }
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

    public Node setTitle(String title) {
        if (title != null)
            this.title = new Node(NodeType.STRING).setData(title.getBytes()).commit().getId();

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

    public int getFunctionId() {
        return functionId;
    }

    public Node setFunctionId(int functionId) {
        this.functionId = functionId;
        return this;
    }

    public ArrayList<Long> getLocal() {
        return local;
    }

    public ArrayList<Long> getParam() {
        return param;
    }

    public ArrayList<Long> getNext() {
        return next;
    }

    public Node addLocal(Long id) {
        local.add(id);
        return this;
    }

    public Node addParam(Long id) {
        param.add(id);
        return this;
    }

    protected Node addNext(Long id) {
        next.add(id);
        return this;
    }

    public Node addCell(Long id) {
        cell.add(id);
        return this;
    }

    public Node addProp(Long id) {
        properties.add(id);
        return this;
    }

    public Node removeLocal(Long id) {
        local.remove(id);
        return this;
    }
}
