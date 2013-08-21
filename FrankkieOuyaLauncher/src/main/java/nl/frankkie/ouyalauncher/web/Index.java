package nl.frankkie.ouyalauncher.web;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public class Index extends WebPage {

    public static final String page = "<html>" +
            "<head><title>BAXY</title></head>" +
            "<body>" +
            "<h2>BAXY</h2>" +
            "<ul>" +
            "<li><a href=\"/upload\">Upload</a>" +
            "<li><a href=\"/files\">Files</a>" +
            "<li><a href=\"/apps\">Apps</a>" +
            "</ul>" +
            "</body>" +
            "</html>";

    @Override
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;
        StringBuilder sb= new StringBuilder();
        sb.append(page);
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
        return response;
    }

}
