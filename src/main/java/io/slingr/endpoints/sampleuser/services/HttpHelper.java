package io.slingr.endpoints.sampleuser.services;

import java.util.Date;
import java.util.Map;

/**
 * Class that helps to build the web UI of the endpoint
 *
 * Created by lefunes on 06/12/16.
 */
public class HttpHelper {

    public static String formatPage(StringBuilder page) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("\t<head>\t\n");
        sb.append("\t\t<meta charset=\"utf-8\">\n");
        sb.append("\t\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
        sb.append("\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append("\t\t<title>Sample User endpoint</title>\n");
        sb.append("\t\t<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n");
        sb.append("\t\t<!--[if lt IE 9]>\n");
        sb.append("\t\t\t<script src=\"https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js\"></script>\n");
        sb.append("\t\t\t<script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\n");
        sb.append("\t\t<![endif]-->\n");
        sb.append("\t</head>\n");
        sb.append("\t<body>\n");
        sb.append("\t\t<div class=\"container\">\n");
        sb.append("\t\t\n");
        sb.append(page.toString());
        sb.append("\n");
        sb.append("\t\t</div>\n");
        sb.append("\t\t<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n");
        sb.append("\t\t<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>\n");
        sb.append("\t</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    public static void addPanel(StringBuilder sb, String title, Map<String, Object> properties){
        final StringBuilder sbPanel = new StringBuilder();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            sbPanel.append(String.format("<h4>%s <span class=\"label label-default\">%s</span></h4>", property.getKey(), property.getValue()));
        }
        addPanel(sb, title, sbPanel);
    }

    public static void addPanel(StringBuilder sb, String title, StringBuilder sbPanel){
        sb.append("<div class=\"row\">");
        sb.append("<div class=\"panel panel-default\">");
        sb.append(String.format("<div class=\"panel-heading\"><h2 class=\"panel-title\">%s</h2></div>", title));
        sb.append("<div class=\"panel-body\">");
        sb.append(sbPanel);
        sb.append("</div>");
        sb.append(String.format("<div class=\"panel-footer\"><h5>Date  <b>%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL</b></h5></div>", new Date()));
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<br />");
    }

    public static void addAlert(StringBuilder sbPanel, String type, String message){
        sbPanel.append("<div class=\"row\">");
        sbPanel.append(String.format("<div class=\"alert alert-%s\" role=\"alert\">%s</div>", type, message));
        sbPanel.append("</div>");
        sbPanel.append("<br />");
    }

    public static void addButton(String webhook, String path, StringBuilder sbPanel, String type, String label){
        sbPanel.append("<div class=\"row\">");
        sbPanel.append(String.format("<form action=\"%s/%s\" method=\"post\">", webhook, path));
        sbPanel.append(String.format("<button type=\"submit\" class=\"btn btn-%s btn-lg\">%s</button>", type, label));
        sbPanel.append("</form>");
        sbPanel.append("</div>");
        sbPanel.append("<br />");
    }
}
