package com.metabrain.djs;

import java.util.ArrayList;

public class RunThread implements Runnable {

    private final static boolean SET_VALUE_FROM_VALUE = false;
    private final static boolean SET_VALUE_FROM_RETURN = true;

    private Long getProps(Long node, Long calledNodeId) {

        return node;
    }


    private void call(Node node, Long calledNodeId) {

    }


    private void cloneObject(Long source, Long value) {

    }

    // TODO add test to cloneArray testing
    private void cloneArray(Long sourceNodeId, Long templateNodeId) {
        Long newArrauNodeId = new Node(NodeType.ARRAY).commit().getId();
        Node templateLinks = new Node(templateNodeId);
        for (Long templateCellNodeId: templateLinks.getCell()){
            run(templateCellNodeId);
            new Node(newArrauNodeId).addCell(getValue(templateCellNodeId));
        }
        new Node(sourceNodeId).setValue(newArrauNodeId);
    }

    void setValue(Long source, Long value, boolean setType, Long ths) {
        Byte setNodeType = value != null ? new Node(value).getType() : null;
        if (setNodeType == NodeType.VAR) {
            value = getProps(value, ths);
            run(value, ths);
            if (new Node(value).getType() == NodeType.OBJECT) {
                cloneObject(source, value);
            } else {
                value = getValueOrNull(value);
                if (value != null) {
                    if (new Node(value).getType() == NodeType.OBJECT)
                        cloneObject(source, value);
                    else
                        new Node(source).setValue(value).commit();
                } else
                    new Node(source).setValue(null).commit();
            }
        } else if (setNodeType == NodeType.OBJECT) {
            if (new Node(source).getNext().size() != 0) {
                Long newNodeId = new Node().commit().getId();
                cloneObject(newNodeId, value);
                new Node(source).setValue(newNodeId).commit();
            }else{
                cloneObject(source, value);
            }

        }else if (setType == SET_VALUE_FROM_VALUE && setNodeType == NodeType.FUNCTION){
            // TODO nodetype.FUNCTION change posistion in code
            new Node(source).setBody(value);
        } else if (value != null){
            run(value, ths);
            Long valueNodeId = getValueOrNull(value);
            new Node(source).setValue(valueNodeId).commit();
        } else {
            new Node(source).setValue(null).commit();
        }
    }

    //
    Long getValueOrNull(Long nodeId) {
        return new Node(nodeId).getValue();
    }

    Long getValue(Long nodeId) {
        Long nodeValue = getValueOrNull(nodeId);
        if (nodeValue != null)
            return nodeValue;
        return nodeId;
    }

    Long exitNodeId = null;

    public void run(Long nodeId) {
        run(nodeId, null);
    }

    public synchronized void run(Long nodeId, Long calledNodeId) {
        Node node = new Node(nodeId);
        for (Long nextNodeId : node.getNext()) {
            run(nextNodeId);
            if (exitNodeId != null) {
                if (exitNodeId.equals(nodeId))
                    exitNodeId = null;
                break;
            }
        }

        if (node.getType() == NodeType.FUNCTION) {
            call(node, calledNodeId);
        }

        if (node.getSource() != null) {
            Long calledObjectFromSource = calledNodeId;
            // calledObjectFromSource bug
            Long sourceNodeId = getProps(node.getSource(), calledObjectFromSource);
            Long setNodeId = node.getSet();
            if (setNodeId != null) {
                setValue(sourceNodeId, setNodeId, SET_VALUE_FROM_VALUE, calledObjectFromSource);
            } else {
                Long methodNodeId = new Node(sourceNodeId).getBody();
                if (methodNodeId != null)
                    sourceNodeId = methodNodeId;
                if (node.getParam().size() != 0) {
                    // TODO execute market add to parser
                    boolean isExecute = node.getParam().get(0) == 0;
                    ArrayList<Long> sourceParams = new Node(sourceNodeId).getParam();
                    for (int i = 0; i < sourceParams.size(); i++)
                        setValue(sourceParams.get(i), isExecute ? null : node.getParam().get(i),
                                SET_VALUE_FROM_VALUE, calledObjectFromSource);
                    setValue(nodeId, sourceNodeId, SET_VALUE_FROM_RETURN, calledObjectFromSource);
                }
            }
        }

        if (node.getIf() != null && node.getTrue() != null) {
            run(node.getIf(), calledNodeId);
            if ("true".equals(new Node(getValue(node.getIf())).getData().toString()))
                run(node.getTrue(), calledNodeId);
            else if (node.getElse() != null)
                run(node.getElse(), calledNodeId);
        }


        if (node.getWhile() != null && node.getIf() != null) {
            run(node.getIf(), calledNodeId);
            while ("true".equals(new Node(getValue(node.getIf())).getData().toString())) {
                run(node.getWhile(), calledNodeId);
                if (exitNodeId != null) {
                    if (exitNodeId.equals(nodeId))
                        exitNodeId = null;
                    break;
                }
                run(node.getIf(), calledNodeId);
            }
        }


        if (node.getExit() != null)
            exitNodeId = node.getExit();
    }

    @Override
    public void run() {
    }
}
