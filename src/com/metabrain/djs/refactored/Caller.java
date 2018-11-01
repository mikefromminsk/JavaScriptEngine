package com.metabrain.djs.refactored;

import com.metabrain.djs.refactored.node.Node;
import com.metabrain.djs.refactored.node.NodeBuilder;
import com.metabrain.djs.refactored.node.NodeType;
import jdk.nashorn.internal.parser.TokenType;

public class Caller {

    public final static int EQ = 0;
    public static final int UNARY_MINUS = 15;
    private static NodeBuilder builder = new NodeBuilder();

    static int fromTokenType(TokenType tokenType){
        switch (tokenType){
            case EQ: return EQ;
        }
        return EQ;
    }

    static Node trueValue = builder.create().setData("true").commit();
    static Node falseValue = builder.create().setData("false").commit();

    static void invoke(Node node, Node calledNodeId){
        if (node.type == NodeType.FUNCTION){
            switch (node.functionId){
                case EQ:
                    builder.set(node);
                    Node left = builder.getParamNode(0);
                    Node right = builder.getParamNode(1);
                    if (left != null && right != null){
                        Node leftValue = builder.set(left).getValueOrSelf();
                        Node rightValue =  builder.set(right).getValueOrSelf();
                        if (leftValue.type < NodeType.VAR && rightValue.type < NodeType.VAR){
                            Object leftObject = builder.set(leftValue).getData().getObject();
                            Object rightObject = builder.set(rightValue).getData().getObject();
                            if (leftObject != null && leftObject.equals(rightObject))
                                builder.set(node).setValue(trueValue).commit();
                        }
                    }
                    builder.set(node).setValue(falseValue).commit();
            }
        }
    }
}
