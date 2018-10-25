package com.metabrain.djs;

import java.util.ArrayList;

public class RunThread implements Runnable {

    private final static boolean SET_VALUE_FROM_VALUE = false;
    private final static boolean SET_VALUE_FROM_RETURN = true;

    private Long getDefaultPrototype(Byte nodeType) {
        return new Node(
                new Node()
                        .makePath("php/prototypes")
                        .getId())
                .findLocal(NodeType.toString(nodeType))
                .getId();
    }

    Long propCalledNodeId = null;

    private Long getProps(Long nodeId) {
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
            // TODO check getValue or getValueOrSelfId
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


    private void cloneObject(Long sourceNodeId, Long templateNodeId) {
        // TODO delete setType
        new Node(sourceNodeId).setType(NodeType.OBJECT).commit();
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
                .commit();
    }

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
    }

    void setValue(Long source, Long value, boolean setType, Long ths) {
        Byte setNodeType = value != null ? new Node(value).getType() : null;
        if (setNodeType == NodeType.VAR) {
            propCalledNodeId = ths;
            value = getProps(value);
            ths = propCalledNodeId;
            run(value, ths);
            if (new Node(value).getType() == NodeType.OBJECT) {
                cloneObject(source, value);
            } else {
                value = new Node(value).getValue();
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
            } else {
                cloneObject(source, value);
            }

        } else if (setType == SET_VALUE_FROM_VALUE && setNodeType == NodeType.FUNCTION) {
            // TODO nodetype.FUNCTION change posistion in code
            new Node(source).setBody(value);
        } else if (value != null) {
            run(value, ths);
            Long valueNodeId = new Node(value).getValue();
            new Node(source).setValue(valueNodeId).commit();
        } else {
            new Node(source).setValue(null).commit();
        }
    }

    //

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
            Caller.invoke(node, calledNodeId);
        }

        if (node.getSource() != null) {
            propCalledNodeId = calledNodeId;
            Long sourceNodeId = getProps(node.getSource());
            Long calledObjectFromSource = propCalledNodeId;
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
            if ("true".equals(new Node(new Node(node.getIf()).getValueOrSelfId()).getData().toString()))
                run(node.getTrue(), calledNodeId);
            else if (node.getElse() != null)
                run(node.getElse(), calledNodeId);
        }


        if (node.getWhile() != null && node.getIf() != null) {
            run(node.getIf(), calledNodeId);
            while ("true".equals(new Node(new Node(node.getIf()).getValueOrSelfId()).getData().toString())) {
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
