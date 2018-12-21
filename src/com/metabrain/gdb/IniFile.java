package com.metabrain.gdb;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile {

    private File iniFile;
    //TODO add comments pattern
    private Pattern section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private Pattern keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    private Map<String, Map<String, String>> entries = new HashMap<>();

    public IniFile(File file) throws IOException {
        if (file == null)
            throw new NullPointerException();
        if (!file.isFile()) {
            if (!file.createNewFile())
                throw new FileNotFoundException();
        }
        iniFile = file;
        load();
    }

    public IniFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    public IniFile(File dir, String fileName) throws IOException {
        this(dir.getAbsolutePath() + "/" + fileName);
    }

    public void load() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(iniFile))) {
            String line;
            String section = null;
            while ((line = br.readLine()) != null) {
                Matcher m = this.section.matcher(line);
                if (m.matches()) {
                    section = m.group(1).trim();
                } else if (section != null) {
                    m = keyValue.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> kv = entries.get(section);
                        if (kv == null)
                            entries.put(section, kv = new HashMap<>());
                        kv.put(key, URLDecoder.decode(value));
                    }
                }
            }
        }
    }

    public void save() {
        try {
            iniFile.delete();
            iniFile.createNewFile();
            PrintWriter out = new PrintWriter(iniFile);
            for (String sectionKey : entries.keySet()) {
                out.println("[" + sectionKey + "]");
                Map<String, String> section = entries.get(sectionKey);
                for (String paramKey : section.keySet()) {
                    String paramValue = section.get(paramKey);
                    if (paramValue != null)
                        out.println(paramKey + "=" + URLEncoder.encode(paramValue));
                }
            }
            out.close();
        } catch (IOException ignored) {
        }
    }

    public Map<String, Map<String, String>> getEntries() {
        return entries;
    }

    public Map<String, String> getSection(String section) {
        return entries.get(section);
    }

    public String get(String section, String key, String defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null)
            return defaultValue;
        return kv.get(key);
    }

    public String get(String section, String key) {
        return get(section, key, null);
    }

    public void put(String sectionKey, String key, String value) {
        Map<String, String> section = entries.get(sectionKey);
        if (section != null) {
            section.put(key, value);
        } else {
            section = new HashMap<>();
            section.put(key, value);
            entries.put(sectionKey, section);
        }
        save();
    }

    public Long getLong(String section, String key, Long defaultValue) {
        String data = get(section, key);
        if (data == null)
            return defaultValue;
        return Long.valueOf(data);
    }

    public Integer getInt(String section, String key, Integer defaultValue) {
        String data = get(section, key);
        if (data == null)
            return defaultValue;
        return Integer.valueOf(data);
    }
}