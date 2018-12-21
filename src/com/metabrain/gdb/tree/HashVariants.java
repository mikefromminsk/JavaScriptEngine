package com.metabrain.gdb.tree;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityConstArrayCell;

import java.util.ArrayList;
import java.util.Arrays;

public class HashVariants implements InfinityConstArrayCell {

    public byte[] mask;
    public ArrayList<Hash> hashes  = new ArrayList<>();

    public HashVariants() {
    }

    public HashVariants(byte[] mask, Hash hash) {
        this.mask = mask;
        this.hashes.add(hash);
    }

    @Override
    public void parse(byte[] data) {
        hashes.clear();
        this.mask = Arrays.copyOfRange(data, 0, TreeNode.MASK_SIZE);
        int hashVariantsCount = (data.length - TreeNode.MASK_SIZE) / Hash.SIZE;
        for (int i = 0; i < hashVariantsCount; i++) {
            int startHashData = TreeNode.MASK_SIZE + i * Hash.SIZE;
            long[] hashData = Bytes.toLongArray(Arrays.copyOfRange(data, startHashData, startHashData + Hash.SIZE));
            hashes.add(new Hash(hashData[0], hashData[1], hashData[2]));
        }
    }

    @Override
    public byte[] build() {
        byte[] data = new byte[TreeNode.MASK_SIZE + hashes.size() * Hash.SIZE];
        System.arraycopy(mask, 0, data, 0, TreeNode.MASK_SIZE);
        for (int i = 0; i < hashes.size(); i++)
            System.arraycopy(hashes.get(i).build(), 0, data, TreeNode.MASK_SIZE + i * Hash.SIZE, Hash.SIZE);
        return data;
    }

    @Override
    public int getSize() {
        return TreeNode.MASK_SIZE + hashes.size() * Hash.SIZE;
    }
}
