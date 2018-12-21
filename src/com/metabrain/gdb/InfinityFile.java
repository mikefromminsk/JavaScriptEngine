package com.metabrain.gdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class InfinityFile {

    public final static String INFINITY_FILE_PART_PREFIX = "part";
    public long partSize;
    String infinityFileID;
    public InfinityFileData fileData;
    ActionThread mainThread;
    public static Map<String, InfinityFileData> infinityFileCache = new HashMap<>();

    public InfinityFile(String infinityFileID) {
        this.infinityFileID = infinityFileID;
        DiskManager diskManager = DiskManager.getInstance();
        this.mainThread = diskManager.mainThread;
        this.partSize = diskManager.partSize;

        fileData = infinityFileCache.get(infinityFileID);
        if (fileData == null) {
            fileData = new InfinityFileData();
            Map<String, String> fileSettings = diskManager.properties.getSection(infinityFileID);
            if (fileSettings != null)
                for (int i = 0; fileSettings.containsKey(INFINITY_FILE_PART_PREFIX + i); i++) {
                    try {
                        String partFileName = fileSettings.get(INFINITY_FILE_PART_PREFIX + i);
                        RandomAccessFile partRandomAccessFile = new RandomAccessFile(partFileName, "rw");
                        fileData.files.add(partRandomAccessFile);
                        fileData.sumFilesSize += partRandomAccessFile.length();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }

    }

    RandomAccessFile getFile(int index) {
        // TODO create file in action thread
        if (index == fileData.files.size()) {
            try {
                DiskManager diskManager = DiskManager.getInstance();
                String partName = INFINITY_FILE_PART_PREFIX + index;
                String newFileName = infinityFileID + "." + partName;
                File partFile = new File(diskManager.dbDir, newFileName);
                diskManager.properties.put(infinityFileID, partName, partFile.getAbsolutePath());
                RandomAccessFile partRandomAccessFile = new RandomAccessFile(partFile, "rw");
                fileData.files.add(partRandomAccessFile);
                return partRandomAccessFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return fileData.files.get(index);
    }

    public byte[] read(long start, long length) {
        long end = start + length;
        if (end > fileData.sumFilesSize)
            return null;

        int startFileIndex = (int) (start / partSize);
        int endFileIndex = (int) (end / partSize);
        if (startFileIndex == endFileIndex) {
            RandomAccessFile readingFile = getFile(startFileIndex);
            int startInFile = (int) (start % partSize);
            return mainThread.read(readingFile, startInFile, (int) length);
        } else {
            RandomAccessFile firstFile = getFile(startFileIndex);
            RandomAccessFile secondFile = getFile(endFileIndex);
            int lengthInSecondFile = (int) (end % partSize);
            int lengthInFirstFile = (int) (length - lengthInSecondFile);
            int startInFirstFile = (int) (start % partSize);
            int startInSecondFile = 0;
            byte[] dataFromFirstFile = mainThread.read(firstFile, startInFirstFile, lengthInFirstFile);
            byte[] dataFromSecondFile = mainThread.read(secondFile, startInSecondFile, lengthInSecondFile);
            return Bytes.concat(dataFromFirstFile, dataFromSecondFile);
        }
    }

    public void write(long start, byte[] data) {
        long length = data.length;
        long end = start + length;
        if (start > fileData.sumFilesSize)
            return;

        int startFileIndex = (int) (start / partSize);
        int endFileIndex = (int) (end / partSize);

        RandomAccessFile firstWriteFile = getFile(startFileIndex);
        RandomAccessFile secondWriteFile = getFile(endFileIndex);

        if (start == fileData.sumFilesSize)
            fileData.sumFilesSize += data.length;

        if (startFileIndex == endFileIndex) {
            int startInFile = (int) (start - startFileIndex * partSize);
            mainThread.write(firstWriteFile, startInFile, data);
            // TODO archive thread
        } else {
            int lengthInSecondFile = (int) (end % partSize);
            int lengthInFirstFile = (int) (length - lengthInSecondFile);
            int startInFirstFile = (int) (start % partSize);
            int startInSecondFile = 0;
            byte[] dataToFirstFile = new byte[lengthInFirstFile];
            byte[] dataToSecondFile = new byte[lengthInSecondFile];
            mainThread.write(firstWriteFile, startInFirstFile, dataToFirstFile);
            mainThread.write(secondWriteFile, startInSecondFile, dataToSecondFile);
            // TODO archive thread
        }
    }

    public long add(byte[] data) {
        long lastSumFileSize = fileData.sumFilesSize;
        write(lastSumFileSize, data);
        return lastSumFileSize;
    }
}
