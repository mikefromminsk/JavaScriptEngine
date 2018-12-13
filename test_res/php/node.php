<?php
include_once "db.php";

if (!function_exist("delete_links")) {
    function delete_links($node_id, $link_type, $offset = false)
    {
        if ($offset === false) {
            query("delete from links where node_id = $node_id and link_type = '$link_type'");
        } else {
            $link_id = scalar("select link_id from links where node_id = $node_id and link_type = '$link_type' limit $offset,1");
            if ($link_id != null)
                query("delete from links where link_id = $link_id");
        }
    }
}

if (!function_exist("set_link")) {
    function set_link($node_id, $link_type, $attach_id, $offset)
    {
        if ($offset === false) {
            delete_links($node_id, $link_type);
            insertList("links", array(
                "node_id" => $node_id,
                "link_type" => $link_type,
                "attach_id" => $attach_id
            ));
        } else if ($offset === true) {
            insertList("links", array(
                "node_id" => $node_id,
                "link_type" => $link_type,
                "attach_id" => $attach_id
            ));
        } else {
            $link_id = scalar("select link_id from links where node_id = $node_id and link_type = '$link_type' limit $offset,1");
            if ($link_id != null)
                query("update links set attach_id = $attach_id where link_id = $link_id");
        }
    }
}

if (!function_exist("node_body")) {
    function node_body($node_id)
    {
        return selectObject("select * from nodes where node_id = $node_id");
    }
}


if (!function_exist("node_link_simplification")) {
    function node_link_simplification($node)
    {
        $link_to_one = array("value", "source", "title", "set", "true", "else", "exit", "while", "if",
            "ext_name", "prototype", "body");
        foreach ($node as $link_type => $attach_id_list)
            if (is_array($attach_id_list) && count($attach_id_list) == 1 && array_search($link_type, $link_to_one) !== false)
                $node[$link_type] = $attach_id_list[0];
        return $node;
    }
}

if (!function_exist("node_links")) {
    function node_links($node_id)
    {
        $node = array();
        $links = select("select * from links where node_id = $node_id");
        foreach ($links as $link)
            $node[$link["link_type"]][] = $link["attach_id"];
        return node_link_simplification($node);
    }
}


if (!function_exist("make_path")) {
    function make_path($node_id, $node_path)
    {
        if ($node_path == "..") {
            if ($node_id == null || $node_id == 1) return null;
            $up_path_node_id = scalar("select node_id from links where link_type = 'dir' and attach_id = $node_id");
            return $up_path_node_id != null ? $up_path_node_id : $node_id;
        } else {
            if ($node_id != null && $node_path == null) return $node_id;
            if ($node_id == null && $node_path != null) $node_id = 1;
            if ($node_id != null && $node_path != null) {
                $node_path = explode("/", $node_path);
                for ($i = 0; $i < count($node_path); $i++) {
                    $node_name = $node_path[$i];
                    $next_node_id = scalar("select attach_id from links t1 "
                        . " right join nodes t2 on t2.node_id = t1.attach_id and t2.data = '$node_name' "
                        . " where t1.node_id = $node_id and t1.link_type = 'dir'");
                    if ($next_node_id == null) {
                        insertList("nodes", array("node_type" => "dir", "data" => $node_name));
                        $next_node_id = scalar("select max(node_id) from nodes");
                        set_link($node_id, 'dir', $next_node_id, true);
                    }
                    $node_id = $next_node_id;
                }
            }
        }
        return $node_id;
    }
}

if (!function_exist("find_local")) {
    function find_local($node_id, &$node_name)
    {
        $attach_id = scalar("select t1.attach_id from links t1 "
            . " right join links t2 on t2.node_id = t1.attach_id and t2.link_type = 'title'"
            . " right join nodes t3 on t3.node_id = t2.attach_id and BINARY t3.data = '$node_name'"
            . " where t1.node_id = $node_id and t1.link_type in ('local', 'params')"
            . " order by field (t1.link_type, 'local', 'params') limit 1");
        if ($attach_id == null) {
            $includes = node_links($node_id)["include"];
            for ($i = count($includes) - 1; $i >= 0; $i--) {
                $include = $includes[$i];
                $body = node_body($include);
                if ($body["node_type"] == "String")
                    $include = make_path(null, $body["data"]);
                $find_node_id = find_local($include, $node_name);
                if ($find_node_id != null)
                    return $find_node_id;
            }
            $local_parent = scalar("select node_id from links where link_type = 'local' and attach_id = $node_id");
            if ($local_parent != null)
                $attach_id = find_local($local_parent, $node_name);
        }
        return $attach_id;
    }
}

