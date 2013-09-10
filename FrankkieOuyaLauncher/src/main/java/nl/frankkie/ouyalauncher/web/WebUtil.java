package nl.frankkie.ouyalauncher.web;

/**
 * Created by FrankkieNL on 24-8-13.
 */
public class WebUtil {
    public static final String top = "\n" +
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "\t<head>\n" +
            "\t\t<meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\" />\n" +
            "\t\t<meta name=\"author\" content=\"www.frebsite.nl\" />\n" +
            "\t\t<meta name=\"viewport\" content=\"width=device-width initial-scale=1.0 maximum-scale=1.0 user-scalable=yes\" />\n" +
            "\t\t<meta name=\"robots\" content=\"noindex, nofollow\" />\n" +
            "\n" +
            "\t\t<title>BAXY REMOTE</title>\n" +
            "\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css\" />\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"/files/FrankkieOuyaLauncher/web/css/layout.css\" />\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"/files/FrankkieOuyaLauncher/web/css/mmenu.css\" />\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"/files/FrankkieOuyaLauncher/web/css/mmenu-sizing-large.css\" />\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"/files/FrankkieOuyaLauncher/web/css/mmenu-widescreen.css\" media=\"all and (min-width: 900px)\" />\n" +
            "\t\t<link type=\"text/css\" rel=\"stylesheet\" href=\"/files/FrankkieOuyaLauncher/web/css/mmenu-iconbar.css\" media=\"all and (min-width: 500px) and (max-width: 899px)\" />\n" +
            "\n" +
            "\t\t<script type=\"text/javascript\" src=\"/files/FrankkieOuyaLauncher/web/js/jquery.min.js\"></script>\n" +
            "\t\t<script type=\"text/javascript\" src=\"/files/FrankkieOuyaLauncher/web/js/jquery.mmenu.min.js\"></script>\n" +
            "\t\t<script type=\"text/javascript\">\n" +
            "\t\t\t$(function() {\n" +
            "\t\t\t\t$('#open-icon-menu a').click(function( e ) {\n" +
            "\t\t\t\t\te.stopImmediatePropagation();\n" +
            "\t\t\t\t\te.preventDefault();\n" +
            "\t\t\t\t\t$('#menu').trigger( 'toggle.mm' );\n" +
            "\t\t\t\t});\n" +
            "\n" +
            "\t\t\t\t$('#menu').mmenu({\n" +
            "\t\t\t\t\tonClick: {\n" +
            "\t\t\t\t\t\tsetLocationHref: true,\n" +
            "\t\t\t\t\t\tsetSelected: false,\n" +
            "\t\t\t\t\t\tcallback: function()\n" +
            "\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\tvar href = $(this).attr( 'href' );\n" +
            "\t\t\t\t\t\t\tsetTimeout(\n" +
            "\t\t\t\t\t\t\t\tfunction()\n" +
            "\t\t\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\t\t$('html, body').animate({\n" +
            "\t\t\t\t\t\t\t\t\t\tscrollTop: $( href ).offset().top\n" +
            "\t\t\t\t\t\t\t\t\t});\n" +
            "\t\t\t\t\t\t\t\t}, 10\n" +
            "\t\t\t\t\t\t\t);\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t});\n" +
            "\t\t\t});\n" +
            "\t\t</script>\n" +
            "\t</head>\n" +
            "\t<body>\n" +
            "\t\t<div id=\"page\">\n" +
            "\t\t\t<a id=\"open-icon-header\" href=\"#menu\"></a>";

    public static final String footer = "\n" +
            "\t\t\t<nav id=\"menu\">\n" +
            "\t\t\t\t<ul>\n" +
            "\t\t\t\t\t<li id=\"open-icon-menu\"><a href=\"#\"><i class=\"icon icon-white icon-th-list\"></i></a></li>\n" +
            "\t\t\t\t\t<li><a href=\"/index\">\n" +
            "\t\t\t\t\t\t<i class=\"icon icon-white icon-home\"></i>\n" +
            "\t\t\t\t\t\tHome</a></li>\n" +
            "\t\t\t\t\t<li><a href=\"/files\">\n" +
            "\t\t\t\t\t\t<i class=\"icon icon-white icon-th\"></i>\n" +
            "\t\t\t\t\t\tFiles</a></li>\n" +
            "\t\t\t\t\t<li><a href=\"/upload\">\n" +
            "\t\t\t\t\t\t<i class=\"icon icon-white icon-bold\"></i>\n" +
            "\t\t\t\t\t\tUpload</a></li>\n" +
            "\t\t\t\t</ul>\n" +
            "\t\t\t</nav>\n" +
            "\n" +
            "\t\t</div> <!--end of page-->\n" +
            "\t</body>\n" +
            "</html>";
}
