package com.metabrain.djs.refactored;

import com.metabrain.djs.refactored.node.Node;
import com.metabrain.djs.refactored.node.NodeBuilder;
import com.metabrain.djs.refactored.node.NodeType;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

public class JsParser {

    private NodeBuilder builder = new NodeBuilder();

    Node jsLine(Node module, jdk.nashorn.internal.ir.Node statement) {

        if (statement instanceof VarNode) {
            VarNode varNode = (VarNode) statement;
            Node variable = jsLine(module, varNode.getName());
            if (varNode.getInit() != null) {
                Node setLink = jsLine(module, varNode.getInit());
                Node node = builder.create()
                        .setSource(variable)
                        .setSet(setLink)
                        .commit();
                builder.get(module)
                        .addNext(node)
                        .commit();
            }
            return variable;
        }

        if (statement instanceof ForNode) {
            ForNode forNode = (ForNode) statement;
            // TODO problem with parsing for init
            Node forInitNode = builder.create().commit();
            builder.get(module).addLocal(forInitNode).commit();
            builder.get(forInitNode).addNext(jsLine(forInitNode, forNode.getInit()));
            Node forBodyNode = jsLine(forInitNode, forNode.getBody());
            Node forStartNode = builder.create()
                    .setWhile(forBodyNode)
                    .setIf(jsLine(forInitNode, forNode.getTest()))
                    .commit();
            Node forModify = jsLine(forInitNode, forNode.getModify());
            builder.get(forBodyNode).addNext(forModify).commit();
            builder.get(forInitNode).addNext(forStartNode);
            builder.get(module).removeLocal(forInitNode);
            return builder.get(forInitNode).removeLocal(forStartNode).commit(); // TODO remove this line
        }

        if (statement instanceof UnaryNode) {
            UnaryNode unaryNode = (UnaryNode) statement;
            // TODO add all unary ++a --a a++ a--
            if (unaryNode.tokenType() == TokenType.SUB)
                return builder.create(NodeType.FUNCTION)
                        .setFunctionId(Functions.UNARY_MINUS)
                        .addParam(jsLine(module, unaryNode.getExpression()))
                        .commit();
        }

        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expressionStatement = (ExpressionStatement) statement;
            return jsLine(module, expressionStatement.getExpression());
        }

