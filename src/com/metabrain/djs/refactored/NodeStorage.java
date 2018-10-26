package com.metabrain.djs.refactored;

import com.metabrain.djs.NodeMetaCell;
import com.metabrain.djs.NodeType;
import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityArray;
import com.metabrain.gdb.InfinityFile;
import com.metabrain.gdb.MetaCell;
import com.metabrain.gdb.tree.Crc16;
import com.metabrain.gdb.tree.Tree;

import java.util.ArrayList;

public class NodeStorage extends InfinityArray {

    public static final Long ROOT_NODE_ID = 0L;
    private static final String nodeStorageID = "node";
    private static final String dataStorageID = "data";
    private static final String hashStorageID = "hash";
    private static final String keyValueStorageID = "kvdb";
    private static final String accountStorageID = "account";

    private static ArrayList<Node> commitNodes;
    private static InfinityFile dataStorage;
    private static NodeStorage instance;
    private static Tree dataHashTree;
    private static Tree keyValueStorage;
    private static Tree accountStorage;


    public NodeStorage(String infinityFileID) {
        super(infinityFileID);
        if (meta.fileData.sumFilesSize == 0)
            initStorage();
    }

    private void initStorage() {
        Node.create(NodeType.THREAD).commit();
        transactionCommit();
    }

    public static NodeStorage getInstance() {
        if (instance == null || dataStorage == null) {
            commitNodes = new ArrayList<>();
            instance = new NodeStorage(nodeStorageID);
            dataStorage = new InfinityFile(dataStorageID);
            dataHashTree = new Tree(hashStorageID);
            keyValueStorage = new Tree(keyValueStorageID);
            accountStorage = new Tree(accountStorageID);
        }
        return instance;
    }

    public void addCommitNode(Node node){
        commitNodes.add(node);
    }

    public void transactionCommit(){
        for (Node commitNode: commitNodes)
            if (commitNode.id == null)
                    add(this);
                else
                    set(id, this);
        commitNodes.clear();
    }

}
