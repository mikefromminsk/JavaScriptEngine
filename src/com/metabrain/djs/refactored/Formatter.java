package com.metabrain.djs.refactored;

import com.google.gson.Gson;
import com.metabrain.djs.refactored.node.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Formatter {

    static NodeBuilder builder = new NodeBuilder();

    private static String dataSimplification(Node node) {
        DataStream dataStream = builder.set(node).getData();
        if (node.type == NodeType.STRING) {
            if (dataStream.length > NodeStorage.MAX_STORAGE_DATA_IN_DB)
                return "@" + node.id;
            else
                return "!" + String.valueOf(dataStream.readChars());
        } else {
            return String.valueOf(dataStream.readChars());
        }
    }

    public static void toJsonRecursive(Map<String, Map<String, Object>> data, int depth, Node node) {
        String nodeName = "node" + node.id;
        if (data.get(nodeName) != null) return;

        Map<String, Object> links = new HashMap<>();
        data.put(nodeName, links);

        links.put("type", NodeType.toString(node.type));

        if (node.type < NodeType.VAR)
            links.put("data", dataSimplification(node));

        if (node.type == NodeType.FUNCTION)
            links.put("functionId", node.functionId); // TODO Functions.toString

        node.listLinks((linkType, link, singleValue) -> {
            Node linkNode = link instanceof Long ? builder.get((Long) link).getNode() : (Node) link;
            String linkTypeStr = LinkType.toString(linkType);
            if (singleValue) {
                if (linkNode.type < NodeType.VAR)
                    links.put(linkTypeStr, dataSimplification(linkNode));
                else if (depth > 0)
                    toJsonRecursive(data, depth - 1, linkNode);
            } else {
                Object linkObject = links.get(linkTypeStr);
                if (linkObject == null)
                    links.put(linkTypeStr, linkObject = new ArrayList<>());
                ArrayList linkList = (ArrayList) linkObject;
                if (linkNode.type < NodeType.VAR) // TODO exception
                    linkList.add(dataSimplification(linkNode));
                else {
                    linkList.add("node" + linkNode);
                    toJsonRecursive(data, depth, linkNode);
                }
            }
        });
    }

    // TODO delete Gson object
    private static Gson json = new Gson();

    public static String toJson(Node node) {
        Map<String, Map<String, Object>> data = new HashMap<>();
        toJsonRecursive(data, 15, node);
        return json.toJson(data);
    }
}
