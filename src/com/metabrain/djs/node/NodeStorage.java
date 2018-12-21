package com.metabrain.djs.node;

import com.metabrain.gdb.*;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

import java.io.*;
import java.util.*;

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
            if (metaCell.type < NodeType.VAR) {
                node.data = new DataStream(metaCell.type, metaCell.start, metaCell.length);
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
        if (node.type >= NodeType.VAR) {
            node.id = super.add(node);
        } else {
            try {
                if (node.externalData != null) {
                    Reader in = new InputStreamReader(node.externalData);
                    byte[] hashKey = null;
                    int hash = 0;
                    OutputStream outStream = null;

                    NodeMetaCell nodeMetaCell = new NodeMetaCell();
                    nodeMetaCell.type = node.type;
                    nodeMetaCell.length = 0;
                    char[] buffer = new char[MAX_STORAGE_DATA_IN_DB];
                    byte[] bytes = null;
                    int readiedBytes;
                    File file = null;
                    while ((readiedBytes = in.read(buffer)) != -1) {
                        bytes = Bytes.fromCharArray(Arrays.copyOfRange(buffer, 0, readiedBytes));
                        hash = Crc16.getHash(hash, bytes);
                        nodeMetaCell.length += readiedBytes;
                        if (outStream == null) {
                            hashKey = bytes;
                            if (readiedBytes == MAX_STORAGE_DATA_IN_DB) {
                                nodeMetaCell.start = random.nextLong();
                                file = DiskManager.getInstance().getFileById(nodeMetaCell.start);
                                if (!file.exists())
                                    file.createNewFile();
                                outStream = new FileOutputStream(file, false);
                                outStream.write(bytes);
                            }
                        } else {
                            outStream.write(bytes);
                        }
                    }
                    if (outStream != null)
                        outStream.close();
                    long prevNodeId = dataHashTree.get(hashKey, Crc16.hashToBytes(hash));
                    if (prevNodeId == Long.MAX_VALUE) {
                        if (nodeMetaCell.length < MAX_STORAGE_DATA_IN_DB) {
                            nodeMetaCell.start = dataStorage.add(bytes);
                        }
                        node.id = meta.add(nodeMetaCell);
                        node.data = new DataStream(nodeMetaCell.type, nodeMetaCell.start, nodeMetaCell.length);
                        node.externalData = null;
                        dataHashTree.put(hashKey, Crc16.hashToBytes(hash), node.id);
                    }else{
                        if (nodeMetaCell.length >= MAX_STORAGE_DATA_IN_DB)
                            file.delete(); // delete read file buffer
                        nodeMetaCell = (NodeMetaCell) meta.get(prevNodeId, nodeMetaCell);
                        node.id = prevNodeId;
                        node.data = new DataStream(nodeMetaCell.type, nodeMetaCell.start, nodeMetaCell.length);
                        node.externalData = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO change getData function name
    public byte[] getData(long start, long offset, int length) {
        return dataStorage.read(start + offset, length);
    }

    public void clearCache() {
        nodesCache.clear();
    }

    public Long getDataId(byte[] title) {
        if (title != null)
            return dataHashTree.get(title, Crc16.getHashBytes(title));
        return null;
    }

    public Long put(byte[] title) {
        if (title != null)
            return dataHashTree.get(title, Crc16.getHashBytes(title));
        return null;
    }

    public Node getObject(String key) {
        long nodeId = keyValueStorage.get(key, Crc16.getHashBytes(key));
        if (nodeId != Long.MAX_VALUE)
            return get(nodeId);
        return null;
    }

    public void putObject(String key, Node value) {
        keyValueStorage.put(key, Crc16.getHashBytes(key), value.id);
    }
}
