package com.metabrain.djs;

import fi.iki.elonen.NanoHTTPD;

public class Controller extends NanoHTTPD {

    private static final int DEFAULT_HTTP_PORT = 778;


    public Controller(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return super.serve(session);
    }

    public static void main(String[] args) {
        new Controller(DEFAULT_HTTP_PORT);
    }
}
