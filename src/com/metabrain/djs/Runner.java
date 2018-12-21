package com.metabrain.djs;


import com.metabrain.djs.node.DataStream;
import com.metabrain.djs.node.Node;
import com.metabrain.djs.node.NodeBuilder;
import com.metabrain.djs.node.NodeType;

public class Runner{

    private NodeBuilder builder = new NodeBuilder();
    private final static boolean SET_VALUE_FROM_VALUE = false;
    private final static boolean SET_VALUE_FROM_RETURN = true;

    private Node defaultPrototypes;// = builder.get(0L).getObject("defaultPrototypes");

    private Node getDefaultPrototype(Byte nodeType) {
        if (defaultPrototypes == null) {
            NodeBuilder nameBuilder = new NodeBuilder();
            NodeBuilder prototypeBuilder = new NodeBuilder();
            Node stringPrototype = prototypeBuilder.create()
                    .setTitle(nameBuilder.create(NodeType.STRING).setData(NodeType.STRING_NAME).commit())
                    .addLocal(builder.create(NodeType.NATIVE_FUNCTION)
                            .setTitle(nameBuilder.create(NodeType.STRING).setData(Caller.STRING_REVERCE_NAME).commit())
                            .setFunctionId(Caller.STRING_REVERCE)
                            .commit())
                    .addLocal(builder.create(NodeType.NATIVE_FUNCTION)
                            .setTitle(nameBuilder.create(NodeType.STRING).setData(Caller.STRING_TRIM_NAME).commit())
                            .setFunctionId(Caller.STRING_TRIM)
                            .commit())
                    .commit();
            defaultPrototypes = builder.create()
                    .addLocal(stringPrototype)
                    .commit();
            builder.get(0L).putObject("defaultPrototypes", defaultPrototypes);
        }
        return builder.set(defaultPrototypes).findLocal(NodeType.toString(nodeType));
    }

    private Node propCalledNode = null;

    private Node getProps(Node node) {
        Node startNode = node;
        if (builder.set(startNode).getSource() != null && builder.set(startNode).getPropertiesCount() > 0)
            node = builder.set(startNode).getSourceNode();
        boolean startFromThis = builder.set(startNode).getSource() == null && builder.set(node).getPropertiesCount() > 0;
        if (startFromThis && propCalledNode != null)
            startNode = propCalledNode;

        if (builder.set(startNode).getPropertiesCount() > 0)
            propCalledNode = node;

        for (int i = 0; i < builder.set(startNode).getPropertiesCount(); i++) {
            Node propNode = builder.set(startNode).getPropertyNode(i);
            run(propNode);
            // TODO check getValue or getValueOrSelf
            Node propNameNode = builder.set(propNode).getValueOrSelf();
            byte propType = propNameNode.type;
            if (propType == NodeType.STRING) {
                String propName = (String) builder.set(propNameNode).getData().getObject();
                // TODO delete duplicate with first call in this func
                Byte nodeType = null;
                if (builder.set(node).getValue() != null)
                    nodeType = builder.set(node).getValueNode().type;
                Node prototypeNode = builder.set(node).getPrototypeNode();
                if ("prototype".equals(propName)) {
                    if (prototypeNode != null) {
                        node = prototypeNode;
                    } else {// proto by node type
                        if (nodeType != null)
                            prototypeNode = getDefaultPrototype(nodeType);
                        if (prototypeNode != null) {
                            node = prototypeNode;
                        } else { // create proto
                            prototypeNode = builder.create().commit();
                            builder.set(node).setPrototype(prototypeNode).commit();
                            node = prototypeNode;
                        }
                    }
                    continue;
                } else { // otherwise
                    Node findPropNode = builder.set(node).findLocal(propName);
                    while (prototypeNode != null && findPropNode == null) {
                        findPropNode = builder.set(prototypeNode).findLocal(propName);
                        if (findPropNode == null)
                            prototypeNode = builder.set(prototypeNode).getPrototypeNode();
                    }
                    if (findPropNode != null) {
                        propCalledNode = node;
                        node = findPropNode;
                        continue;
                    } else {
                        if (nodeType != null)
                            prototypeNode = getDefaultPrototype(nodeType);
                        if (prototypeNode != null) {
                            findPropNode = builder.set(prototypeNode).findLocal(propName);
                            if (findPropNode != null) {
                                node = findPropNode;
                                continue;
                            }
                        } else {
                            Node varPrototype = getDefaultPrototype(NodeType.VAR);
                            if (varPrototype != null)
                                findPropNode = builder.set(varPrototype).findLocal(propName);
                            if (findPropNode != null) {
                                node = findPropNode;
                                continue;
                            }
                        }
                    }
                }
                propCalledNode = node;
                Node newField = builder.create().commit();
                Node titleData = builder.create(NodeType.STRING).setData(propName).commit();
                builder.set(newField).setTitle(titleData).commit();
                builder.set(node).addLocal(newField).commit();
                node = newField;
            } else if (propType == NodeType.NUMBER) {
                int index = (int) builder.set(propNameNode).getData().getObject();
                propCalledNode = node;
                node = builder.set(node).getCellNode(index);
            }
        }
        return node;
    }

    private void cloneObject(Node sourceNode, Node templateNode) {
        // TODO delete setType
        sourceNode.type = NodeType.OBJECT;
        builder.set(sourceNode).commit();
        run(templateNode, sourceNode);

        if (builder.set(templateNode).getNextCount() > 0) {
            // TODO delete duplicate
            //run(templateNodeId, sourceNodeId);
        } else {
            for (int i = 0; i < builder.set(templateNode).getLocalCount(); i++) {
                Node localNode = builder.set(templateNode).getLocalNode(i);
                if (builder.set(localNode).getTitle() != null) {
                    if (builder.set(localNode).getSource() != null)
                        run(localNode, sourceNode);
                    Node localValue = builder.set(localNode).getValueNode();
                    if (localValue != null) {
                        Node localTitle = builder.set(localNode).getTitleNode();
                        Node newLocalInSource = builder.create()
                                .setTitle(localTitle)
                                .setValue(localValue)
                                .commit();
                        builder.set(sourceNode)
                                .addLocal(newLocalInSource)
                                .commit();
                    }
                }
            }
        }
        builder.set(sourceNode)
                .setPrototype(templateNode)
                .commit();
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
            propCalledNode = ths;
            value = getProps(value);
            ths = propCalledNode;
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

    public void run(Long nodeId) {
        run(builder.get(nodeId).getNode());
    }

    public void run(Node node) {
        run(node, null);
    }

    private Node exitNode = null;

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
            propCalledNode = calledNodeId;
            Node sourceNode = getProps(builder.set(node).getSourceNode());
            Node calledObjectFromSource = propCalledNode;
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
