package com.metabrain.djs.node;

import com.metabrain.gdb.Bytes;
import com.metabrain.gdb.InfinityArrayCell;

import java.io.InputStream;
import java.util.ArrayList;

public class Node implements InfinityArrayCell {

    public boolean isSaved;
    public Long id;
    public DataStream data;
    // TODO read external data in another thread
    public InputStream externalData;
    public byte type;
    public Integer functionId; // change to primitive long
    public Object value;
    public Object source;
    public Object title;
    public Object set;
    public Object _true;
    public Object _else;
    public Object exit;
    public Object _while;
    public Object _if;
    public Object prototype;
    public Object body;

    public ArrayList<Object> local;
    public ArrayList<Object> param;
    public ArrayList<Object> next;
    public ArrayList<Object> cell;
    public ArrayList<Object> prop;
    public ArrayList<Object> style;
    // after add new link you should add it to listLinks and parse function

    @Override
    public byte[] build() {
        ArrayList<Long> links = new ArrayList<>();
        listLinks((linkType, link, singleValue) -> {
                Long linkId = link instanceof Long ? (Long) link : ((Node) link).id;
                long dataLink = linkId * 256L + (long) linkType;
                links.add(dataLink);
        });
        return Bytes.fromLongList(links);
    }


    public interface NodeLinkListener {
        void get(byte linkType, Object link, boolean singleValue);
    }

    public void listLinks(NodeLinkListener linkListener) {
        if (linkListener == null)
            return;
        // sequences for well see in Formatter
        if (title != null)
            linkListener.get(LinkType.TITLE, title, true);
        if (value != null)
            linkListener.get(LinkType.VALUE, value, true);
        if (source != null)
            linkListener.get(LinkType.SOURCE, source, true);
        if (set != null)
            linkListener.get(LinkType.SET, set, true);
        if (_true != null)
            linkListener.get(LinkType.TRUE, _true, true);
        if (_else != null)
            linkListener.get(LinkType.ELSE, _else, true);
        if (exit != null)
            linkListener.get(LinkType.EXIT, exit, true);
        if (_while != null)
            linkListener.get(LinkType.WHILE, _while, true);
        if (_if != null)
            linkListener.get(LinkType.IF, _if, true);
        if (prototype != null)
            linkListener.get(LinkType.PROTOTYPE, prototype, true);
        if (body != null)
            linkListener.get(LinkType.BODY, body, true);
        if (local != null)
            for (Object item : local)
                linkListener.get(LinkType.LOCAL, item, false);
        if (param != null)
            for (Object item : param)
                linkListener.get(LinkType.PARAM, item, false);
        if (next != null)
            for (Object item : next)
                linkListener.get(LinkType.NEXT, item, false);
        if (prop != null)
            for (Object item : prop)
                linkListener.get(LinkType.PROP, item, false);
        if (cell != null)
            for (Object item : cell)
                linkListener.get(LinkType.CELL, item, false);
        if (style != null)
            for (Object item : style)
                linkListener.get(LinkType.STYLE, item, false);
    }

    @Override
    public void parse(byte[] data) {
        long[] links = Bytes.toLongArray(data);
        for (long dataLink : links) {
            byte linkType = (byte) (dataLink % 256);
            long linkId = (dataLink - linkType) / 256;
            switch (linkType) {
                case LinkType.VALUE:
                    value = linkId;
                    break;
                case LinkType.SOURCE:
                    source = linkId;
                    break;
                case LinkType.TITLE:
                    title = linkId;
                    break;
                case LinkType.SET:
                    set = linkId;
                    break;
                case LinkType.TRUE:
                    _true = linkId;
                    break;
                case LinkType.ELSE:
                    _else = linkId;
                    break;
                case LinkType.EXIT:
                    exit = linkId;
                    break;
                case LinkType.WHILE:
                    _while = linkId;
                    break;
                case LinkType.IF:
                    _if = linkId;
                    break;
                case LinkType.PROTOTYPE:
                    prototype = linkId;
                    break;
                case LinkType.BODY:
                    body = linkId;
                    break;
                case LinkType.LOCAL:
                    if (local == null)
                        local = new ArrayList<>();
                    local.add(linkId);
                    break;
                case LinkType.PARAM:
                    if (param == null)
                        param = new ArrayList<>();
                    param.add(linkId);
                    break;
                case LinkType.NEXT:
                    if (next == null)
                        next = new ArrayList<>();
                    next.add(linkId);
                    break;
                case LinkType.CELL:
                    if (cell == null)
                        cell = new ArrayList<>();
                    cell.add(linkId);
                    break;
                case LinkType.STYLE:
                    if (style == null)
                        style = new ArrayList<>();
                    style.add(linkId);
                    break;
            }
        }
    }
}
