package com.metabrain.djs.refactored;

import com.metabrain.djs.refactored.node.Node;
import com.metabrain.djs.refactored.node.NodeBuilder;
import com.metabrain.djs.refactored.node.NodeType;
import jdk.nashorn.internal.parser.TokenType;

public class Caller {

    public final static int EQ = 0;
    public final static int ADD = 1;
    public static final int UNARY_MINUS = 15;
    private static NodeBuilder builder = new NodeBuilder();

    static int fromTokenType(TokenType tokenType) {
        switch (tokenType) {
            case EQ:
                return EQ;
            case ASSIGN_ADD:
                return ADD;
        }
        return EQ;
    }

    static Node trueValue = builder.create(NodeType.BOOL).setData("true").commit();
    static Node falseValue = builder.create(NodeType.BOOL).setData("false").commit();

    static void invoke(Node node, Node calledNodeId) {
        builder.set(node);

        Node left = builder.getParamNode(0);
        Node right = builder.getParamNode(1);

        Node leftValue = builder.set(left).getValueOrSelf();
        Node rightValue = builder.set(right).getValueOrSelf();

        Object leftObject = null;
        Object rightObject = null;

        if (leftValue.type < NodeType.VAR)
            leftObject = builder.set(leftValue).getData().getObject();
        if (rightValue.type < NodeType.VAR)
            rightObject = builder.set(rightValue).getData().getObject();

        switch (node.functionId) {
            case EQ:
                boolean isTrue = leftObject != null && leftObject.equals(rightObject);
                builder.set(node).setValue(isTrue ? trueValue : falseValue).commit();
                break;
            case ADD:
                if (leftObject instanceof String && rightObject instanceof String) {
                    String newString = (String) leftObject + (String) rightObject;
                    Node newStringNode = builder.create(NodeType.STRING).setData(newString).commit();
                    builder.set(node).setValue(newStringNode).commit();
                }
                break;
        }
    }
}
