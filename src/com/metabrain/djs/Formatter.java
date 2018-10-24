package com.metabrain.djs;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Formatter {

    public static String toJson(Node node) {
        Map<String, Map<String, Object>> data = new HashMap<>();
        toJsonRecursive(data, 15, node);
        return mapToJson(data);
    }

    static String dataSimplification(Node linkNode) {
        byte linkNodeType = linkNode.getType();
        if (linkNodeType == NodeType.BOOL || linkNodeType == NodeType.NUMBER) {
            return linkNode.getData().readString();
        } else if (linkNodeType == NodeType.STRING) {
            DataStream dataStream = linkNode.getData();
            if (dataStream.size() > NodeStorage.MAX_STORAGE_DATA_IN_DB)
                return "@" + linkNode.getId();
            else
                return "!" + dataStream.readString();
        }
        return null;
    }

    public static void toJsonRecursive(Map<String, Map<String, Object>> data, int depth, Node node) {
        String nodeName = "node" + node.getId();
        if (data.get(nodeName) != null) return;

        Map<String, Object> links = new HashMap<>();
        data.put(nodeName, links);

        links.put("type", NodeType.toString(node.getType()));

        String selfDataSimplification = dataSimplification(node);
        if (selfDataSimplification != null)
            links.put("data", selfDataSimplification);

        if (node.getType() == NodeType.FUNCTION)
            links.put("functionId", Functions.toString(node.getFunctionId()));

        node.listLinks((linkType, linkId, singleValue) -> {
            String linkTypeStr = LinkType.toString(linkType);
            Node linkNode = NodeStorage.getInstance().get(linkId, new Node());
            String linkDataSimplification = dataSimplification(linkNode);
            if (singleValue) {
                if (linkDataSimplification != null)
                    links.put(linkTypeStr, linkDataSimplification);
                else if (depth > 0)
                    toJsonRecursive(data, depth - 1, linkNode);
            } else {
                Object linkObject = links.get(linkTypeStr);
                if (linkObject == null)
                    links.put(linkTypeStr, linkObject = new ArrayList<>());
                ArrayList<Object> linkList = (ArrayList<Object>) linkObject;
                if (linkDataSimplification != null)
                    linkList.add(linkDataSimplification);
                else {
                    linkList.add("node" + linkNode);
                    toJsonRecursive(data, depth, linkNode);
                }
            }
        });
    }

    // TODO delete Gson object
    private static Gson json = new Gson();

    private static String mapToJson(Map<String, Map<String, Object>> data) {
        return json.toJson(data);
    }
}
