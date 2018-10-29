package com.metabrain.djs.refactored;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.DiskManager;
import com.metabrain.gdb.InfinityArray;
import com.metabrain.gdb.InfinityFile;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class NodeStorage extends InfinityArray {

    public static final Long ROOT_NODE_ID = 0L;
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
    }

    public void transactionCommit() {
        for (Node commitNode : transactionNodes)
            if (commitNode.id == null)
                add(commitNode);
            else
                set(commitNode.id, commitNode);
        transactionNodes.clear();
    }

    public Node get(Long index) {
        Node node = nodesCache.get(index);
        if (node == null) {
            NodeMetaCell metaCell = (NodeMetaCell) getMeta(index);
            node = new Node();
            node.type = metaCell.type;
            if (metaCell.type == NodeType.STRING) {
                node.data = new DataStream(metaCell.start, metaCell.length);
            } else {
                byte[] readiedData = read(metaCell.start, metaCell.length);
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

    public void add(Node node) {
        if (node.type >= NodeType.VAR)
            node.id = super.add(node);
        else {
            if (node.externalData != null) {
                Reader in = new InputStreamReader(node.externalData);
                char[] buffer = new char[MAX_STORAGE_DATA_IN_DB];

                OutputStream outStream = null;

                while (true) {
                    int hash = 0;
                    int readiedBytes = -1;
                    try {
                        readiedBytes = in.read(buffer, 0, buffer.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (readiedBytes < 0) {
                        break;
                    } else if (outStream == null) {
                        if (readiedBytes == MAX_STORAGE_DATA_IN_DB) {
                            try {
                                outStream = new FileOutputStream(DiskManager.getInstance().newBigFile());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            node.id = super.add(node);
                        }
                    }
                    if (outStream != null) {
                        try {
                            byte[] bytes = Bytes.fromCharArray(buffer);
                            hash = Bytes.toInt(Crc16.getHash(hash, bytes));
                            // TODO !!!!!!!!!!!!!!
                            outStream.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public byte[] getData(long start, long offset, int length) {
        return dataStorage.read(start + offset, length);
    }
}
