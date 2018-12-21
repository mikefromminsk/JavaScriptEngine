package com.metabrain.djs;

import com.metabrain.djs.node.NodeStorage;
import com.metabrain.djs.node.NodeType;
import com.metabrain.djs.node.Node;
import com.metabrain.djs.node.NodeBuilder;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.ParserException;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.util.ArrayList;

public class Parser {

    private NodeBuilder builder = new NodeBuilder();
    ArrayList<Node> localStack = new ArrayList<>();

    Node findNodeInLocalStack(String name) {
        Long titleId = NodeStorage.getInstance().getDataId(name.getBytes());
        for (int i = localStack.size() - 1; i >= 0; i--) {
            Node node = localStack.get(i);
            Node findNode = builder.set(node).findLocal(titleId);
            if (findNode == null)
                findNode = builder.set(node).findParam(titleId);
            if (findNode != null)
                return findNode;
        }
        return null;
    }

    Node jsLine(Node module, jdk.nashorn.internal.ir.Node statement) {
        // TODO delete addToLocalStack true by default
        return jsLine(module, statement, true);
    }

    Node jsLine(Node module, jdk.nashorn.internal.ir.Node statement, boolean addToLocalStack) {
        try {
            if (addToLocalStack && module != null)
                localStack.add(module);

            if (statement instanceof VarNode) {
                VarNode varNode = (VarNode) statement;
                Node node = jsLine(module, varNode.getName());
                if (varNode.getInit() != null) {
                    Node setLink = jsLine(node, varNode.getInit());
                    node = builder.create().setSource(node).setSet(setLink).commit();
                }
                return node;
            }

            if (statement instanceof ForNode) {
                ForNode forStatement = (ForNode) statement;

                Node forNode = builder.create().commit();
                Node initBlockNode = jsLine(forNode, forStatement.getInit());
                builder.set(forNode).addNext(initBlockNode).commit();

                Node forBodyNode = jsLine(forNode, forStatement.getBody());
                Node forTestNode = jsLine(forNode, forStatement.getTest());
                Node forStartNode = builder.create().setWhile(forBodyNode).setIf(forTestNode).commit();

                Node forModifyNode = jsLine(forNode, forStatement.getModify());
                builder.set(forBodyNode).addNext(forModifyNode).commit();

                builder.set(forNode).addNext(forStartNode).commit();
                return forNode;
            }

            // i++ in for
            if (statement instanceof JoinPredecessorExpression) {
                JoinPredecessorExpression joinPredecessorExpression = (JoinPredecessorExpression) statement;
                return jsLine(module, joinPredecessorExpression.getExpression());
            }

            if (statement instanceof UnaryNode) {
                UnaryNode unaryNode = (UnaryNode) statement;
                // TODO add ++a --a
                TokenType tokenType = unaryNode.tokenType();
                if (tokenType == TokenType.INCPOSTFIX || tokenType == TokenType.DECPOSTFIX) {
                    Node variable = jsLine(module, unaryNode.getExpression());
                    Node func = builder.create(NodeType.NATIVE_FUNCTION)
                            .setFunctionId(Caller.fromTokenType(tokenType))
                            .addParam(variable)
                            .commit();
                    return builder.create()
                            .setSource(variable)
                            .setValue(variable) // important
                            .setSet(func)
                            .commit();
                } else if (tokenType.toString().equals("-")) {
                    Node expression = jsLine(module, unaryNode.getExpression());
                    return builder.create(NodeType.NATIVE_FUNCTION)
                            .setFunctionId(Caller.UNARY_MINUS)
                            .addParam(expression)
                            .commit();
                } else {
                    return jsLine(module, unaryNode.getExpression());
                }
            }

            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) statement;
                return jsLine(module, expressionStatement.getExpression());
            }

