package com.metabrain.djs;

import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import static jdk.nashorn.internal.parser.TokenType.*;
import static jdk.nashorn.internal.parser.TokenType.EQ;

public class JsParser {

    Node jsLine(Node module, jdk.nashorn.internal.ir.Node statement) {

        if (statement instanceof VarNode) {
            VarNode varNode = (VarNode) statement;
            Node variable = jsLine(module, varNode.getName());
            if (varNode.getInit() != null) {
                Node setLink = jsLine(module, varNode.getInit());
                module.addNext(new Node()
                        .setSource(variable.getId())
                        .setSet(setLink.getId())
                        .commit().getId()
                ).commit();
            }
            return variable;
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
                    return new Node()
                            .setSource(left.getId())
                            .setSet(right.getId())
                            .commit();
                } else {
                    Node functionCalc = new Node(NodeType.FUNCTION)
                            .setFunctionId(Functions.fromTokenType(binaryNode.tokenType()))
                            .addParam(left.getId())
                            .addParam(right.getId())
                            .commit();
                    return new Node()
                            .setSource(left.getId())
                            .setSet(functionCalc.getId())
                            .commit();
                }
            }
            if (binaryNode.isComparison()) {
                return new Node(NodeType.FUNCTION)
                        .setFunctionId(Functions.fromTokenType(binaryNode.tokenType()))
                        .addParam(jsLine(module, binaryNode.lhs()).getId())
                        .addParam(jsLine(module, binaryNode.rhs()).getId())
                        .commit();
            }
        }


        if (statement instanceof Block) {
            Block block = (Block) statement;
            for (jdk.nashorn.internal.ir.Node line : block.getStatements())
                module.addNext(jsLine(module, line).getId());
            return module.commit();
        }

        if (statement instanceof IfNode) {
            IfNode ifNode = (IfNode) statement;
            Node ifs = new Node()
                    .setIf(jsLine(module, ifNode.getTest()).getId())
                    .setTrue(jsLine(module, ifNode.getPass()).getId());
            if (ifNode.getFail() != null)
                ifs.setElse(jsLine(module, ifNode.getFail()).getId());
            return ifs.commit();
        }

        if (statement instanceof FunctionNode) {
            FunctionNode functionNode = (FunctionNode) statement;
            Node func = module.makeLocal(functionNode.getName());
            for (IdentNode param : functionNode.getParameters())
                func.addParam(new Node().setTitle(param.getName()).commit().getId());
            func.commit();
            return jsLine(func, functionNode.getBody());
        }

        if (statement instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) statement;
            return new Node()
                    .setSource(module.getId())
                    .setSet(jsLine(module, returnNode.getExpression()).getId())
                    .setExit(module.getId())
                    .commit();
        }

        if (statement instanceof IdentNode) {
            IdentNode identNode = (IdentNode) statement;
            return module.makeLocal(identNode.getName().getBytes());
        }

        if (statement instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) statement;
            Node obj = new Node().commit();
            module.addLocal(obj.getId()).commit(); // TODO commit ?
            for (PropertyNode item : objectNode.getElements())
                obj.addProp(jsLine(obj, item).getId()); // TODO add 3th param to jsLine $add_to_local_path
            module.removeLocal(obj.getId()).commit(); // TODO commit ?
            return obj.commit();
        }

        if (statement instanceof PropertyNode) {
            PropertyNode propertyNode = (PropertyNode) statement;

            String key = "";
            if (propertyNode.getKey() instanceof LiteralNode) {
                LiteralNode literalNode = (LiteralNode) propertyNode.getKey();
                key = literalNode.getString();
            }
            if (propertyNode.getValue() instanceof FunctionCall) {
                return module
                        .addLocal(new Node()
                                .setTitle(key)
                                .setBody(jsLine(module, propertyNode.getValue()).getId())
                                .commit().getId()
                        ).commit();
            } else {
                return module
                        .addLocal(jsLine(module, propertyNode.getValue()).setTitle(key).commit().getId())
                        .commit();
            }
        }

        if (statement instanceof LiteralNode) {
            LiteralNode literalNode = (LiteralNode) statement;

            if (literalNode instanceof LiteralNode.ArrayLiteralNode) {
                LiteralNode.ArrayLiteralNode arrayLiteralNode = (LiteralNode.ArrayLiteralNode) literalNode;
                Node arr = new Node(NodeType.ARRAY);
                for (jdk.nashorn.internal.ir.Node item : arrayLiteralNode.getElementExpressions())
                    arr.addCell(jsLine(module, item).getId());
                return arr;
            }
            byte nodeType = NodeType.STRING;
            if (literalNode.isString())
                nodeType = NodeType.STRING;
            if (literalNode.isNumeric())
                nodeType = NodeType.NUMBER;
            if (literalNode.isAlwaysTrue())
                nodeType = NodeType.BOOL;
            if (literalNode.isAlwaysFalse())
                nodeType = NodeType.BOOL;
            return new Node()
                    .setValue(new Node(nodeType).setData(literalNode.getString().getBytes()).commit().getId())
                    .commit();

        }
        return null;
    }

    public void parse(String sourceString) {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", sourceString);
        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();
        Block block = functionNode.getBody();
        for (Statement statement : block.getStatements())
            jsLine(new Node(), statement);
    }


}
