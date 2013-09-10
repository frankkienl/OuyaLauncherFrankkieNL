package nl.frankkie.ouyalauncher.web;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by FrankkieNL on 24-8-13.
 */
public class GetDirectory extends WebPage {
    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        StringBuilder sb = new StringBuilder();
        String path = "/sdcard/";
        try {
            path = parms.get("path");
            if (path == null){
                path = "/sdcard/";
            }
        } catch (Exception e) {
            path = "/sdcard/";
        }
        if (!path.endsWith("/")){
            path+="/";
        }
        sb.append("[");
        try {
            File dir = new File(path);
            String[] list = dir.list();
            for (int i=0; i<list.length; i++){
                String s = list[i];
                File temp = new File(path + s);
                sb.append("{ \"name\": \"").append(s).append("\", " +
                        "\"isDir\": ").append(temp.isDirectory()).append(", " +
                        "\"bytes\": ").append(temp.length()).append("}\n");
                if (i<list.length-1){
                    sb.append(",");
                }
            }
        } catch (Exception e){
            new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain", "Error");
        }
        sb.append("]");

        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/json", sb.toString());
    }
}