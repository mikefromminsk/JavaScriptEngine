package com.metabrain.djs;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityArrayCell;

import java.util.ArrayList;

class Node implements InfinityArrayCell {

    private Long id;
    private byte[] data;
    private byte type;

    private Long value;
    private Long source;
    private Long title;
    private Long set;
    private Long _true;
    private Long _else;
    private Long exit;
    private Long _while;
    private Long _if;
    private Long prototype;
    private Long body;

    private ArrayList<Long> local = new ArrayList<>();
    private ArrayList<Long> param = new ArrayList<>();
    private ArrayList<Long> next = new ArrayList<>();

    Node() {
        setType(NodeType.VAR);
    }

    Node(long node_id) {
        NodeStorage.getInstance().get(node_id, this);
    }

    public Node commit() {
        if (id == null)
            NodeStorage.getInstance().add(this);
        else
            NodeStorage.getInstance().set(id, this);
        return this;
    }



    public Node makeLocal(String path) {
        /*    function make_local($node_id, $node_local, $node_type)
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
    }*/
        return null;
    }

    public Node findLocal(String path) {
        /*
        * function find_local($node_id_list, $node_name)
    {
        foreach (array_reverse($node_id_list) as $node_id) {
            $attach_id = scalar("select t1.attach_id from links t1 "
                . " right join links t2 on t2.node_id = t1.attach_id and t2.link_type = 'title'"
                . " right join nodes t3 on t3.node_id = t2.attach_id and BINARY t3.data = '$node_name'"
                . " where t1.node_id = $node_id and t1.link_type in ('local', 'params')"
                . " order by field (t1.link_type, 'local', 'params') limit 1");
        if ($attach_id != null)
            return $attach_id;
    }
        return null;
}
        * */
        return null;
    }

    public Node makePath(String path) {
        /*
        * function make_path($node_id, $node_path)
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
                        insertList("nodes", array("node_type" => "Dir", "data" => $node_name));
                        $next_node_id = scalar("select max(node_id) from nodes");
                        set_link($node_id, 'dir', $next_node_id, true);
                    }
                    $node_id = $next_node_id;
                }
            }
        }
        return $node_id;
    }
        * */
        return null;
    }


    private void appendDataBody(ArrayList<Long> links, Long nodeId, byte type) {
        if (nodeId != null && links != null) {
            // TODO change type of convert
            byte[] bytes = Bytes.fromLong(nodeId);
            bytes[0] = type;
            long dataLink = Bytes.toLong(bytes);
            links.add(dataLink);
        }
    }

    private void appendArrayBody(ArrayList<Long> links, ArrayList<Long> nodeIdList, byte type) {
        if (nodeIdList != null && nodeIdList.size() > 0)
            for (Long nodeId : nodeIdList)
                appendDataBody(links, nodeId, type);
    }

    @Override
    public byte[] build() {
        ArrayList<Long> links = new ArrayList<>();
        appendDataBody(links, value, LinkType.VALUE);
        appendDataBody(links, source, LinkType.SOURCE);
        appendDataBody(links, title, LinkType.TITLE);
        appendDataBody(links, set, LinkType.SET);
        appendDataBody(links, _true, LinkType.TRUE);
        appendDataBody(links, _else, LinkType.ELSE);
        appendDataBody(links, exit, LinkType.EXIT);
        appendDataBody(links, _while, LinkType.WHILE);
        appendDataBody(links, _if, LinkType.IF);
        appendDataBody(links, prototype, LinkType.PROTOTYPE);
        appendDataBody(links, body, LinkType.BODY);
        appendArrayBody(links, local, LinkType.LOCAL);
        appendArrayBody(links, param, LinkType.PARAM);
        appendArrayBody(links, next, LinkType.NEXT);
        return Bytes.fromLongList(links);
    }

    @Override
    public void parse(byte[] data) {
        long[] links = Bytes.toLongArray(data);
        for (long dataLink : links) {
            byte[] bytes = Bytes.fromLong(dataLink);
            dataLink = Bytes.toLong(bytes);
            switch (bytes[0]) {
                case LinkType.VALUE:
                    value = dataLink;
                    break;
                case LinkType.SOURCE:
                    source = dataLink;
                    break;
                case LinkType.TITLE:
                    title = dataLink;
                    break;
                case LinkType.SET:
                    set = dataLink;
                    break;
                case LinkType.TRUE:
                    _true = dataLink;
                    break;
                case LinkType.ELSE:
                    _else = dataLink;
                    break;
                case LinkType.EXIT:
                    exit = dataLink;
                    break;
                case LinkType.WHILE:
                    _while = dataLink;
                    break;
                case LinkType.IF:
                    _if = dataLink;
                    break;
                case LinkType.PROTOTYPE:
                    prototype = dataLink;
                    break;
                case LinkType.BODY:
                    body = dataLink;
                    break;
                case LinkType.LOCAL:
                    local.add(dataLink);
                    break;
                case LinkType.PARAM:
                    param.add(dataLink);
                    break;
                case LinkType.NEXT:
                    next.add(dataLink);
                    break;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public Node setId(Long id) {
        this.id = id;
        return this;
    }

    public byte getType() {
        return type;
    }

    public Node setType(byte type) {
        this.type = type;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public Node setData(byte[] data) {
        this.data = data;
        return this;
    }


    public Long getValue() {
        return value;
    }

    public Node setValue(Long value) {
        this.value = value;
        return this;
    }

    public Long getSource() {
        return source;
    }

    public Node setSource(Long source) {
        this.source = source;
        return this;
    }

    public Long getTitle() {
        return title;
    }

    public Node setTitle(Long title) {
        this.title = title;
        return this;
    }

    public Long getSet() {
        return set;
    }

    public Node setSet(Long set) {
        this.set = set;
        return this;
    }

    public Long getTrue() {
        return _true;
    }

    public Node setTrue(Long _true) {
        this._true = _true;
        return this;
    }

    public Long getElse() {
        return _else;
    }

    public Node setElse(Long _else) {
        this._else = _else;
        return this;
    }

    public Long getExit() {
        return exit;
    }

    public Node setExit(Long exit) {
        this.exit = exit;
        return this;
    }

    public Long getWhile() {
        return _while;
    }

    public Node setWhile(Long _while) {
        this._while = _while;
        return this;
    }

    public Long getIf() {
        return _if;
    }

    public Node setIf(Long _if) {
        this._if = _if;
        return this;
    }

    public Long getPrototype() {
        return prototype;
    }

    public Node setPrototype(Long prototype) {
        this.prototype = prototype;
        return this;
    }

    public Long getBody() {
        return body;
    }

    public Node setBody(Long body) {
        this.body = body;
        return this;
    }

    public ArrayList<Long> getLocal() {
        return local;
    }

    public Node setLocal(ArrayList<Long> local) {
        this.local = local;
        return this;
    }

    public ArrayList<Long> getParam() {
        return param;
    }

    public Node setParam(ArrayList<Long> param) {
        this.param = param;
        return this;
    }

    public ArrayList<Long> getNext() {
        return next;
    }

    public Node setNext(ArrayList<Long> next) {
        this.next = next;
        return this;
    }
}
