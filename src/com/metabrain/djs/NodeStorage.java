package com.metabrain.djs;

import com.metabrain.gdb.*;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeStorage extends InfinityArray {

    // TODO add root node when create database
    public static final Long ROOT_NODE_ID = 0L;
    public static final int MAX_STORAGE_DATA_IN_DB = 2048;
    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static final String hashStorageID = "hash";
    private static final String keyValueStorageID = "kvdb";
    private static final String accountStorageID = "account";

    private static ArrayList commitNodes;
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
            commitNodes = new ArrayList();
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
        // TODO change Tree set to byte[]
        // TODO change Crc16 set to byte[]
        return getData(new String(title));
    }

    public Long getData(String title) {
        if (title != null)
            return dataHashTree.get(title, Crc16.getHashBytes(title));
        return null;
    }

    public void putData(String title, Long nodeId) {
        if (title != null && nodeId != null)
            dataHashTree.put(title, Crc16.getHashBytes(title), nodeId);
    }

    public void putData(byte[] title, Long nodeId) {
        // TODO change Tree set to byte[]
        // TODO change Crc16 set to byte[]
        putData(new String(title), nodeId);
    }

    public Long getKey(String key) {
        long value = keyValueStorage.get(key, Crc16.getHashBytes(key));
        return value == Long.MAX_VALUE ? null : value;
    }

    public void putKey(String key, Long nodeId) {
        keyValueStorage.put(key, Crc16.getHashBytes(key), nodeId);
    }

    public long addMeta(NodeMetaCell nodeMetaCell) {
        return meta.add(nodeMetaCell);
    }


    boolean login(String login, String password) {
        byte[] loginHash = Crc16.getHashBytes(login);
        long readiedPasswordHash = accountStorage.get(login, loginHash);
        return readiedPasswordHash == Bytes.toLong(Crc16.getHashBytes(password));
    }

    boolean registration(String login, String password) {
        byte[] loginHash = Crc16.getHashBytes(login);
        boolean loginExist = accountStorage.get(login, loginHash) != Long.MAX_VALUE;
        if (!loginExist) {
            byte[] passwordHash = Crc16.getHashBytes(password);
            accountStorage.put(login, loginHash, Bytes.toLong(passwordHash));
            return true;
        }
        return false;
    }

    boolean changePassword(String login, String password, String newPassword) {
        byte[] loginHash = Crc16.getHashBytes(login);
        long readiedPasswordHash = accountStorage.get(login, loginHash);
        if (readiedPasswordHash == Bytes.toLong(Crc16.getHashBytes(password))) {
            byte[] newPasswordHash = Crc16.getHashBytes(newPassword);
            accountStorage.put(login, newPasswordHash, Bytes.toLong(newPasswordHash));
            return true;
        }
        return false;
    }
}
