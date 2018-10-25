package com.metabrain.djs;

public class Caller {
    static void invoke(Node node, Long calledNodeId){
        if (node.getType() == NodeType.FUNCTION){
            switch (node.getFunctionId()){
                case Functions.EQ:
                    Long leftNodeId = node.getParam().get(0);
                    Long rightNodeId = node.getParam().get(1);
                    if (leftNodeId != null && rightNodeId != null){
                        Node leftNode = new Node(new Node(leftNodeId).getValueOrSelfId());
                        Node rightNode = new Node(new Node(rightNodeId).getValueOrSelfId());
                        if (leftNode.getType() == NodeType.BOOL &&
                                rightNode.getType() == NodeType.BOOL){
                            Boolean leftValue = leftNode.getData().getBoolean();
                            Boolean rightValue = rightNode.getData().getBoolean();
                            if (leftValue != null && leftValue.equals(rightValue))
                                node.setValue(new Node("true".getBytes()).getId()).commit();
                            else
                                node.setValue(new Node("false".getBytes()).getId()).commit();
                        }
                    }
            }
        }
    }
}
