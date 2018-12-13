<?php

use Peast\Syntax\Node\ArrayExpression;
use Peast\Syntax\Node\AssignmentExpression;
use Peast\Syntax\Node\BinaryExpression;
use Peast\Syntax\Node\BlockStatement;
use Peast\Syntax\Node\CallExpression;
use Peast\Syntax\Node\ClassDeclaration;
use Peast\Syntax\Node\ExpressionStatement;
use Peast\Syntax\Node\ForStatement;
use Peast\Syntax\Node\FunctionDeclaration;
use Peast\Syntax\Node\FunctionExpression;
use Peast\Syntax\Node\Identifier;
use Peast\Syntax\Node\IfStatement;
use Peast\Syntax\Node\Literal;
use Peast\Syntax\Node\MemberExpression;
use Peast\Syntax\Node\MethodDefinition;
use Peast\Syntax\Node\NewExpression;
use Peast\Syntax\Node\ObjectExpression;
use Peast\Syntax\Node\ParenthesizedExpression;
use Peast\Syntax\Node\Program;
use Peast\Syntax\Node\Property;
use Peast\Syntax\Node\ReturnStatement;
use Peast\Syntax\Node\ThisExpression;
use Peast\Syntax\Node\UpdateExpression;
use Peast\Syntax\Node\VariableDeclaration;

function __autoload($class)
{
    require $class . '.php';
}

