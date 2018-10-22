package com.metabrain.djs;

import com.metabrain.gdb.*;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

public class NodeStorage extends InfinityArray {

    // TODO add root node when create database
    public static final Long ROOT_NODE_ID = 0L;
    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static final String hashStorageID = "hash";
    private static final String keyValueStorageID = "kvdb";
    private static final String accountStorageID = "account";

    private static InfinityFile dataStorage;
    private static NodeStorage instance;
    private static Tree dataHashTree;
    private static Tree keyValueStorage;
    private static Tree accountStorage;


    public NodeStorage(String infinityFileID) {
        super(infinityFileID);
    }

    public static NodeStorage getInstance() {
        if (instance == null || dataStorage == null) {
            instance = new NodeStorage(nodeStorageID);
            dataStorage = new InfinityFile(dataStorageID);
            dataHashTree = new Tree(hashStorageID);
            keyValueStorage = new Tree(keyValueStorageID);
            accountStorage = new Tree(accountStorageID);
        }
        return instance;
    }

    private static NodeMetaCell nodeMetaCell = new NodeMetaCell();

    @Override
    public MetaCell initMeta() {
        return new NodeMetaCell();
    }

    public Node get(long index, Node node) {
        NodeMetaCell metaCell = (NodeMetaCell) getMeta(index);
        node.setType((byte) metaCell.type);
        if (metaCell.type == NodeType.STRING) {
            byte[] readiedData = dataStorage.read(metaCell.start, metaCell.length);
            node.setData(readiedData);
            // todo add decode
        } else {
            byte[] readiedData = read(metaCell.start, metaCell.length);
            decodeData(readiedData, metaCell.accessKey);
            node.parse(readiedData);
        }
        return node;
    }

    public void set(long index, Node node) {
        if (node.getType() != NodeType.STRING &&
                node.getType() != NodeType.NUMBER &&
                node.getType() != NodeType.BOOL)
            super.set(index, node);
    }

    public void add(Node node) {
        long id = super.add(node);
        node.setId(id);
    }

    public Long getData(byte[] title) {
        // TODO change Tree get to byte[]
        // TODO change Crc16 get to byte[]
        return getData(new String(title));
    }

    public Long getData(String title) {
        if (title != null)
            return dataHashTree.get(title, Crc16.getHash(title));
        return null;
    }

    public void putData(String title, Long nodeId) {
        if (title != null && nodeId != null)
            dataHashTree.put(title, Crc16.getHash(title), nodeId);
    }

    public void putData(byte[] title, Long nodeId) {
        // TODO change Tree get to byte[]
        // TODO change Crc16 get to byte[]
        putData(new String(title), nodeId);
    }

    public Long getKey(String key) {
        long value = keyValueStorage.get(key, Crc16.getHash(key));
        return value == Long.MAX_VALUE ? null : value;
    }

    public void putKey(String key, Long nodeId) {
        keyValueStorage.put(key, Crc16.getHash(key), nodeId);
    }

    public long addMeta(NodeMetaCell nodeMetaCell) {
        return meta.add(nodeMetaCell);
    }

    boolean login(String login, String password) {
        byte[] loginHash = Crc16.getHash(login);
        long readiedPasswordHash = accountStorage.get(login, loginHash);
        return readiedPasswordHash == Bytes.toLong(Crc16.getHash(password));
    }

    boolean registration(String login, String password) {
        byte[] loginHash = Crc16.getHash(login);
        boolean loginExist = accountStorage.get(login, loginHash) != Long.MAX_VALUE;
        if (!loginExist) {
            byte[] passwordHash = Crc16.getHash(password);
            accountStorage.put(login, loginHash, Bytes.toLong(passwordHash));
            return true;
        }
        return false;
    }

    boolean changePassword(String login, String password, String newPassword) {
        byte[] loginHash = Crc16.getHash(login);
        long readiedPasswordHash = accountStorage.get(login, loginHash);
        if (readiedPasswordHash == Bytes.toLong(Crc16.getHash(password))) {
            byte[] newPasswordHash = Crc16.getHash(newPassword);
            accountStorage.put(login, newPasswordHash, Bytes.toLong(newPasswordHash));
            return true;
        }
        return false;
    }
}