if (!function_exist("make_local")) {
    function make_local($node_id, $node_local, $node_type)
    {
        if ($node_id != null && $node_local == null) return $node_id;
        if ($node_id == null && $node_local != null) $node_id = 1;
        if ($node_type == null) $node_type = "Var";
        if ($node_id != null && $node_local != null) {
            $node_local = explode(".", $node_local);
            $find_node_id = find_local($node_id, $node_local[0]);
            if ($find_node_id != null) $node_id = $find_node_id;
            for ($i = ($find_node_id == null ? 0 : 1); $i < count($node_local); $i++) {
                $node_name = $node_local[$i];
                $next_node_id = scalar("select t1.node_id from links t1 "
                    . " right join nodes t2 on t2.node_id = t1.attach_id and BINARY t2.data = '$node_name'"
                    . " where t1.node_id in (select attach_id from links where node_id = $node_id and link_type = 'local') and t1.link_type = 'title'");
                if ($next_node_id == null) {
                    insertList("nodes", array("node_type" => "String", "data" => $node_name));
                    $title_node_id = scalar("select max(node_id) from nodes");
                    insertList("nodes", array("node_type" => $node_type));
                    $variable_node_id = scalar("select max(node_id) from nodes");
                    set_link($variable_node_id, 'title', $title_node_id, false);
                    set_link($node_id, 'local', $variable_node_id, true);
                    $next_node_id = $variable_node_id;
                }
                $node_id = $next_node_id;
            }
        }
        return $node_id;
    }
}


if (!function_exist("node")) {
    function node($params = null, $execute_mode = false)
    {
        if ($params == null)
            $params = array();

        $node_id = $params["node_id"];
        $data = $params["data"];
        $url = $params["url"];
        $node_type = $params["node_type"];
        $node_path = $params["node_path"];
        $node_local = $params["node_local"];

        unset($params["node_id"]);
        unset($params["level"]);
        unset($params["node_type"]);
        unset($params["node_path"]);
        unset($params["node_local"]);
        unset($params["device_id"]);
        unset($params["data"]);
        unset($params["url"]);

        if ($node_path != null)
            $node_id = make_path($node_id, $node_path);

        if ($node_local != null)
            $node_id = make_local($node_id, $node_local, $node_type);

        if ($node_id == null) {
            insertList("nodes", array("node_type" => $node_type != null ? $node_type : "Var"));
            $node_id = scalar("select max(node_id) from nodes");
        }

        foreach ($params as $link_type => $value) {

            if ($value == null)
                continue;

            $offset = false;
            if ($link_type[strlen($link_type) - 1] == "]") {
                $offset = substr($link_type, strpos($link_type, "[") + 1, -1);
                $link_type = substr($link_type, 0, strpos($link_type, "["));
                if ($offset === "")
                    $offset = true;
            }

            if (ctype_digit($value[0])) {
                if (strpos($value, ",") === false) {
                    set_link($node_id, $link_type, $value, $offset);
                } else {
                    delete_links($node_id, $link_type);
                    foreach (explode(",", $value) as $param_node_id)
                        set_link($node_id, $link_type, $param_node_id, true);
                }
            } else if ($value == "!") {
                delete_links($node_id, $link_type, $offset);
            } else {
                if ($value[0] == "!") {
                    insertList("nodes", array(
                        "node_type" => "String",
                        "data" => substr($value, 1),
                        "url" => null,
                    ));
                    set_link($node_id, $link_type, scalar("select max(node_id) from nodes"), $offset);
                } else {
                    insertList("nodes", array(
                        "node_type" => "Var",
                        "data" => null,
                        "url" => $value,
                    ));
                    $find_node_id = scalar("select max(node_id) from nodes");
                    set_link($node_id, $link_type, $find_node_id, $offset);
                }
            }
        }

        updateList("nodes", array(
            "data" => $data,
            "url" => $url,
            "node_type" => $node_type,
        ), "node_id", $node_id);

        if ($node_type == "js_code" && $data != null) {
            include_once "js_parser.php";
            parse_js_code($node_id, $data);
        }

        if ($execute_mode) {
            include_once "run.php";
            run($node_id);
        }

        return $node_id;
    }
}

$params = merge($_GET, $_POST);
$execute_mode = $params["execute"] != null;
$extend = $params["extend"] != null;
unset($params["execute"]);
unset($params["extend"]);
$node_id = node($params, $execute_mode);
insert_cache("nodes", $node_id);
$result["node_id"] = $node_id;
response($result);
