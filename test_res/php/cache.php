<?php

include_once "db.php";
// TODO change name of get_cache
if (!function_exist("get_cache")) {
    function get_cache()
    {
        $cache = array();
        $cache_map = $GLOBALS["cache_map"];
        if ($cache_map["nodes"] != null) {
            $node_id_list = $cache_map["nodes"];
            for ($i = 0; $i < get_int("level", 2) && count($node_id_list) > 0; $i++) {

                $links = select("select * from links where node_id in (" . implode(",", $node_id_list) . ")");

                $data_node_id_list = $node_id_list;
                foreach ($links as $link)
                    $data_node_id_list[] = $link["attach_id"];

                $nodes = selectMap("select * from nodes where node_id in (" . implode(",", $data_node_id_list) . ")", "node_id");

                foreach ($nodes as $node_id => $node) {
                    if (!$GLOBALS["extend"]) {
                        unset($node["node_id"]);
                        if ($node["node_type"] == "Var") unset($node["node_type"]);
                        if ($node["data"] == null) unset($node["data"]);
                        if ($node["url"] == null) unset($node["url"]);
                    }
                    if (in_array($node_id, $node_id_list))
                        $cache["nodes"][$node_id] = $node;
                }

                $node_id_list = array();
                foreach ($links as $link) {
                    $cache["nodes"][$link["node_id"]][$link["link_type"]][] = $link["attach_id"];
                    if ($cache["nodes"][$link["attach_id"]] == null)
                        $node_id_list[] = $link["attach_id"];
                }

            }

            $save_strings = array();
            foreach ($cache["nodes"] as $node_id => $node)
                foreach ($node as $link_name => $link_type)
                    if (count($link_type) == 1 && $cache["nodes"][$link_type[0]]["node_type"] == "String") {
                        if (count(array_keys($cache["nodes"][$link_type[0]])) <= 2){
                            $cache["nodes"][$node_id][$link_name] = $cache["nodes"][$link_type[0]]["data"];
                        }else{
                            $save_strings[] = $link_type[0];
                        }
                    } else if (count($link_type) > 1)
                        foreach ($link_type as $link_node_id)
                            if ($cache["nodes"][$link_node_id]["node_type"] == "String")
                                $save_strings[] = $link_node_id;
            foreach ($cache["nodes"] as $node_id => $node)
                if ($node["node_type"] == "String" && !in_array($node_id, $save_strings))
                    unset($cache["nodes"][$node_id]);

            foreach ($cache["nodes"] as $node_id => $node)
                $cache["nodes"][$node_id] = node_link_simplification($node);
        };

        $node_id_list = array_keys($cache["nodes"]);
        $all_node_id_list = array();
        while (count($node_id_list) != 0) {
            $top_node_id_list = selectMap("select node_id, attach_id from links where link_type = 'dir' and attach_id in (" . implode(",", array_unique($node_id_list)) . ")", "attach_id");
            $node_id_list = array();
            foreach ($top_node_id_list as $attach_id => $link)
                if ($all_node_id_list[$attach_id] == null) {
                    $all_node_id_list[$attach_id] = $link["node_id"];
                    $node_id_list[] = $link["node_id"];
                }
        }
        if ($all_node_id_list != null) {
            $all_node_title = selectMap("select t1.node_id, t2.data from links t1 "
                . " right join nodes t2 on t2.node_id = t1.attach_id "
                . " where t1.link_type = 'title' and t1.node_id in (" . implode(",", array_keys($all_node_id_list)) . ")", "node_id");

            foreach (array_keys($cache["nodes"]) as $node_id) {
                $name = "";
                $last_node_id = $node_id;
                while ($all_node_title[$last_node_id]["data"] != null) {
                    $name = "/" . $all_node_title[$last_node_id]["data"] . $name;
                    $last_node_id = $all_node_id_list[$last_node_id];
                }
                if ($name != null)
                    $cache["nodes"][$node_id] = array("node_path" => $name) + $cache["nodes"][$node_id];
            }
        }
        return $cache;
    }
}