package nl.frankkie.ouyalauncher.web;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 11-8-13.
 */
public class Upload extends WebPage {
    public static final String page = "\t\t\t<section id=\"upload\">\n" +
            "\t\t\t\t<div>\n" +
            "\t\t\t\t\t<h2>Upload</h2>\n" +
            "\t\t\t\t\t<form enctype=\"multipart/form-data\" action=\"/upload\" method=\"post\">\n" +
            "\t\t\t\t\t\t<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"2000000\">\n" +
            "\t\t\t\t\t\tFile: <input name=\"uploadFile\" type=\"file\"><br>\n" +
            "\t\t\t\t\t\tPath: <input type=\"text\" name=\"path\" value=\"/sdcard/uploads/\"><br>\n" +
            "\t\t\t\t\t\t<input name=\"gezien\" value=\"ja\" type=\"hidden\">\n" +
            "\t\t\t\t\t\t<input type=\"submit\" value=\"Start Upload\" name=\"submitButton\">\n" +
            "\t\t\t\t\t</form>\n" +
            "\t\t\t\t</div>\n" +
            "\t\t\t</section>";

    public static final String pageDone = "\t\t\t<section id=\"upload\">\n" +
            "\t\t\t\t<div>\n" +
            "\t\t\t\t\t<h2>Upload</h2>\n" +
            "DONE" +
            "\t\t\t\t</div>\n" +
            "\t\t\t</section>";

    @Override
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;
        if (method.equals(NanoHTTPD.Method.POST)) {
            String tempFilename = files.get("uploadFile");
            if (tempFilename != null) {
                //rename
                File file = new File(tempFilename);
                try {
                    //
                    String pathString = parms.get("path");
                    if (!pathString.startsWith("/")){
                        pathString = "/sdcard/uploads/";
                    }
                    File path = new File(pathString);
                    path.mkdirs();
                    //
                    file.renameTo(new File(pathString + parms.get("uploadFile")));
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, WebUtil.top + pageDone + WebUtil.footer);
                } catch (Exception e) {
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Upload Error !");
                }
            } else {
                response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, "Upload Error !");
            }
            return response;
        }
        ///
        StringBuilder sb = new StringBuilder();
        sb.append(WebUtil.top);
        sb.append(page);
        sb.append(WebUtil.footer);
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, sb.toString());
        return response;

    }
}
