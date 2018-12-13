<?php

include_once "php/modules/modules.php";

function get_default_prototype($node_type)
{
    $default_prototypes_node_id = node(array("node_path" => "php/prototypes"));
    return find_local($default_prototypes_node_id, $node_type);
}

function get_props($node_id, &$called_object = null)
{
    $links = node_links($node_id);
    $props = $links["prop"];
    if ($links["source"] != null && $props != null)
        $node_id = $links["source"];
    $start_from_this = $links["source"] == null && $props != null;
    if ($start_from_this && $called_object != null) {
        $node_id = $called_object;
        $links = node_links($node_id);
    }

    foreach ($props as $prop_node_id) {
        $called_object = $node_id;
        run($prop_node_id);
        $prop_name_node = node_body(get_value($prop_node_id));
        $prop_name = $prop_name_node["data"];
        $prop_type = $prop_name_node["node_type"];
        if ($prop_type == "String") {
            $node_type = null;
            $links = node_links($node_id);
            // TODO change init of node_type
            if ($links["source"] != null) { //hardcode
                $source_links = node_links($links["source"]);
                if ($source_links["value"] != null)
                    $node_type = node_body($source_links["value"])["node_type"];
            } elseif ($links["value"] != null) {
                $node_type = node_body($links["value"])["node_type"];
            }
            $prototype_node_id = $links["prototype"];
            if ($prop_name == "node_id") {
                $node_id = node(array("node_type" => "Number", "data" => $node_id));
                continue;
            } else if ($prop_name == "prototype") {
                if ($prototype_node_id != null) { //proto exist
                    $node_id = $prototype_node_id;
                } else { //proto by node_type
                    if ($prototype_node_id == null && $node_type != null)
                        $prototype_node_id = get_default_prototype($node_type);
                    if ($prototype_node_id != null) {
                        $node_id = $prototype_node_id;
                    } else { // create proto
                        $prototype_node_id = node();
                        node(array("node_id" => $node_id, "prototype" => $prototype_node_id));
                        $node_id = $prototype_node_id;
                    }
                }
                continue;
            } else { // otherwise
                $find_prop_node_id = null;
                while ($prototype_node_id != null && $find_prop_node_id == null) {
                    $find_prop_node_id = find_local($prototype_node_id, $prop_name);
                    if ($find_prop_node_id == null)
                        $prototype_node_id = node_links($prototype_node_id)["prototype"];
                }
                if ($find_prop_node_id != null) {
                    $node_id = $find_prop_node_id;
                    continue;
                } else {
                    if ($node_type != null)
                        $prototype_node_id = get_default_prototype($node_type);
                    if ($prototype_node_id != null) {
                        $find_prop_node_id = find_local($prototype_node_id, $prop_name);
                        if ($find_prop_node_id != null) {
                            $node_id = $find_prop_node_id;
                            continue;
                        }
                    }
                }
            }
            // create new local node if prop_name not found
            $node_id = node(array("node_id" => $node_id, "node_local" => $prop_name));
        } else if ($prop_type == "Number") {
            $index = $prop_name;
            $node_id = scalar("select attach_id from links where node_id = $node_id and link_type = 'cell' limit $index + 1, 1");
        }
    }
    return $node_id;
}

function get_value_or_null($node_id)
{
    return scalar("select attach_id from links where node_id = $node_id and link_type = 'value'");
}

function get_value($node_id)
{
    $node_value = get_value_or_null($node_id);
    if ($node_value != null)
        return $node_value;
    return $node_id;
}

class NodeResult
{
    public $node_id;

    public function __construct($node_id)
    {
        $this->node_id = $node_id;
    }
}

function var_to_node($value)
{
    $result_node_id = null;
    if ($value instanceof NodeResult) {
        $result_node_id = $value->node_id;
    } else if (is_double($value)) {
        $result_node_id = node(array("node_type" => "Number", "data" => "" . $value));
    } else if (is_string($value)) {
        $result_node_id = node(array("node_type" => "String", "data" => "" . $value));
    } else if (is_bool($value)) {
        $result_node_id = node(array("node_type" => "Bool", "data" => "" . ($value ? 1 : 0)));
    } else if (is_array($value)) {
        $result_node_id = node(array("node_type" => "Object", "data" => "" . json_encode($value)));
    }
    return $result_node_id;
}

function call($node, $call_node_id)
{
    //first param always ths
    $call_params = array($call_node_id);
    foreach ($node["params"] as $param_node_id) {
        $data_node = node_body(get_value($param_node_id));
        if ($data_node["node_type"] == "null")
            $call_params[] = null;
        else if ($data_node["node_type"] == "Number")
            $call_params[] = doubleval($data_node["data"]);
        else
            $call_params[] = $data_node["data"];
    }

    $function_name = node_body($node["ext_name"])["data"];
    $result = call_user_func_array($function_name, $call_params);
    node(array(
        "node_id" => $node["node_id"],
        "value" => var_to_node($result)
    ));
}

