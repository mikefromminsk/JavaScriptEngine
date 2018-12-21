package com.metabrain.djs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabrain.djs.node.*;
import com.metabrain.djs.node.*;

import java.util.*;
/*
* {
  "node1": {
    // exept true false
    "path": "/reverse2",
    "local": "!wefwef"
  },
  "node2": {
    "start": "/root/dev",
    "path": "/reverse",
    "name": "!title",
    "local": ["node1"],
    "data": "reverse",
    "type": "Object",
    "string": "!new title",
    "number": 30,
    "bool": true,
    "link": "node1",
    "array": [
      "node1",
      "node2",
      true,
      30
    ]
  }
}

/* parse priority
        node_path
        node_local
        type
        data

*
*
* */
public class Formatter {

    public static final String NEW_NODE_PREFIX = "w";
    public static final String NODE_PREFIX = "n";
    public static final String TYPE_PREFIX = "type";
    public static final String DATA_PREFIX = "data";
    public static final String FUNCTION_ID_PREFIX = "function_id";
    public static final String STRING_PREFIX = "!";
    public static final String LINK_PREFIX = "@";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    private static Object dataSimplification(NodeBuilder builder, Node node) {
        DataStream dataStream = builder.set(node).getData();
        if (node.type == NodeType.STRING) {
            if (dataStream.length > NodeStorage.MAX_STORAGE_DATA_IN_DB)
                return LINK_PREFIX + node.id;
            else
                return STRING_PREFIX + String.valueOf(dataStream.readChars());
        } else {
            return dataStream.getObject();
        }
    }

    public static void toJsonRecursive(NodeBuilder builder, Map<String, Map<String, Object>> data, int depth, Node node) {
        String nodeName = NODE_PREFIX + node.id;
        if (data.get(nodeName) != null) return;

        Map<String, Object> links = new LinkedHashMap<>();
        data.put(nodeName, links);

        if (node.type != NodeType.VAR)
            links.put(TYPE_PREFIX, NodeType.toString(node.type));

        if (node.type < NodeType.VAR)
            links.put(DATA_PREFIX, dataSimplification(builder, node));

        if (node.type == NodeType.NATIVE_FUNCTION)
            links.put(FUNCTION_ID_PREFIX, node.functionId); // TODO Functions.toString

        node.listLinks((linkType, link, singleValue) -> {
            Node linkNode = link instanceof Long ? builder.get((Long) link).getNode() : (Node) link;
            String linkTypeStr = LinkType.toString(linkType);
            if (singleValue) {
                if (linkNode.type < NodeType.VAR)
                    links.put(linkTypeStr, dataSimplification(builder, linkNode));
                else if (depth > 0){
                    links.put(linkTypeStr, NODE_PREFIX + linkNode.id);
                    toJsonRecursive(builder, data, depth - 1, linkNode);
                }
            } else {
                Object linkObject = links.get(linkTypeStr);
                if (linkObject == null)
                    links.put(linkTypeStr, linkObject = new ArrayList<>());
                ArrayList linkList = (ArrayList) linkObject;

                if (linkNode.type < NodeType.VAR) // TODO exception
                    linkList.add(dataSimplification(builder, linkNode));
                else {
                    linkList.add(NODE_PREFIX + linkNode.id);
                    toJsonRecursive(builder, data, depth, linkNode);
                }
            }
        });
    }

    public static Map<String, Map<String, Object>> toMap(Node node) {
        Map<String, Map<String, Object>> data = new LinkedHashMap<>();
        toJsonRecursive(new NodeBuilder(), data, 15, node);
        return data;
    }

    // TODO delete Gson object
    private static Gson json = new GsonBuilder().setPrettyPrinting().create();
    public static String toJson(Node node) {
        return json.toJson(toMap(node));
    }
}
