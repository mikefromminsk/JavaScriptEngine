package com.metabrain.djs;

import com.metabrain.gdb.*;

public class NodeStorage extends InfinityArray {

    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static InfinityFile dataStorage;
    private static NodeStorage instance;

    public NodeStorage(String infinityFileID) {
        super(infinityFileID);
    }

    public static NodeStorage getInstance() {
        if (instance == null || dataStorage == null) {
            instance = new NodeStorage(nodeStorageID);
            dataStorage = new InfinityFile(dataStorageID);
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
            long position = dataStorage.add(node.getData());
            nodeMetaCell.type = NodeType.DATA;
            nodeMetaCell.start = position;
            nodeMetaCell.length = node.getData().length;
            nodeMetaCell.accessKey = 0;
            // TODO add generate access key
            long id = meta.add(nodeMetaCell);
            node.setId(id);
        } else{
            long id = super.add(node);
            node.setId(id);
        }
    }

}
