package com.metabrain.djs;

import com.metabrain.gdb.*;
import com.metabrain.gdb.tree.CRC16;
import com.metabrain.gdb.tree.Tree;

import java.util.Map;

public class NodeStorage extends InfinityArray {

    public static final Long ROOT_NODE_ID = 1L;
    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static final String hashStorageID = "hash";
    private static final String dbStorageID = "db";

    private static InfinityFile dataStorage;
    private static NodeStorage instance;
    private static Tree hashTree;
    private static Tree dbRootStorage;
    private static Map<Long, Tree> dbDataStoreage;


    public NodeStorage(String infinityFileID) {
        super(infinityFileID);
    }

    public static NodeStorage getInstance() {
        if (instance == null || dataStorage == null) {
            instance = new NodeStorage(nodeStorageID);
            dataStorage = new InfinityFile(dataStorageID);
            hashTree = new Tree(hashStorageID);
        }
        return instance;
    }

    private static NodeMetaCell nodeMetaCell = new NodeMetaCell();

    @Override
    public MetaCell getMeta(long index) {
        return (MetaCell) meta.get(index, nodeMetaCell);
    }

    public Node get(long index, Node node) {
        NodeMetaCell metaCell = (NodeMetaCell) getMeta(index);
        node.setType((byte) metaCell.type);
        if (metaCell.type == NodeType.DATA) {
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
        if (node.getType() != NodeType.DATA)
            super.set(index, node);
    }

    public void add(Node node) {
        if (node.getId() == NodeType.DATA) {

            Long lastDataId = getData(node.getData());
            if (lastDataId != null){
                node.setId(lastDataId);
            }else{
                long position = dataStorage.add(node.getData());
                nodeMetaCell.type = NodeType.DATA;
                nodeMetaCell.start = position;
                nodeMetaCell.length = node.getData().length;
                nodeMetaCell.accessKey = 0;
                // TODO add generate access key
                long id = meta.add(nodeMetaCell);
                node.setId(id);
                putData(node.getData(), id);
            }
        } else {
            long id = super.add(node);
            node.setId(id);
        }
    }

    public Long getData(byte[] title) {
        // TODO change Tree get to byte[]
        // TODO change CRC16 get to byte[]
        return getData(new String(title));
    }

    public Long getData(String title) {
        if (title != null)
            return hashTree.get(title, CRC16.getHash(title));
        return null;
    }

    public void putData(String title, Long nodeId) {
        if (title != null && nodeId != null)
            hashTree.put(title, CRC16.getHash(title), nodeId);
    }

    public void putData(byte[] title, Long nodeId) {
        // TODO change Tree get to byte[]
        // TODO change CRC16 get to byte[]
        putData(new String(title) , nodeId);
    }
}
