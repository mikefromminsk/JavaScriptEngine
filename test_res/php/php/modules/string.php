<?php

$module = node(array("node_path" => "php/prototypes", "node_local" => "String"));

function ext_string_reverse($node_id)
{
    $value_node_id = get_value($node_id);
    if ($value_node_id != $node_id){
        $original_str = scalar("select data from nodes where node_id = $value_node_id");
        return strrev($original_str);
    }
    return "";
}

node(array(
    "node_id" => $module,
    "node_local" => "reverse",
    "ext_name" => "!ext_string_reverse",
    "node_type" => "function",
    "params" => node(array("title" => "!node_id"))
));


