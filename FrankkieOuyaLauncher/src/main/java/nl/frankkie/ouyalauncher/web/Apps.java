package nl.frankkie.ouyalauncher.web;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import nl.frankkie.ouyalauncher.WebserverActivity;
import nl.frankkie.ouyalauncher.databaserows.DatabaseAppInfo;
import nl.wotuu.database.DatabaseOpenHelper;

/**
 * Created by FrankkieNL on 14-8-13.
 */
public class Apps extends WebPage {

    public NanoHTTPD.Response serve(String uri, NanoHTTPD.Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        NanoHTTPD.Response response = null;

        DatabaseOpenHelper helper = DatabaseOpenHelper.CreateInstance(WebserverActivity.context);
        Cursor cursor = helper.WriteableDatabase.rawQuery("SELECT id FROM appinfo", null);
        if (cursor.getCount() == 0) {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, "text/html", "Apps not found !\nGo to applist and restart webserver.");
        }
        ArrayList<DatabaseAppInfo> appInfoArrayList = new ArrayList<DatabaseAppInfo>();
        while (cursor.moveToNext()) {
            DatabaseAppInfo appInfo = new DatabaseAppInfo(cursor.getInt(0));
            appInfo.OnLoad();
            appInfoArrayList.add(appInfo);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>BAXY - Apps</title></head><body>" +
                "<a href=\"/\">HOME</a><br /><br />");
        sb.append("<table>\n");
        for (DatabaseAppInfo info : appInfoArrayList){
            sb.append("<tr><td><img src=\"/appImage?packageName="+info.packageName+"\" alt=\"\" /><td>" + info.title + "<td>" + info.packageName + "</tr>\n");
        }
        sb.append("\n</table>");
        sb.append("</body></html>");
        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK,"text/html",sb.toString());
        return response;
    }
}
