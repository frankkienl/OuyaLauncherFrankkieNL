package nl.frankkie.ouyalauncher.web;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public abstract class WebPage {
    public abstract NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files);
}