            if (statement instanceof BinaryNode) {
                BinaryNode binaryNode = (BinaryNode) statement;
                Node left = jsLine(module, binaryNode.lhs());
                Node right = jsLine(module, binaryNode.rhs());
                if (binaryNode.isAssignment()) {
                    if (binaryNode.tokenType() == TokenType.ASSIGN_ADD ||
                            binaryNode.tokenType() == TokenType.ASSIGN_SUB ||
                            binaryNode.tokenType() == TokenType.ASSIGN_MUL ||
                            binaryNode.tokenType() == TokenType.ASSIGN_DIV)
                        right = builder.create(NodeType.NATIVE_FUNCTION)
                                .setFunctionId(Caller.fromTokenType(binaryNode.tokenType()))
                                .addParam(left)
                                .addParam(right)
                                .commit();
                    return builder.create()
                            .setSource(left)
                            .setSet(right)
                            .commit();
                } else {
                    return builder.create(NodeType.NATIVE_FUNCTION)
                            .setFunctionId(Caller.fromTokenType(binaryNode.tokenType()))
                            .addParam(left)
                            .addParam(right)
                            .commit();
                }
            }


            /*if (statement instanceof IndexNode) {
                IndexNode index = (IndexNode) statement;
                Node base = jsLine(module, index.getBase());
                Node objectNode = builder.create().setSource(base).commit();

                if (index.getIndex() instanceof LiteralNode || index.getIndex() instanceof IdentNode) {
                    Node indexNode = jsLine(module, index.getIndex());
                    builder.set(objectNode).addProperty(indexNode).commit();
                }
                return objectNode;
            }*/

            if (statement instanceof AccessNode) {
                AccessNode index = (AccessNode) statement;
                Node base;

                if (index.getBase() instanceof IdentNode) {
                    if ("this".equals(((IdentNode) index.getBase()).getName()))
                        base = builder.create().commit();
                    else {
                        base = jsLine(module, index.getBase());
                        base = builder.create().setSource(base).commit();
                    }
                } else
                    base = jsLine(module, index.getBase());
                Node propertyNode = builder.create(NodeType.STRING).setData(index.getProperty()).commit();
                builder.set(base).addProperty(propertyNode).commit();
                return base;
            }

            if (statement instanceof Block) {
                Block block = (Block) statement;
                Node blockNode = builder.create().commit();
                for (jdk.nashorn.internal.ir.Node line : block.getStatements()) {
                    Node lineNode = jsLine(blockNode, line);
                    //if (lineNode.next == null)
                    builder.set(blockNode).addNext(lineNode);
                }
                return builder.set(blockNode).commit();
            }

            if (statement instanceof IfNode) {
                IfNode ifStatement = (IfNode) statement;
                Node ifQuestionNode = jsLine(module, ifStatement.getTest());
                Node ifTrueNode = jsLine(module, ifStatement.getPass());
                Node ifElseNode;
                if (ifStatement.getFail() != null) {
                    ifElseNode = jsLine(module, ifStatement.getFail());
                    return builder.create()
                            .setIf(ifQuestionNode)
                            .setTrue(ifTrueNode)
                            .setElse(ifElseNode)
                            .commit();
                }
            }

            if (statement instanceof FunctionNode) {
                FunctionNode function = (FunctionNode) statement;
                Node functionNode = module;
                functionNode.type = NodeType.FUNCTION;
                for (IdentNode param : function.getParameters()) {
                    Node titleData = builder.create(NodeType.STRING).setData(param.getName()).commit();
                    Node paramNode = builder.create().setTitle(titleData).commit();
                    builder.set(functionNode).addParam(paramNode);
                }
                builder.set(functionNode).removeAllNext().commit();
                for (jdk.nashorn.internal.ir.Node line : function.getBody().getStatements()) {
                    Node lineNode = jsLine(functionNode, line);
                    builder.set(functionNode).addNext(lineNode);
                }
                return builder.set(functionNode).commit();
            }

            if (statement instanceof PropertyNode) {
                PropertyNode propertyNode = (PropertyNode) statement;
                String key = "";
                if (propertyNode.getKey() instanceof LiteralNode) {
                    LiteralNode literalNode = (LiteralNode) propertyNode.getKey();
                    key = literalNode.getString();
                }
                if (propertyNode.getKey() instanceof IdentNode) {
                    IdentNode identNode = (IdentNode) propertyNode.getKey();
                    key = identNode.getName();
                }
                if (propertyNode.getValue() instanceof FunctionNode) {
                    Node functionBody = builder.create(NodeType.FUNCTION).commit();
                    Node body = jsLine(functionBody, propertyNode.getValue());
                    Node titleData = builder.create(NodeType.STRING).setData(key).commit();
                    Node functionHead = builder.create().setTitle(titleData).setBody(body).commit();
                    return builder.set(module).addLocal(functionHead).commit();
                } else {
                    Node value = jsLine(module, propertyNode.getValue());
                    Node titleData = builder.create(NodeType.STRING).setData(key).commit();
                    builder.set(value).setTitle(titleData).commit();
                    return builder.set(module).addLocal(value).commit();
                }
            }

