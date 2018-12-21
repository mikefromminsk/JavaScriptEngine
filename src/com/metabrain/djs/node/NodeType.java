package com.metabrain.djs.node;

public class  NodeType {
    // todo change String to char[]
    public static final byte STRING = -3;
    public static final String STRING_NAME = "string";
    public static final byte NUMBER = -2;
    public static final String NUMBER_NAME = "number";
    public static final byte BOOL = -1;
    public static final String BOOL_NAME = "bool";
    public static final byte VAR = 0;
    public static final String VAR_NAME = "var";
    public static final byte ARRAY = 1;
    public static final String ARRAY_NAME = "array";
    public static final byte OBJECT = 2;
    public static final String OBJECT_NAME = "object";
    public static final byte NATIVE_FUNCTION = 3;
    public static final String NATIVE_FUNCTION_NAME = "native_function";
    public static final byte THREAD = 4;
    public static final String THREAD_NAME = "thread";
    public static final byte FUNCTION = 5; // TODO delete
    public static final String FUNCTION_NAME = "function";

    public static String toString(byte type) {
        switch (type){
            case STRING: return STRING_NAME;
            case NUMBER: return NUMBER_NAME;
            case BOOL: return BOOL_NAME;
            case VAR: return VAR_NAME;
            case ARRAY: return ARRAY_NAME;
            case OBJECT: return OBJECT_NAME;
            case NATIVE_FUNCTION: return NATIVE_FUNCTION_NAME;
            case THREAD: return THREAD_NAME;
            case FUNCTION: return FUNCTION_NAME;
        }
        return null;
    }

    public static byte fromString(String str) {
        switch (str){
            case STRING_NAME: return STRING;
            case NUMBER_NAME: return NUMBER;
            case BOOL_NAME: return BOOL;
            case VAR_NAME: return VAR;
            case ARRAY_NAME: return ARRAY;
            case OBJECT_NAME: return OBJECT;
            case NATIVE_FUNCTION_NAME: return NATIVE_FUNCTION;
            case THREAD_NAME: return THREAD;
            case FUNCTION_NAME: return FUNCTION;
            default: return -1;
        }
    }
}