function js_line($module_node_id, $statement)
{
    if ($statement instanceof Literal) {
        $raw = $statement->getRaw();
        if ($raw == "null") {
            return "" . node(array("node_type" => "Null"));
        } else if ($raw == "false") {
            return "" . node(array("node_type" => "Bool", "data" => "0"));
        } else if ($raw == "true") {
            return "" . node(array("node_type" => "Bool", "data" => "1"));
        } else if (is_numeric($raw)) {
            return "" . node(array("node_type" => "Number", "data" => $raw));
        } else {
            return "" . node(array("node_type" => "String", "data" => substr($raw, 1, strlen($raw) - 2)));
        }
    }

    if ($statement instanceof Identifier) {
        return node(array("node_id" => $module_node_id, "node_local" => $statement->getName()));
    }

    if ($statement instanceof ArrayExpression) {
        $array_node_id = node(array("node_type" => "Array"));
        foreach ($statement->getElements() as $element)
            node(array("node_id" => $array_node_id, "cell[]" => js_line($module_node_id, $element)));
        return $array_node_id;
    }

    if ($statement instanceof ObjectExpression) {
        $object_node_id = node(array("node_type" => "Object"));
        node(array("node_id" => $module_node_id, "local[]" => $object_node_id));
        foreach ($statement->getProperties() as $property)
            js_line($object_node_id, $property);
        query("delete from links where node_id = $module_node_id and link_type = 'local' and attach_id = $object_node_id");
        return $object_node_id;
    }

    if ($statement instanceof Property || $statement instanceof MethodDefinition) {
        $key = "";
        if ($statement->getKey() instanceof Identifier)
            $key = $statement->getKey()->getName();
        if ($statement->getKey() instanceof Literal)
            $key = $statement->getKey()->getValue();
        if ($statement->getValue() instanceof Literal)
            return node(array(
                "node_id" => $module_node_id,
                "local[]" => node(array(
                    "title" => "!" . $key,
                    "value" => js_line($module_node_id, $statement->getValue())
                )),
            ));
        elseif ($statement->getValue() instanceof FunctionExpression)
            return node(array(
                "node_id" => $module_node_id,
                "local[]" => node(array(
                    "node_id" => js_line($module_node_id, $statement->getValue()),
                    "title" => "!" . $key,
                )),
            ));
        else
            return node(array(
                "node_id" => $module_node_id,
                "local[]" => node(array(
                    "title" => "!" . $key,
                    "source" => js_line($module_node_id, $statement->getValue())
                )),
            ));
    }

    if ($statement instanceof ClassDeclaration) {
        $class_node_id = node(array("node_id" => $module_node_id,
            "node_local" => $statement->getId()->getName(),
            "local" => "!"
        ));
        foreach ($statement->getBody()->getBody() as $local)
            js_line($class_node_id, $local);
    }

    if ($statement instanceof MemberExpression) {
        if ($statement->getObject() instanceof Identifier) {
            $object_node_id = node(array("source" => js_line($module_node_id, $statement->getObject())));
        } else {
            $object_node_id = js_line($module_node_id, $statement->getObject());
        }
        if ($statement->getComputed()) {
            return node(array("node_id" => $object_node_id, "prop[]" => js_line($module_node_id, $statement->getProperty())));
        } else {
            if ($statement->getProperty() instanceof Literal || $statement->getProperty() instanceof Identifier)
                return node(array("node_id" => $object_node_id, "prop[]" => "!" . $statement->getProperty()->getName()));
        }
    }

    if ($statement instanceof ThisExpression) {
        return node();
    }

    if ($statement instanceof BinaryExpression) {
        $operator = $statement->getOperator();
        $function_name = "";
        if ($operator == "+") $function_name = "add";
        else if ($operator == "-") $function_name = "minus";
        else if ($operator == "*") $function_name = "mul";
        else if ($operator == "/") $function_name = "div";
        else if ($operator == "%") $function_name = "mod";
        else if ($operator == "&&") $function_name = "and";
        else if ($operator == "||") $function_name = "or";
        else if ($operator == "==") $function_name = "equals";
        else if ($operator == "<") $function_name = "less";
        else if ($operator == "<=") $function_name = "less_or_equal";
        else if ($operator == ">") $function_name = "more";
        else if ($operator == ">=") $function_name = "more_or_equal";
        return node(array(
            "source" => node(array("node_path" => "php", "node_local" => $function_name)),
            "params" =>
                js_line($module_node_id, $statement->getLeft()) . "," .
                js_line($module_node_id, $statement->getRight()),
        ));
    }

    if ($statement instanceof ParenthesizedExpression) {
        return js_line($module_node_id, $statement->getExpression());
    }

    if ($statement instanceof BlockStatement) {
        $block_node_id = node();
        node(array("node_id" => $module_node_id, "local[]" => $block_node_id));
        foreach ($statement->getBody() as $block_statement)
            node(array("node_id" => $block_node_id, "next[]" => js_line($block_node_id, $block_statement)));
        query("delete from links where node_id = $module_node_id and link_type = 'local' and attach_id = $block_node_id");
        return $block_node_id;
    }

    if ($statement instanceof Program) {
        node(array("node_id" => 1, "local[]" => $module_node_id));
        foreach ($statement->getBody() as $block_statement)
            node(array("node_id" => $module_node_id, "next[]" => js_line($module_node_id, $block_statement)));
        query("delete from links where node_id = 1 and link_type = 'local' and attach_id = $module_node_id");
        return $module_node_id;
    }

    if ($statement instanceof IfStatement) {
        return node(array(
            "if" => js_line($module_node_id, $statement->getTest()),
            "true" => js_line($module_node_id, $statement->getConsequent()),
            "else" => js_line($module_node_id, $statement->getAlternate()),
        ));
    }
    if ($statement instanceof ForStatement) {
        $for_init_node_id = node();
        //init
        node(array("node_id" => $module_node_id, "local[]" => $for_init_node_id));
        node(array("node_id" => $for_init_node_id, "next[]" => js_line($for_init_node_id, $statement->getInit())));
        //body
        $for_body_node_id = js_line($for_init_node_id, $statement->getBody());
        //equal
        $for_start_node_id = node(array(
            "while" => $for_body_node_id,
            "if" => js_line($for_init_node_id, $statement->getTest())
        ));
        //update
        node(array(
            "node_id" => $for_body_node_id,
            "next[]" => js_line($for_init_node_id, $statement->getUpdate())
        ));
        node(array("node_id" => $for_init_node_id, "next[]" => $for_start_node_id));

        query("delete from links where node_id = $module_node_id and link_type = 'local' and attach_id = $for_init_node_id");
        query("delete from links where node_id = $for_init_node_id and link_type = 'local' and attach_id = $for_start_node_id");

        return $for_init_node_id;
    }

    if ($statement instanceof UpdateExpression) {
        $operator = $statement->getOperator();
        $function_name = null;
        if ($operator == "++") $function_name = "inc";
        if ($operator == "--") $function_name = "dec";
        $var_node_id = js_line($module_node_id, $statement->getArgument());
        return node(array(
            "source" => $var_node_id,
            "next" => node(array(
                "source" => $var_node_id,
                "set" => node(array(
                    "source" => node(array("node_path" => "php", "node_local" => $function_name)),
                    "params" => $var_node_id,
                ))
            ))
        ));
    }

    if ($statement instanceof VariableDeclaration) {
        foreach ($statement->getDeclarations() as $declaration) {
            $var_node_id = node(array("node_id" => $module_node_id, "node_local" => $declaration->getId()->getName()));
            if ($declaration->getInit() != null) {
                node(array("node_id" => $module_node_id, "next[]" =>
                    node(array(
                        "source" => $var_node_id,
                        "set" => js_line($module_node_id, $declaration->getInit())
                    ))
                ));
            }
        }
        return null;
    }

    if ($statement instanceof ReturnStatement) {
        return node(array(
            "source" => $module_node_id,
            "set" => js_line($module_node_id, $statement->getArgument()),
            "exit" => $module_node_id,
        ));
    }

    if ($statement instanceof CallExpression || $statement instanceof NewExpression) {
        if ($statement->getCallee() instanceof Identifier)
            if ($statement->getCallee()->getName() == "include") {
                node(array("node_id" => $module_node_id, "include[]" => js_line($module_node_id, $statement->getArguments()[0])));
                return null;
            }
        $args_node_id_list = array();
        foreach ($statement->getArguments() as $argument)
            $args_node_id_list[] = js_line($module_node_id, $argument);
        $result_node_id = node(array(
            "node_type" => ($statement instanceof NewExpression) ? "Object" : "Var",
            "source" => js_line($module_node_id, $statement->getCallee()),
            "params" => implode(",", $args_node_id_list)
        ));
        return $result_node_id;
    }

    if ($statement instanceof ExpressionStatement) {
        $expression = $statement->getExpression();
        if ($expression instanceof AssignmentExpression) {
            $left = $expression->getLeft();
            $right = $expression->getRight();
            if ($left instanceof MemberExpression &&
                $left->getProperty()->getName() == "prototype")
                // TODO not implemented calculated prototype field
                return node(array(
                    "source" => js_line($module_node_id, $left),
                    "set" => js_line($module_node_id, $right),
                ));
            else
                return node(array(
                    "source" => js_line($module_node_id, $left),
                    "set" => js_line($module_node_id, $right),
                ));
        }
        if ($expression instanceof CallExpression) {
            return js_line($module_node_id, $expression);
        }
    }
    if ($statement instanceof FunctionDeclaration) {
        $params = array();
        foreach ($statement->getParams() as $param)
            $params[] = node(array("title" => "!" . $param->getName()));
        $function_node_id = node(array(
            "node_id" => $module_node_id,
            "node_local" => $statement->getId()->getName(),
            "params" => implode(",", $params),
            "local" => "!", "include" => "!", "next" => "!", "value" => "!", "set" => "!", "if" => "!",
        ));
        foreach ($statement->getBody()->getBody() as $block_statement)
            node(array("node_id" => $function_node_id, "next[]" => js_line($function_node_id, $block_statement)));
        return null;
    }
    if ($statement instanceof FunctionExpression) {
        $params = array();
        foreach ($statement->getParams() as $param)
            $params[] = node(array("title" => "!" . $param->getName()));
        $function_node_id = node(array("node_type" => "Function", "params" => implode(",", $params),));
        node(array("node_id" => $module_node_id, "local[]" => $function_node_id));
        foreach ($statement->getBody()->getBody() as $block_statement)
            node(array("node_id" => $function_node_id, "next[]" => js_line($function_node_id, $block_statement)));
        query("delete from links where node_id = $module_node_id and link_type = 'local' and attach_id = $function_node_id");
        return $function_node_id;
    }

    return null;
}

function readable_ast($ast)
{
    if (is_object($ast) || is_array($ast)) {
        $ast_array = (array)$ast;
        $new_result = array();
        foreach (array_keys($ast_array) as $key) {
            $trim_key = trim($key, "*\0");
            if ($trim_key != "location" && $trim_key != "trailingComments"
                && $trim_key != "leadingComments" && $trim_key != "propertiesMap")
                $new_result[$trim_key] = readable_ast($ast_array[$key]);
        }
        return $new_result;
    }
    return $ast;
}

function parse_js_code($node_id, $source)
{
    $ast = Peast\Peast::latest($source, array())->parse();
    //echo json_encode_readable(readable_ast(json_decode(json_encode($ast))));
    node(array("node_id" => $node_id, "local" => "!", "next" => "!", "include" => "!"));
    js_line($node_id, $ast);
}
