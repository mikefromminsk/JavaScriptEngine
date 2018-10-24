package com.metabrain.djs;

public class DataStream {

    int BUFFER_SIZE = NodeStorage.MAX_STORAGE_DATA_IN_DB * 500;
    int position;
    int length;
    int index;

    void seekToStart(){

    }

    void hasNext(){

    }

    byte[] read(){
        return null;
    }

    byte[] readAll(){
        return null;
    }

    public long size() {
        return 22;
    }

    public String readString() {
        return null;
    }
}
