package com.metabrain.gdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class DiskManager {

    private static DiskManager instance;
    public IniFile properties = null;
    public ActionThread mainThread;

    public final static File dbDir = new File("out/SimpleGraphDB");
    public final static File propertiesFile = new File(dbDir, "settings.properties");
    public Integer partSize;
    public Integer cacheSize;

    public static DiskManager getInstance() {
        if (instance == null) {
            try {
                instance = new DiskManager();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public final static String SECTION = "_manager_";
    public final static String PART_SIZE_KEY = "part_size";
    public final static Integer PART_SIZE_DEFAULT = 4096;
    public final static String CACHE_SIZE_KEY = "cache_size";
    public final static Integer CACHE_SIZE_DEFAULT = 4096;

    private DiskManager() throws FileNotFoundException {
        // TODO double save settings
        // TODO problem when DiskManager init without saving data rights

        if (!dbDir.isDirectory())
            if (!dbDir.mkdirs())
                throw new FileNotFoundException();
        try {
            properties = new IniFile(propertiesFile);
            if (properties.getSection(SECTION) == null)
                initProperties(properties);
            loadProperties(properties);

            mainThread = new ActionThread(cacheSize);
            Thread thread = new Thread(mainThread);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties(IniFile properties) {
        this.partSize = properties.getInt(SECTION, PART_SIZE_KEY, PART_SIZE_DEFAULT);
        this.cacheSize = properties.getInt(SECTION, CACHE_SIZE_KEY, CACHE_SIZE_DEFAULT);
    }

    private void initProperties(IniFile properties) {
        properties.put(SECTION, PART_SIZE_KEY, "" + PART_SIZE_DEFAULT);
        properties.put(SECTION, CACHE_SIZE_KEY, "" + CACHE_SIZE_DEFAULT);
    }

    public void addDisk(String rootDir) {
    }

    public void diskTesting() {
        // TODO testing of all disks
    }

    public File getFileById(long fileId) {
        return new File(dbDir, fileId + ".data");
    }
}
