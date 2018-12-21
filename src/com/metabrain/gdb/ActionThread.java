package com.metabrain.gdb;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class ActionThread implements Runnable {

    private static final boolean ACTION_READ = true;
    private static final boolean ACTION_WRITE = false;
    private int threadsWaiting = 0;
    private final Object syncObject = 1;
    private Map<RandomAccessFile, Map<Integer, CacheData>> cache = new HashMap<>();
    private List<CacheData> writeSequences = new LinkedList<>();
    public long maxCacheSize;
    public long cacheSize = 0;
    private PriorityQueue<CacheData> cachePriority = new PriorityQueue<>(Comparator.comparingLong(s -> s.saveTime));

    public ActionThread(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public void addToCache(Map<Integer, CacheData> cachedFile, CacheData newCache) {
        while (cacheSize != 0 && cacheSize + newCache.data.length > maxCacheSize) {
            CacheData writeCache = cachePriority.poll();
            // TODO why poll can return null
            if (writeCache != null) {
                if (writeCache.isUpdated) {
                    doAction(ACTION_WRITE, writeCache.file, writeCache.offset, writeCache.data);
                    cachedFile.remove(writeCache.offset, writeCache);
                    writeSequences.remove(writeCache);
                }
                cacheSize -= writeCache.data.length;
            }
        }
        cacheSize += newCache.data.length;
        cachePriority.remove(newCache);
        cachePriority.add(newCache);
        cachedFile.put(newCache.offset, newCache);
    }

    public byte[] read(RandomAccessFile file, int offset, int length) {
        byte[] data = new byte[length];

        Map<Integer, CacheData> cachedFile = cache.get(file);
        if (cachedFile != null) {
            CacheData cachedData = cachedFile.get(offset);
            if (cachedData != null) {
                int now = (int) (System.currentTimeMillis() / 1000L);
                if (now != cachedData.lastTime) {
                    cachedData.lastTime = now;
                    cachedData.readCount += 1;
                    cachedData.saveTime = cachedData.lastTime + cachedData.readCount;
                }
                System.arraycopy(cachedData.data, 0, data, 0, length);
                return data;
            }
        }
        threadsWaiting++;
        boolean success = doAction(ACTION_READ, file, offset, data);
        if (success) {
            if (cachedFile == null) {
                cachedFile = new HashMap<>();
                cache.put(file, cachedFile);
            }
            CacheData cachedData = new CacheData(false, file, offset, data);
            addToCache(cachedFile, cachedData);
            return data;
        }
        return null;
    }

    public void write(RandomAccessFile file, int offset, byte[] data) {
        if (data == null || data.length == 0)
            return;
        // TODO merge strings net more 512 byte in mainThread and max in achieveTread

        Map<Integer, CacheData> cachedFile = cache.get(file);
        if (cachedFile == null) {
            cachedFile = new HashMap<>();
            cache.put(file, cachedFile);
        }

        CacheData cachedData = cachedFile.get(offset);
        if (cachedData == null) {
            cachedData = new CacheData(true, file, offset, data);
            addToCache(cachedFile, cachedData);
        } else {
            cachedData.data = data;
            cachedData.isUpdated = true;
            writeSequences.remove(cachedData);
        }
        writeSequences.add(cachedData);

        synchronized (syncObject) {
            syncObject.notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (threadsWaiting == 0 && writeSequences.size() > 0) {
                // TODO find why action cab be null
                CacheData action = writeSequences.get(0); // NullPointerException
                if (action != null) {
                    boolean success = doAction(ACTION_WRITE, action.file, action.offset, action.data);
                    if (success) {
                        action.isUpdated = false;
                        writeSequences.remove(0);
                    }
                }
            } else {
                synchronized (syncObject) {
                    try {
                        syncObject.wait();
                    } catch (InterruptedException continueLoop) {
                    }
                }
            }
        }
    }

    synchronized boolean doAction(boolean actionType, RandomAccessFile file, int offset, byte[] data) {
        if (actionType == ACTION_READ) {
            try {
                file.seek(offset);
                file.read(data);
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                threadsWaiting--;
                if (threadsWaiting == 0) {
                    synchronized (syncObject) {
                        syncObject.notify();
                    }
                }
            }
        } else {
            try {
                file.seek(offset);
                file.write(data);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