function clone_object($source_node_id, $template_node_id)
{
    node(array("node_id" => $source_node_id, "node_type" => "Object",
        "local" => "!", "next" => "!", "prototype" => "!"));

    run($template_node_id, $source_node_id);
    $template_links = node_links($template_node_id);
    if ($template_links["source"] != null) {
        $template_node_id = $template_links["source"];
        $template_links = node_links($template_node_id);
    }

    if ($template_links["next"] != null) {
        // clone from function
        run($template_node_id, $source_node_id);
    } else {
        //clone from object
        foreach ($template_links["local"] as $template_local_node_id) {
            $template_local_links = node_links($template_local_node_id);
            if ($template_local_links["title"] != null) {
                if ($template_local_links["source"] != null) {
                    run($template_local_node_id, $source_node_id);
                    $template_local_value_node_id = get_value_or_null($template_local_node_id);
                    if ($template_local_value_node_id != null)
                        node(array(
                            "node_id" => $source_node_id,
                            "local[]" => node(array(
                                "title" => "!" . node_body($template_local_links["title"])["data"],
                                "value" => $template_local_value_node_id
                            )),
                        ));
                } else if ($template_local_links["value"] != null) {
                    node(array(
                        "node_id" => $source_node_id,
                        "local[]" => node(array(
                            "title" => "!" . node_body($template_local_links["title"])["data"],
                            "value" => $template_local_links["value"]
                        )),
                    ));
                }
            }
        }
    }
    node(array("node_id" => $source_node_id, "prototype" => $template_node_id));
}

function clone_array($source_node_id, $template_node_id)
{
    $new_array_node_id = node(array("node_type" => "Array"));
    $template_links = node_links($template_node_id);
    foreach ($template_links["cell"] as $template_cell_node_id) {
        run($template_cell_node_id);
        node(array("node_id" => $new_array_node_id, "cell[]" => get_value($template_cell_node_id)));
    }
    node(array("node_id" => $source_node_id, "value" => $new_array_node_id));
}

function is_data_node($node_id)
{
    return array_search(node_body($node_id)["node_type"], array("String", "Bool", "Number")) !== false;
}

// TODO multithreading
function run($node_id, $called_object = null)
{
    //echo "\n" . $node_id;
    $node = node_body($node_id) + node_links($node_id);

    foreach ($node["next"] as $next_node_id) {
        run($next_node_id, $called_object);
        if ($GLOBALS["run_exit_node_id"] != null) {
            if ($GLOBALS["run_exit_node_id"] == $node_id)
                $GLOBALS["run_exit_node_id"] = null;
            break;
        }
    }

    if ($node["node_type"] == "function") {
        call($node, $called_object);
    }

    if ($node["source"] != null) {
        $called_object_from_source = $called_object;
        $source = get_props($node["source"], $called_object_from_source);
        $set_node_id = $node["set"];
        if ($set_node_id != null) {
            $set_node_type = node_body($set_node_id)["node_type"];
            if ($set_node_type == "Var") {
                $set_node_id = get_props($set_node_id);
                run($set_node_id, $called_object);
                $set_value_result = get_value($set_node_id);
                if ($set_value_result != $set_node_id || is_data_node($set_value_result))
                    node(array("node_id" => $source, "value" => $set_value_result));
                else
                    node(array("node_id" => $source, "value" => "!"));
            } else if ($set_node_type == "Function") {
                node(array("node_id" => $source, "body" => $set_node_id, "set" => "!"));
            } else if ($set_node_type == "Object") {
                clone_object($source, $set_node_id);
            } else {
                node(array("node_id" => $source, "value" => $set_node_id));
            }
        } else {/*func(a)*/
            $method_node_id = node_links($source)["body"];
            if ($method_node_id != null)
                $source = $method_node_id;

            $source_params = node_links($source)["params"];
            for ($i = 0; $i < count($node["params"]); $i++) {
                run($node["params"][$i], $called_object_from_source);
                $param_value = get_props($node["params"][$i], $param_call_node_id = $called_object_from_source);
                node(array("node_id" => $source_params[$i], "value" => get_value($param_value)));
            }
            for ($j = $i; $j < count($source_params); $j++)
                node(array("node_id" => $source_params[$j], "value" => "!"));

            run($source, $called_object_from_source); /*a()*/
            node(array("node_id" => $node_id, "value" => get_value_or_null($source) ?: "!"));
        }
    }

    // if (){} else{}
    if ($node["if"] != null && $node["true"] != null) {
        run($node["if"], $called_object);
        if (node_body(get_value($node["if"]))["data"] == 1) {
            run($node["true"], $called_object);
        } else if ($node["else"] != null)
            run($node["else"], $called_object);
    }

    // while()
    if ($node["while"] != null && $node["if"] != null) {
        run($node["if"], $called_object);
        $i = 0;

        while (node_body(get_value($node["if"]))["data"] == 1) {
            run($node["while"], $called_object);
            if ($GLOBALS["run_exit_node_id"] != null) {
                if ($GLOBALS["run_exit_node_id"] == $node_id)
                    $GLOBALS["run_exit_node_id"] = null;
                break;
            }
            $i++;
            run($node["if"], $called_object);
        }
    }

    if ($node["exit"] != null)
        $GLOBALS["run_exit_node_id"] = $node["exit"];

}





