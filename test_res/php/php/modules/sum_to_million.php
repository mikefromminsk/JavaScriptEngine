<?php

$module = node(array("node_path" => "php/modules/sum_to_million.php", "local" => "!", "dir" => "!"));

function ext_sum_to_million()
{
    $i = 0;
    for (; $i < 1000000; $i++) {
    }
    return $i;
}

node(array(
    "node_id" => $module,
    "node_local" => "sum",
    "ext_name" => "!ext_sum_to_million",
    "node_type" => "function"
));


