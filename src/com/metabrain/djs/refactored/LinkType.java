package com.metabrain.djs.refactored;

public class LinkType {
    public static final byte VALUE = 0;
    private static final String VALUE_NAME = "value";
    public static final byte SOURCE = 1;
    private static final String SOURCE_NAME = "source";
    public static final byte TITLE = 2;
    private static final String TITLE_NAME = "title";
    public static final byte SET = 3;
    private static final String SET_NAME = "set";
    public static final byte TRUE = 4;
    private static final String TRUE_NAME = "true";
    public static final byte ELSE = 5;
    private static final String ELSE_NAME = "else";
    public static final byte EXIT = 6;
    private static final String EXIT_NAME = "exit";
    public static final byte WHILE = 7;
    private static final String WHILE_NAME = "while";
    public static final byte IF = 8;
    private static final String IF_NAME = "if";
    public static final byte PROTOTYPE = 9;
    private static final String PROTOTYPE_NAME = "prototype";
    public static final byte BODY = 10;
    private static final String BODY_NAME = "body";
    public static final byte LOCAL = 11;
    private static final String LOCAL_NAME = "local";
    public static final byte PARAM = 12;
    private static final String PARAM_NAME = "param";
    public static final byte NEXT = 13;
    private static final String NEXT_NAME = "next";
    public static final byte CELL = 14;
    private static final String CELL_NAME = "cell";
    public static final byte FUNCTION_ID = 15;
    private static final String FUNCTION_ID_NAME = "function_id";

    public static String toString(byte linkType) {
        switch (linkType){
            case VALUE: return VALUE_NAME;
            case SOURCE: return SOURCE_NAME;
            case TITLE: return TITLE_NAME;
            case SET: return SET_NAME;
            case TRUE: return TRUE_NAME;
            case ELSE: return ELSE_NAME;
            case EXIT: return EXIT_NAME;
            case WHILE: return WHILE_NAME;
            case IF: return IF_NAME;
            case PROTOTYPE: return PROTOTYPE_NAME;
            case BODY: return BODY_NAME;
            case LOCAL: return LOCAL_NAME;
            case PARAM: return PARAM_NAME;
            case NEXT: return NEXT_NAME;
            case CELL: return CELL_NAME;
            case FUNCTION_ID: return FUNCTION_ID_NAME;
        }
        return null;
    }
}
