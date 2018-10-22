package com.metabrain.djs;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.NodeVisitor;

public class JsParser {

    static class Printer implements NodeVisitor {
        @Override
        public boolean visit(AstNode node) {
            String indent = "%1$Xs".replace("X", String.valueOf(node.depth() + 1));
            if (node instanceof FunctionNode)
                System.out.println("123");
            System.out.format(indent, "").println(node.getClass());
            return true;
        }
    }

    public static void main(String[] args) {
        String file = "foo.js";
        String reader = "function ss(){}";
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecordingLocalJsDocComments(true);
        env.setAllowSharpComments(true);
        env.setRecordingComments(true);
        AstRoot node = new Parser(env).parse(reader, file, 1);
        node.visitAll(new Printer());
    }

    public static void parse(String source, Node node) {

    }
}
