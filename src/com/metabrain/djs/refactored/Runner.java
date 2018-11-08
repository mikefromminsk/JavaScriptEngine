package com.metabrain.djs.refactored;


import com.metabrain.djs.refactored.node.DataStream;
import com.metabrain.djs.refactored.node.Node;
import com.metabrain.djs.refactored.node.NodeBuilder;
import com.metabrain.djs.refactored.node.NodeType;

public class Runner {

    private NodeBuilder builder = new NodeBuilder();
    private final static boolean SET_VALUE_FROM_VALUE = false;
    private final static boolean SET_VALUE_FROM_RETURN = true;

    private Long getDefaultPrototype(Byte nodeType) {
        return null;
    }

    private Node propCalledNodeId = null;

    private Node getProps(Node node) {
        return node;
    }

    /*
    Node node = new Node(nodeId);
    ArrayList<Long> props = node.getProperties();
    if (node.getSource() != null && props.size() > 0)
        nodeId = node.getSource();
    boolean startFromThis = node.getSource() == null && props.size() > 0;
    if (startFromThis && propCalledNodeId != null)
        nodeId = propCalledNodeId;

    if (props.size() > 0)
        propCalledNodeId = nodeId;

    for (Long propNodeId : props) {
        run(propNodeId);
        // TODO check getValue or getValueOrSelf
        Node propNameNode = new Node(new Node(propNodeId).getValueOrSelfId());
        // TODO delete toString
        String propName = propNameNode.getData().toString();
        byte propType = propNameNode.getType();
        if (propType == NodeType.STRING) {
            // TODO delete duplicate with first call in this func
            node = new Node(nodeId);
            Byte nodeType = null;
            if (node.getValue() != null)
                nodeType = new Node(node.getValue()).getType();
            Long prototypeNodeId = node.getPrototype();
            if (propName.equals("prototype")) {
                if (prototypeNodeId != null) {
                    nodeId = prototypeNodeId;
                } else {// proto by node type
                    if (prototypeNodeId == null && nodeType == null)
                        prototypeNodeId = getDefaultPrototype(nodeType);
                    if (prototypeNodeId != null) {
                        nodeId = prototypeNodeId;
                    } else { // create proto
                        prototypeNodeId = new Node().commit().getId();
                        node.setPrototype(prototypeNodeId).commit();
                        nodeId = prototypeNodeId;
                    }
                }
                continue;
            } else { // otherwise
                Long findPropNodeId = null;
                while (prototypeNodeId != null && findPropNodeId == null) {
                    findPropNodeId = new Node(prototypeNodeId).findLocal(propName).getId();
                    if (findPropNodeId == null)
                        prototypeNodeId = new Node(prototypeNodeId).getPrototype();
                }
                if (findPropNodeId != null) {
                    propCalledNodeId = nodeId;
                    nodeId = findPropNodeId;
                    continue;
                } else {
                    if (nodeType != null)
                        prototypeNodeId = getDefaultPrototype(nodeType);
                    if (prototypeNodeId != null) {
                        // TODO NullPointerException
                        findPropNodeId = new Node(prototypeNodeId).findLocal(propName).getId();
                        if (findPropNodeId != null) {
                            nodeId = findPropNodeId;
                            continue;
                        }
                    } else {
                        Long varPrototype = getDefaultPrototype(NodeType.VAR);
                        findPropNodeId = new Node(varPrototype).findLocal(propName).getId();
                        if (findPropNodeId != null) {
                            nodeId = findPropNodeId;
                            continue;
                        }
                    }
                }
            }
            propCalledNodeId = nodeId;
            nodeId = new Node(nodeId).makeLocal(propName).getId();
        } else if (propType == NodeType.NUMBER) {
            propCalledNodeId = nodeId;
            int index = Integer.valueOf(propName);
            nodeId = new Node(nodeId).getCell().get(index);
        }
    }
    return nodeId;
}

*/
    private void cloneObject(Node sourceNode, Node templateNode) {
        // TODO delete setType
        /*new Node(sourceNodeId).setType(NodeType.OBJECT).commit();
        run(templateNodeId, sourceNodeId);
        Node templateNode = new Node(templateNodeId);

        if (templateNode.getNext().size() > 0) {
            // TODO delete duplicate
            run(templateNodeId, sourceNodeId);
        } else {
            for (Long localNodeId : templateNode.getLocal()) {
                Node localNode = new Node(localNodeId);
                if (localNode.getTitle() != null) {
                    run(localNodeId, sourceNodeId);
                    Long localValue = new Node(localNodeId).getValue();
                    if (localValue != null) {
                        new Node(sourceNodeId)
                                .addLocal(new Node()
                                        .setTitle(new Node(localNode.getTitle()).getData().toString())
                                        .setValue(localValue)
                                        .commit().getId()
                                ).commit();
                    }
                } else if (localNode.getValue() != null) {
                    new Node(sourceNodeId)
                            .addLocal(new Node()
                                    .setTitle(new Node(localNode.getTitle()).getData().toString())
                                    .setValue(localNode.getValue())
                                    .commit().getId()
                            ).commit();
                }
            }
        }
        new Node(sourceNodeId)
                .setPrototype(templateNodeId)
                .commit();*/
    }
/*
    // TODO add test to cloneArray testing
    private void cloneArray(Long sourceNodeId, Long templateNodeId) {
        Long newArrauNodeId = new Node(NodeType.ARRAY).commit().getId();
        Node templateLinks = new Node(templateNodeId);
        for (Long templateCellNodeId : templateLinks.getCell()) {
            run(templateCellNodeId);
            new Node(newArrauNodeId).addCell(
                    new Node(templateCellNodeId).getValueOrSelfId()
            ).commit();
        }
        new Node(sourceNodeId).setValue(newArrauNodeId);
    }*/

