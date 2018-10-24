package com.metabrain.djs;

import jdk.nashorn.internal.parser.TokenType;

public class Functions {
    public final static int EQ = 0;
    public final static int ADD = 1;
    public static final int SUB = 2;
    public static final int MUL = 3;
    public static final int DIV = 4;
    public static final int MOD = 5;
    public static final int AND = 6;
    public static final int OR = 7;
    public static final int LE = 8;
    public static final int EQ_STRICT = 9;
    public static final int NE = 10;
    public static final int NE_STRICT = 11;
    public static final int LT = 12 ;
    public static final int GE = 13;
    public static final int GT = 14;
    public static final int UNARY_MINUS = 15;

    static int fromTokenType(TokenType tokenType){
        // TODO add all tokens
        return EQ;
    }

    public static String toString(int functionId) {
        return null;
    }
}
