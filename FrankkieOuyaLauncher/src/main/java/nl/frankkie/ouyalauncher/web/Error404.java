package nl.frankkie.ouyalauncher.web;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public class Error404 extends WebPage {
    @Override
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, not found");
    }
}
