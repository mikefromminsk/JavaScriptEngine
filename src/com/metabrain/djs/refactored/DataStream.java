package com.metabrain.djs.refactored;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.DiskManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DataStream {

    private static final long BUFFER_SIZE = NodeStorage.MAX_STORAGE_DATA_IN_DB * 500;
    public long start;
    public long length;
    private long currentPosition;
    private NodeStorage storage = NodeStorage.getInstance();
    private FileReader fileReader;

    public DataStream(long start, long length) {
        this.start = start;
        this.length = length;
        currentPosition = 0;
        if (length > NodeStorage.MAX_STORAGE_DATA_IN_DB) {
            try {
                fileReader = new FileReader(DiskManager.getInstance().getFileById(start));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    boolean hasNext() {
        boolean nextExist = currentPosition < length;
        if (!nextExist) currentPosition = 0;
        return nextExist;
    }

    private byte[] readFromDb(){
        byte[] data = storage.getData(start, currentPosition, (int) Math.min(BUFFER_SIZE, length));
        currentPosition += data.length;
        return data;
    }

    private char[] readFromFs(){
        try {
            char[] buf = new char[NodeStorage.MAX_STORAGE_DATA_IN_DB];
            int readiedChars = fileReader.read(buf);
            if  ((readiedChars) > 0) {
                if (readiedChars < NodeStorage.MAX_STORAGE_DATA_IN_DB)
                    buf = Arrays.copyOf(buf, readiedChars); // remove zero bytes
                currentPosition += readiedChars;
                return buf;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] readBytes() {
        if (length < NodeStorage.MAX_STORAGE_DATA_IN_DB)
            return readFromDb();
        else
            return Bytes.fromCharArray(readFromFs());
    }

    char[] readChars() {
        if (length < NodeStorage.MAX_STORAGE_DATA_IN_DB)
            return Bytes.toCharArray(readFromDb());
        else
            return readFromFs();
    }
}