            if (statement instanceof ReturnNode) {
                ReturnNode returnNode = (ReturnNode) statement;
                Node setNode = jsLine(module, returnNode.getExpression());
                return builder.create()
                        .setSource(module)
                        .setSet(setNode)
                        .setExit(module)
                        .commit();
            }

            if (statement instanceof IdentNode) {
                IdentNode identNode = (IdentNode) statement;

                Node ident = findNodeInLocalStack(identNode.getName());
                if (ident == null) {
                    ident = builder.create().commit();
                    Node titleData = builder.create(NodeType.STRING).setData(identNode.getName()).commit();
                    builder.set(ident).setTitle(titleData).commit();
                    builder.set(module).addLocal(ident).commit();
                }
                return ident;
            }

            if (statement instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) statement;
                Node obj = builder.create(NodeType.OBJECT).commit();
                for (PropertyNode property : objectNode.getElements())
                    jsLine(obj, property);
                return obj;
            }


            if (statement instanceof CallNode) { // TODO NewExpression
                CallNode call = (CallNode) statement;
                Node callNode = builder.create().commit();
                if (call.getArgs().size() > 0) {
                    for (jdk.nashorn.internal.ir.Node arg : call.getArgs()) {
                        Node argNode = jsLine(module, arg);
                        builder.set(callNode).addParam(argNode);
                    }
                } else {
                    builder.set(callNode).addParam(0L);
                }
                Node sourceFunc = jsLine(module, call.getFunction());
                return builder.set(callNode)
                        .setSource(sourceFunc)
                        .commit();
            }


            if (statement instanceof LiteralNode) {
                LiteralNode literalNode = (LiteralNode) statement;

                if (literalNode instanceof LiteralNode.ArrayLiteralNode) {
                    LiteralNode.ArrayLiteralNode arrayLiteralNode = (LiteralNode.ArrayLiteralNode) literalNode;
                    Node arr = builder.create(NodeType.ARRAY).commit();
                    for (jdk.nashorn.internal.ir.Node item : arrayLiteralNode.getElementExpressions()) {
                        Node itemNode = jsLine(module, item);
                        builder.set(arr).addCell(itemNode);
                    }
                    return arr;
                }
                byte nodeType = NodeType.BOOL;
                if (literalNode.isNumeric())
                    nodeType = NodeType.NUMBER;
                else if (literalNode.isString())
                    nodeType = NodeType.STRING;
                Node value = builder.create(nodeType)
                        .setData(literalNode.getString())
                        .commit();
                return builder.create()
                        .setValue(value)
                        .commit();
            }
            return null;
        } finally {
            if (addToLocalStack)
                localStack.remove(module);
        }

    }

    // this function solve 05testFunctionVariables.js problem with var init
    private boolean isOperation(BinaryNode binaryNode) {
        try {
            return binaryNode.isLocal();
        } catch (NullPointerException e) {
            return true;
        }
    }

    public Node parse(Node module, String sourceString) throws ParserException {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);
        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", sourceString);
        jdk.nashorn.internal.parser.Parser parser = new jdk.nashorn.internal.parser.Parser(context.getEnv(), source, errors);
        jdk.nashorn.internal.ir.Node rootParserNode = parser.parse();
        if (rootParserNode == null) {
            throw new ParserException("");
        } else {
            if (module == null)
                module = builder.create().commit();

            Node node = jsLine(module, rootParserNode);

            NodeBuilder builder = new NodeBuilder();
            Node sourceCodeDataNode = builder.create(NodeType.STRING).setData(sourceString).commit();

            Node sourceCodeTitle = builder.create(NodeType.STRING).setData("source_code").commit();
            Node sourceCode = builder.set(module).findStyle(sourceCodeTitle.id);
            if (sourceCode == null) {
                Node sourceCodeNode = builder.create(NodeType.VAR).setTitle(sourceCodeTitle).setValue(sourceCodeDataNode).commit();
                builder.set(module).addStyle(sourceCodeNode).commit();
            } else {
                builder.set(sourceCode).setValue(sourceCodeDataNode).commit();
            }
            return node;
        }
    }


}