        if (statement instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) statement;
            if (binaryNode.isAssignment()) {
                Node left = jsLine(module, binaryNode.lhs());
                Node right = jsLine(module, binaryNode.rhs());
                if (binaryNode.tokenType() == TokenType.ASSIGN) {
                    return builder.create()
                            .setSource(left)
                            .setSet(right)
                            .commit();
                } else {
                    Node functionCalc = builder.create(NodeType.FUNCTION)
                            .setFunctionId(Functions.fromTokenType(binaryNode.tokenType()))
                            .addParam(left)
                            .addParam(right)
                            .commit();
                    return builder.create()
                            .setSource(left)
                            .setSet(functionCalc)
                            .commit();
                }
            }
            if (binaryNode.isComparison()) {
                Node left = jsLine(module, binaryNode.lhs());
                Node right = jsLine(module, binaryNode.rhs());
                return builder.create(NodeType.FUNCTION)
                        .setFunctionId(Functions.fromTokenType(binaryNode.tokenType()))
                        .addParam(left)
                        .addParam(right)
                        .commit();
            }
        }


        if (statement instanceof Block) {
            Block block = (Block) statement;
            for (jdk.nashorn.internal.ir.Node line : block.getStatements()) {
                Node lineNode = jsLine(module, line);
                builder.get(module).addNext(lineNode);
            }
            return builder.get(module).commit();
        }

        if (statement instanceof IfNode) {
            IfNode ifStatement = (IfNode) statement;
            Node ifQuestionNode = jsLine(module, ifStatement.getTest());
            Node ifTrueNode = jsLine(module, ifStatement.getPass());
            Node ifElseNode = null;
            if (ifStatement.getFail() != null) {
                ifElseNode = jsLine(module, ifStatement.getFail());
                return builder.create()
                        .setIf(ifQuestionNode)
                        .setTrue(ifTrueNode)
                        .setElse(ifElseNode)
                        .commit();
            }

            if (statement instanceof FunctionNode) {
                FunctionNode functionNode = (FunctionNode) statement;
                Node func = builder.get(module).findLocal(functionNode.getName());
                if (func == null){
                    func = builder.create().commit();
                    Node titleData = builder.create(NodeType.STRING).setData(functionNode.getName()).commit();
                    builder.get(func).setTitle(titleData).commit();
                }
                for (IdentNode param : functionNode.getParameters()) {
                    Node titleData = builder.create(NodeType.STRING).setData(param.getName()).commit();
                    Node paramNode = builder.create().setTitle(titleData).commit();
                    builder.get(func).addParam(paramNode);
                }
                builder.get(func).commit();
                return jsLine(func, functionNode.getBody());
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

                Node ident = builder.get(module).findLocal(identNode.getName());
                if (ident == null){
                    ident = builder.create().commit();
                    Node titleData = builder.create(NodeType.STRING).setData(identNode.getName()).commit();
                    builder.get(ident).setTitle(titleData).commit();
                }
                return ident;
            }

            if (statement instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) statement;
                Node obj = builder.create().commit();
                builder.get(module).addLocal(obj).commit(); // TODO commit ?
                for (PropertyNode item : objectNode.getElements())
                    builder.get(obj).addProperty(jsLine(obj, item)); // TODO add 3th param to jsLine $add_to_local_path
                builder.get(module).removeLocal(obj).commit(); // TODO commit ?
                return builder.get(obj).commit();
            }

            if (statement instanceof PropertyNode) {
                PropertyNode propertyNode = (PropertyNode) statement;
                String key = "";
                if (propertyNode.getKey() instanceof LiteralNode) {
                    LiteralNode literalNode = (LiteralNode) propertyNode.getKey();
                    key = literalNode.getString();
                }
                if (propertyNode.getValue() instanceof FunctionCall) {
                    Node body = jsLine(module, propertyNode.getValue());
                    Node titleData = builder.create(NodeType.STRING).setData(key).commit();
                    return builder.get(module)
                            .addLocal(builder.create()
                                    .setTitle(titleData)
                                    .setBody(body)
                                    .getId()
                            ).commit();
                } else {
                    Node value = jsLine(module, propertyNode.getValue());
                    Node titleData = builder.create(NodeType.STRING).setData(key).commit();
                    builder.get(value).setTitle(titleData).commit();
                    return builder.get(module)
                            .addLocal(value)
                            .commit();
                }
            }

            if (statement instanceof LiteralNode) {
                LiteralNode literalNode = (LiteralNode) statement;

                if (literalNode instanceof LiteralNode.ArrayLiteralNode) {
                    LiteralNode.ArrayLiteralNode arrayLiteralNode = (LiteralNode.ArrayLiteralNode) literalNode;
                    Node arr = builder.create(NodeType.ARRAY).commit();
                    for (jdk.nashorn.internal.ir.Node item : arrayLiteralNode.getElementExpressions()) {
                        Node itemNode = jsLine(module, item);
                        builder.get(arr).addCell(itemNode);
                    }
                    return arr;
                }
                byte nodeType = NodeType.STRING;
                if (literalNode.isNumeric())
                    nodeType = NodeType.NUMBER;
                if (literalNode.isAlwaysTrue())
                    nodeType = NodeType.BOOL;
                if (literalNode.isAlwaysFalse())
                    nodeType = NodeType.BOOL;
                Node value = builder.create()
                        .setData(literalNode.getString().getBytes())
                        .commit();
                return builder.create()
                        .setValue(value)
                        .commit();
            }
            return null;
        }
        return null;
    }

    public Node parse(String sourceString) {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);
        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", sourceString);
        Parser parser = new Parser(context.getEnv(), source, errors);
        // TODO catch parse error
        return jsLine(builder.create().commit(), parser.parse().getBody());
    }


}
