package com.metabrain.gdb.tree;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityConstArrayCell;

import java.util.Arrays;

public class TreeNode implements InfinityConstArrayCell {


    public final static int MASK_SIZE = 4;
    public final static int LINKS_COUNT = MASK_SIZE * MASK_SIZE;
    public final static int LINKS_SIZE = LINKS_COUNT * Long.BYTES;
    public final static int SIZE = MASK_SIZE + LINKS_SIZE;

    byte[] mask;
    long[] links;

    public TreeNode() {
    }

    public TreeNode(byte[] data) {
        parse(data);
    }

    public TreeNode(byte[] mask, long[] links) {
        this.mask = mask;
        this.links = links;
    }

    @Override
    public void parse(byte[] data) {
        this.mask = Arrays.copyOfRange(data, 0, MASK_SIZE);
        byte[] linksArr = Arrays.copyOfRange(data, MASK_SIZE, MASK_SIZE + LINKS_SIZE);
        this.links = Bytes.toLongArray(linksArr);
    }

    @Override
    public byte[] build() {
        byte[] data = new byte[MASK_SIZE + LINKS_SIZE];
        System.arraycopy(mask, 0, data, 0, MASK_SIZE);
        System.arraycopy(Bytes.fromLongArray(links), 0, data, MASK_SIZE, LINKS_SIZE);
        return data;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

}
