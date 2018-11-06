package com.metabrain.djs.refactored;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabrain.djs.refactored.node.*;

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

    static NodeBuilder builder = new NodeBuilder();
    static final String NODE_PREFIX = "n";
    static final String TYPE_PREFIX = "type";
    static final String DATA_PREFIX = "data";
    static final String FUNCTION_ID_PREFIX = "function_id";
    static final String STRING_PREFIX = "!";
    static final String LINK_PREFIX = "@";

    private static String dataSimplification(Node node) {
        DataStream dataStream = builder.set(node).getData();
        if (node.type == NodeType.STRING) {
            if (dataStream.length > NodeStorage.MAX_STORAGE_DATA_IN_DB)
                return LINK_PREFIX + node.id;
            else
                return STRING_PREFIX + String.valueOf(dataStream.readChars());
        } else {
            return String.valueOf(dataStream.readChars());
        }
    }

    public static void toJsonRecursive(Map<String, Map<String, Object>> data, int depth, Node node) {
        String nodeName = NODE_PREFIX + node.id;
        if (data.get(nodeName) != null) return;

        Map<String, Object> links = new LinkedHashMap<>();
        data.put(nodeName, links);

        if (node.type != NodeType.VAR)
            links.put(TYPE_PREFIX, NodeType.toString(node.type));

        if (node.type < NodeType.VAR)
            links.put(DATA_PREFIX, dataSimplification(node));

        if (node.type == NodeType.NATIVE_FUNCTION)
            links.put(FUNCTION_ID_PREFIX, node.functionId); // TODO Functions.toString

        node.listLinks((linkType, link, singleValue) -> {
            Node linkNode = link instanceof Long ? builder.get((Long) link).getNode() : (Node) link;
            String linkTypeStr = LinkType.toString(linkType);
            if (singleValue) {
                if (linkNode.type < NodeType.VAR)
                    links.put(linkTypeStr, dataSimplification(linkNode));
                else if (depth > 0){
                    links.put(linkTypeStr, NODE_PREFIX + linkNode.id);
                    toJsonRecursive(data, depth - 1, linkNode);
                }
            } else {
                Object linkObject = links.get(linkTypeStr);
                if (linkObject == null)
                    links.put(linkTypeStr, linkObject = new ArrayList<>());
                ArrayList linkList = (ArrayList) linkObject;

                if (linkNode.type < NodeType.VAR) // TODO exception
                    linkList.add(dataSimplification(linkNode));
                else {
                    linkList.add(NODE_PREFIX + linkNode.id);
                    toJsonRecursive(data, depth, linkNode);
                }
            }
        });
    }

    // TODO delete Gson object
    private static Gson json = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Node node) {
        Map<String, Map<String, Object>> data = new LinkedHashMap<>();
        toJsonRecursive(data, 15, node);
        return json.toJson(data);
    }
}
