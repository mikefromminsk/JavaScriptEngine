package com.metabrain.djs.refactored;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.DiskManager;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DataStream {

    // TODO set buffer size > MAX_STORAGE_DATA_IN_DB
    private static final int BUFFER_SIZE = NodeStorage.MAX_STORAGE_DATA_IN_DB;
    public long start;
    public long length;
    private long currentPosition;
    private NodeStorage storage = NodeStorage.getInstance();
    private FileReader fileReader;

    public DataStream(long start, long length) {
        this.start = start;
        this.length = length;
        currentPosition = 0;
    }

    boolean hasNext() {
        boolean nextExist = currentPosition < length;
        if (!nextExist) currentPosition = 0;
        return nextExist;
    }

    private byte[] readFromDb() {
        byte[] data = storage.getData(start, currentPosition, (int) Math.min(BUFFER_SIZE, length));
        currentPosition += data.length;
        return data;
    }

    private char[] readFromFs() {
        try {
            if (fileReader == null)
                fileReader = new FileReader(DiskManager.getInstance().getFileById(start));
            char[] buf = new char[BUFFER_SIZE];
            int readiedChars = fileReader.read(buf);
            if ((readiedChars) > 0) {
                if (readiedChars < BUFFER_SIZE)
                    buf = Arrays.copyOf(buf, readiedChars); // remove zero bytes
                currentPosition += readiedChars;
                return buf;
            }
            if (currentPosition == length) {
                fileReader.close();
                fileReader = null;
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