    void setValue(Node source, Node value, boolean setType, Node ths) {
        if (value == null) {
            builder.set(source).setValue(value).commit();
        } else if (value.type == NodeType.VAR) {
            propCalledNodeId = ths;
            value = getProps(value);
            ths = propCalledNodeId;
            run(value, ths);
            if (value.type == NodeType.OBJECT) {
                cloneObject(source, value);
            } else {
                value = builder.set(value).getValueNode();
                if (value != null && value.type == NodeType.OBJECT)
                    cloneObject(source, value);
                builder.set(source).setValue(value).commit();
            }
        } else if (value.type == NodeType.OBJECT) {
            if (builder.set(source).getNextCount() != 0) {
                Node newNodeId = builder.create().getNode();
                cloneObject(newNodeId, value);
                builder.set(source).setValue(newNodeId).commit();
            } else {
                cloneObject(source, value);
            }

        } else if (setType == SET_VALUE_FROM_VALUE && value.type == NodeType.FUNCTION) {
            // TODO nodetype.FUNCTION change posistion in code
            builder.set(source).setBody(value).commit();
        } else {
            run(value, ths);
            value = builder.set(value).getValueNode();
            builder.set(source).setValue(value).commit();
        }
    }

    private Node exitNode = null;

    public void run(Node node) {
        run(node, null);
    }

    private void run(Node node, Node calledNodeId) {
        for (int i = 0; i < builder.set(node).getNextCount(); i++) {
            run(builder.set(node).getNextNode(i));
            if (exitNode != null) {
                if (exitNode.equals(node))
                    exitNode = null;
                break;
            }
        }

        if (node.type == NodeType.NATIVE_FUNCTION) {
            if (builder.set(node).getParamCount() != 0) {
                for (int i = 0; i < builder.set(node).getParamCount(); i++) {
                    Node sourceParam = builder.set(node).getParamNode(i);
                    run(sourceParam, calledNodeId);
                }
            }
            Caller.invoke(node, calledNodeId);
        }

        if (builder.set(node).getSource() != null) {
            propCalledNodeId = calledNodeId;
            Node sourceNode = getProps(builder.set(node).getSourceNode());
            Node calledObjectFromSource = propCalledNodeId;
            Node setNode = builder.set(node).getSetNode();
            if (setNode != null) {
                setValue(sourceNode, setNode, SET_VALUE_FROM_VALUE, calledObjectFromSource);
            } else {
                Node bodyNode = builder.set(sourceNode).getBodyNode();
                if (bodyNode != null)
                    sourceNode = bodyNode;
                if (builder.set(node).getParamCount() != 0) {
                    // TODO execute market add to parser
                    boolean isExecute = builder.set(node).getParamNode(0).id == 0;
                    for (int i = 0; i < builder.set(sourceNode).getParamCount(); i++) {
                        Node sourceParam = builder.set(sourceNode).getParamNode(i);
                        Node nodeParam = builder.set(node).getParamNode(i);
                        setValue(sourceParam, isExecute ? null : nodeParam,
                                SET_VALUE_FROM_VALUE, calledObjectFromSource);
                    }
                    setValue(node, sourceNode, SET_VALUE_FROM_RETURN, calledObjectFromSource);
                }
            }
        }

        if (builder.set(node).getIf() != null && builder.set(node).getTrue() != null) {
            Node ifNode = builder.set(node).getIfNode();
            run(ifNode, calledNodeId);
            Node ifNodeData = builder.set(ifNode).getValueNode();
            DataStream dataStream = builder.set(ifNodeData).getData();
            if (ifNode.type == NodeType.BOOL && (Boolean) dataStream.getObject())
                run(builder.set(node).getTrueNode(), calledNodeId);
            else if (builder.set(node).getElse() != null)
                run(builder.set(node).getElseNode(), calledNodeId);
        }


        if (builder.set(node).getWhile() != null && builder.set(node).getIf() != null) {
            Node ifNode = builder.set(node).getIfNode();
            run(ifNode, calledNodeId);
            Node ifNodeData = builder.set(ifNode).getValueNode();
            DataStream dataStream = builder.set(ifNodeData).getData();
            while (ifNodeData.type == NodeType.BOOL && (Boolean) dataStream.getObject()) {
                run(builder.set(node).getWhileNode(), calledNodeId);
                if (exitNode != null) {
                    if (exitNode.equals(node))
                        exitNode = null;
                    break;
                }
                run(ifNode, calledNodeId);
                ifNodeData = builder.set(ifNode).getValueNode();
                dataStream = builder.set(ifNodeData).getData();
            }
        }

        if (builder.set(node).getExit() != null)
            exitNode = builder.set(node).getExitNode();
    }
}
