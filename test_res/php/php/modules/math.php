<?php
$module = node(array("node_path" => "php", "local" => "!", "dir" => "!"));

function ext_add($node_id, $a, $b)
{
    return $a + $b;
}

// TODO change ext_name to data
// TODO automatic scan fucntions and registry
node(array(
    "node_id" => $module,
    "node_local" => "add",
    "ext_name" => "!ext_add",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));


function ext_minus($node_id, $a, $b)
{
    return $a - $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "minus",
    "ext_name" => "!ext_minus",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_mul($node_id, $a, $b)
{
    return $a * $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "mul",
    "ext_name" => "!ext_mul",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_div($node_id, $a, $b)
{
    return $a / $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "div",
    "ext_name" => "!ext_div",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_mod($node_id, $a, $b)
{
    return $a % $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "mod",
    "ext_name" => "!ext_mod",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_equals($node_id, $a, $b)
{
    return $a == $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "equals",
    "ext_name" => "!ext_equals",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));
function ext_and($node_id, $a, $b)
{
    return $a && $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "and",
    "ext_name" => "!ext_and",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_or($node_id, $a, $b)
{
    return $a || $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "or",
    "ext_name" => "!ext_or",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_inc($node_id, $a)
{
    return $a + 1;
}

node(array(
        "node_id" => $module,
        "node_local" => "inc",
        "ext_name" => "!ext_inc",
        "node_type" => "function",
        "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a"))
));
function ext_dec($node_id, $a)
{
    return $a--;
}

node(array(
        "node_id" => $module,
        "node_local" => "dec",
        "ext_name" => "!ext_dec",
        "node_type" => "function",
        "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a"))
));

function ext_less($node_id, $a, $b)
{
    return $a < $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "less",
    "ext_name" => "!ext_less",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));


function ext_more($node_id, $a, $b)
{
    return $a > $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "more",
    "ext_name" => "!ext_more",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_less_or_equal($node_id, $a, $b)
{
    return $a <= $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "less_or_equal",
    "ext_name" => "!ext_less_or_equal",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));

function ext_more_or_equal($node_id, $a, $b)
{
    return $a >= $b;
}

node(array(
    "node_id" => $module,
    "node_local" => "more_or_equal",
    "ext_name" => "!ext_more_or_equal",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!a")) . "," . node(array("title" => "!b"))
));







