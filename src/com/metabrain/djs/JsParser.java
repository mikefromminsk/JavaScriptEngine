package com.metabrain.djs;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

import java.util.Iterator;

public class JsParser {

    Node jsLine(Node module, org.mozilla.javascript.Node statement) {

        if (statement instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) statement;
            for (VariableInitializer variableInitializer : variableDeclaration.getVariables()) {
                Node variable = jsLine(module, variableInitializer.getTarget());
                if (variableInitializer.getInitializer() != null) {
                    Node setLink = jsLine(module, variableInitializer.getInitializer());
                    module.addNext(new Node()
                            .setSource(variable.getId())
                            .setSet(setLink.getId())
                            .commit().getId()
                    ).commit();
                }
            }
        }

        if (statement instanceof ObjectLiteral) {
            ObjectLiteral objectLiteral = (ObjectLiteral) statement;
            Node obj = new Node().commit();
            module.addLocal(obj.getId()).commit(); // TODO commit ?
            for (ObjectProperty item : objectLiteral.getElements())
                obj.addProp(jsLine(obj, item).getId()); // TODO add 3th param to jsLine $add_to_local_path
            module.removeLocal(obj.getId()).commit(); // TODO commit ?
            return obj.commit();
        }

        if (statement instanceof ObjectProperty) {
            ObjectProperty objectProperty = (ObjectProperty) statement;

            String key = "";
            if (objectProperty.getLeft() instanceof StringLiteral) {
                StringLiteral stringLiteral = (StringLiteral) objectProperty.getLeft();
                key = stringLiteral.getValue();
            } else if (objectProperty.getLeft() instanceof NumberLiteral) {
                NumberLiteral numberLiteral = (NumberLiteral) objectProperty.getLeft();
                key = numberLiteral.getValue();
            }
            if (objectProperty.getRight() instanceof FunctionCall) {
                return module
                        .addLocal(new Node()
                                .setTitle(key)
                                .setBody(jsLine(module, objectProperty.getRight()).getId())
                                .commit().getId()
                        ).commit();
            } else {
                return module
                        .addLocal(jsLine(module, objectProperty.getRight()).setTitle(key).commit().getId())
                        .commit();
            }
        }

        if (statement instanceof ArrayLiteral) {
            ArrayLiteral arrayLiteral = (ArrayLiteral) statement;
            Node arr = new Node(NodeType.ARRAY);
            for (AstNode item : arrayLiteral.getElements())
                arr.addCell(jsLine(module, item).getId());
            return arr.commit();
        }

        if (statement instanceof NumberLiteral) {
            NumberLiteral numberLiteral = (NumberLiteral) statement;
            return new Node()
                    .setValue(new Node(NodeType.NUMBER).setData(numberLiteral.getValue().getBytes()).commit().getId())
                    .commit();
        }

        if (statement instanceof StringLiteral) {
            StringLiteral numberLiteral = (StringLiteral) statement;
            return new Node()
                    .setValue(new Node(NodeType.STRING).setData(numberLiteral.getValue().getBytes()).commit().getId())
                    .commit();
        }

        if (statement instanceof KeywordLiteral) {
            KeywordLiteral numberLiteral = (KeywordLiteral) statement;
            String data = "";
            switch (numberLiteral.getType()) {
                case Token.TRUE:
                    data = "1";
                    break;
                case Token.FALSE:
                    data = "0";
                    break;
            }
            return new Node()
                    .setValue(new Node(NodeType.BOOL).setData(data.getBytes()).commit().getId())
                    .commit();
        }

        if (statement instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) statement;
            return new Node()
                    .setSource(module.getId())
                    .setSet(jsLine(module, returnStatement.getReturnValue()).getId())
                    .setExit(module.getId())
                    .commit();
        }

        if (statement instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statement;
            return new Node()
                    .setIf(jsLine(module, ifStatement.getCondition()).getId())
                    .setTrue(jsLine(module, ifStatement.getThenPart()).getId())
                    .setElse(jsLine(module, ifStatement.getElsePart()).getId())
                    .commit();
        }

        if (statement instanceof Assignment) {
            Assignment assignment = (Assignment) statement;
            Node left = jsLine(module, assignment.getLeft());
            Node right = jsLine(module, assignment.getRight());
            if (assignment.getOperator() == Token.IFEQ) {
                return new Node()
                        .setSource(left.getId())
                        .setSet(right.getId())
                        .commit();
            } else {
                int functionId = Functions.EQ;
                switch (assignment.getOperator()) {
                    case Token.ASSIGN_ADD:
                        functionId = Functions.ADD;
                        break;
                }
                Node functionCalc = new Node(NodeType.FUNCTION)
                        .setFunctionId(functionId)
                        .addParam(left.getId())
                        .addParam(right.getId())
                        .commit();
                return new Node()
                        .setSource(left.getId())
                        .setSet(functionCalc.getId())
                        .commit();
            }
        }

        if (statement instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) statement;
            int functionId = Functions.EQ; // error
            // TODO add all functions
            switch (infixExpression.getOperator()) {
                case Token.EQ:
                    functionId = Functions.EQ;
                    break;
                case Token.ADD:
                    functionId = Functions.ADD;
                    break;
                case Token.SUB:
                    functionId = Functions.SUB;
                    break;
                case Token.MUL:
                    functionId = Functions.MUL;
                    break;
                case Token.DIV:
                    functionId = Functions.DIV;
                    break;
                case Token.MOD:
                    functionId = Functions.MOD;
                    break;
                case Token.AND:
                    functionId = Functions.AND;
                    break;
                case Token.OR:
                    functionId = Functions.OR;
                    break;
                case Token.LE:
                    functionId = Functions.LE;
                    break;
            }
            return new Node(NodeType.FUNCTION)
                    .setFunctionId(functionId)
                    .addParam(jsLine(module, infixExpression.getLeft()).getId())
                    .addParam(jsLine(module, infixExpression.getRight()).getId())
                    .commit();
        }

        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expressionStatement = (ExpressionStatement) statement;
            return jsLine(module, expressionStatement.getExpression());
        }

        if (statement instanceof FunctionNode) {
            FunctionNode funcAst = (FunctionNode) statement;
            Node func = module.makeLocal(funcAst.getName());
            for (AstNode param : funcAst.getParams())
                func.addParam(new Node().setTitle(param.shortName()).commit().getId());
            func.commit();
            Iterator<org.mozilla.javascript.Node> it = funcAst.getBody().iterator();
            while (it.hasNext())
                jsLine(func, it.next());
        }

        if (statement instanceof Name) {
            Name name = (Name) statement;
            return module.makeLocal(name.getIdentifier().getBytes());
        }
        if (statement instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall) statement;
            Node func = jsLine(module, functionCall.getTarget());
            for (AstNode param : functionCall.getArguments())
                func.addParam(jsLine(module, param).getId());
            func.commit();
        }
        return null;
    }

    public void parse(String sourceString) {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecordingLocalJsDocComments(true);
        env.setAllowSharpComments(true);
        env.setRecordingComments(true);
        AstRoot statement = new Parser(env).parse(sourceString, sourceString, 1);
        Iterator<org.mozilla.javascript.Node> jsNode = statement.iterator();
        while (jsNode.hasNext())
            jsLine(new Node(), jsNode.next());
    }


}
