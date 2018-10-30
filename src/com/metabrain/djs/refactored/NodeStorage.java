package com.metabrain.djs.refactored;

import com.metabrain.gdb.*;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class NodeStorage extends InfinityArray {

    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static final String hashStorageID = "hash";
    private static final String keyValueStorageID = "kvdb";
    private static final String accountStorageID = "account";

    public static final int MAX_STORAGE_DATA_IN_DB = 2048;
    private static final int MAX_TRANSACTION_CACHE_NODE_COUNT = 10;
    private static ArrayList<Node> transactionNodes;
    private static InfinityFile dataStorage;
    private static NodeStorage instance;
    private static Tree dataHashTree;
    private static Tree keyValueStorage;
    private static Tree accountStorage;
    private static Map<Long, Node> nodesCache = new TreeMap<>();


    public NodeStorage(String infinityFileID) {
        super(infinityFileID);
        if (meta.fileData.sumFilesSize == 0)
            initStorage();
    }

    private void initStorage() {
        Node root = new Node();
        root.type = NodeType.THREAD;
        add(root);
        transactionCommit();
    }

    public static NodeStorage getInstance() {
        if (instance == null || dataStorage == null) {
            transactionNodes = new ArrayList<>();
            instance = new NodeStorage(nodeStorageID);
            dataStorage = new InfinityFile(dataStorageID);
            dataHashTree = new Tree(hashStorageID);
            keyValueStorage = new Tree(keyValueStorageID);
            accountStorage = new Tree(accountStorageID);
        }
        return instance;
    }

    public void addToTransaction(Node node) {
        if (transactionNodes.size() >= MAX_TRANSACTION_CACHE_NODE_COUNT)
            transactionCommit();
        transactionNodes.add(node);
        node.isSaved = true;
        nodesCache.put(node.id, node);
    }

    public void transactionCommit() {
        for (Node commitNode : transactionNodes) {
            if (commitNode.id == null)
                add(commitNode);
            else
                set(commitNode.id, commitNode);
            commitNode.isSaved = false;
        }
        transactionNodes.clear();
    }

    @Override
    public MetaCell initMeta() {
        return new NodeMetaCell();
    }

    public Node get(Long index) {
        Node node = nodesCache.get(index);
        if (node == null) {
            NodeMetaCell metaCell = (NodeMetaCell) getMeta(index);
            node = new Node();
            node.id = index;
            node.type = metaCell.type;
            if (metaCell.type == NodeType.STRING) {
                node.data = new DataStream(metaCell.start, metaCell.length);
            } else {
                byte[] readiedData = read(metaCell.start, metaCell.length);
                if (readiedData == null)
                    return null;
                decodeData(readiedData, metaCell.accessKey);
                node.parse(readiedData);
            }
            nodesCache.put(index, node);
        }
        return node;
    }

    public void set(long index, Node node) {
        if (node.type >= NodeType.VAR)
            super.set(index, node);
        // else {data is not mutable}
    }

    private static Random random = new Random();

    public void add(Node node) {
        if (node.type >= NodeType.VAR)
            node.id = super.add(node);
        else {
            if (node.externalData != null) {
                Reader in = new InputStreamReader(node.externalData);
                byte[] hashKey = null;
                int hash = 0;
                OutputStream outStream = null;

                NodeMetaCell nodeMetaCell = new NodeMetaCell();
                nodeMetaCell.type = node.type;
                nodeMetaCell.length = 0;
                try {
                    char[] buffer = new char[MAX_STORAGE_DATA_IN_DB];
                    byte[] bytes;
                    int readiedBytes;
                    while ((readiedBytes = in.read(buffer)) != -1) {
                        bytes = Bytes.fromCharArray(buffer);
                        hash = Crc16.getHash(hash, bytes);
                        nodeMetaCell.length += bytes.length;
                        if (outStream == null) {
                            hashKey = bytes;
                            if (readiedBytes == MAX_STORAGE_DATA_IN_DB) {
                                nodeMetaCell.start = random.nextLong();
                                File file = DiskManager.getInstance().getFileById(nodeMetaCell.start);
                                if (!file.exists())
                                    file.createNewFile();
                                outStream = new FileOutputStream(file, false);
                                outStream.write(bytes);
                            } else {
                                nodeMetaCell.start = dataStorage.add(bytes);
                            }
                        } else {
                            outStream.write(bytes);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                node.id = meta.add(nodeMetaCell);
                dataHashTree.put(hashKey, Crc16.hashToBytes(hash), node.id);
            }
        }
    }

    public byte[] getSmallData(long start, long offset, int length) {
        return dataStorage.read(start + offset, length);
    }

    public void clearCache() {
        nodesCache.clear();
    }
}
