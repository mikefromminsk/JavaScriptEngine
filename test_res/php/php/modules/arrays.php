<?php

$module = node(array("node_path" => "php/prototypes", "node_local" => "Array"));

function ext_array_push($node_id, $value)
{
    node(array("node_id" => get_value($node_id), "cell[]" => var_to_node($value)));
}

node(array(
    "node_id" => $module,
    "node_local" => "push",
    "ext_name" => "!ext_array_push",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id")) . "," . node(array("title" => "!value"))
));
