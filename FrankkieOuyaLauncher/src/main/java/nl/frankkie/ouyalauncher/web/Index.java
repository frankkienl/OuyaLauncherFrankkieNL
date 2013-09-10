package nl.frankkie.ouyalauncher.web;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public class Index extends WebPage {

    public static final String page = "<section id=\"info\">\n" +
            "\t\t\t\t<div>\n" +
            "\t\t\t\t\t<h2>Home</h2>\n" +
            "\t\t\t\t</div>\n" +
            "\n" +
            "\t\t\t</section>";

    public static final String pageAndroid = "<section id=\"info\">\n" +
            "\t\t\t\t<div>\n" +
            "\t\t\t\t\t<h2>Home</h2>\n" +
            "<a href=\"http://frankkie.nl/android/dev/BaxyRemote.apk\">Download Android App</a>" +
            "\t\t\t\t</div>\n" +
            "\n" +
            "\t\t\t</section>";

    @Override
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;

        StringBuilder sb= new StringBuilder();
        sb.append(WebUtil.top);
        if (containsIgnoreCase(header.get("user-agent"),"Android")){
            sb.append(pageAndroid);
        } else {
            sb.append(page);
        }
        sb.append(WebUtil.footer);
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
        return response;
    }

    public boolean containsIgnoreCase(String s1, String s2){
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

}
