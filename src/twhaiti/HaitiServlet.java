/*
 * This is a webservice to convert the latest data on Twitter to PFIF.
 * @author     Chirag Shah <chiragshah1@gmail.com>
 * @license    Public Domain
 */
package twhaiti;

import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.google.appengine.api.urlfetch.*;
import com.google.appengine.api.urlfetch.HTTPHeader;

/*
 * URL: http://twitter-haiti.appspot.com/pfif?role=provide
 */
@SuppressWarnings("serial")
public class HaitiServlet extends HttpServlet {
    private static final URLFetchService fetch = URLFetchServiceFactory.getURLFetchService();
    private static final String HAITI_SEARCH_QUERY = "&ands=&phrase=&ors=%23need+%23imok+%23damage+%23injured&nots=RT+via+epiccolorado+text+tweakthetweet+roubboy+posting+twitagsearch+openstreetmap&tag=haiti&lang=all&from=&to=&ref=&near=&since=&until=&rpp=100";
        
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {        
        if(!"provide".equals(req.getParameter("role"))) {
            resp.sendError(400, "Only supports the 'provide' role. Send role=provide in the query");
            return;
        } 
        
        String query = HaitiServlet.HAITI_SEARCH_QUERY;
        String since_id = "7816973595";
        if(null != req.getParameter("since_id") && !"".equals(req.getParameter("since_id"))) {
           since_id = req.getParameter("since_id");
        }
        
        query += "&since_id="+since_id;
        
        URL url = new URL("http://search.twitter.com/search.json?q=" + query);
        HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET);
        httpRequest.addHeader(new HTTPHeader("User-Agent", "Haiti-PFIF-Converter"));
        HTTPResponse httpResponse = fetch.fetch(httpRequest);
        
        JSONObject twitterResp;
        JSONArray results;
        try {
            twitterResp = new JSONObject(new String(httpResponse.getContent()));
            results = twitterResp.getJSONArray("results");
            twitterResp.remove("results");
            System.err.println(twitterResp.getString("max_id"));
        } catch (JSONException e) {
            resp.sendError(400, "Bad data from Twitter. Try again. Error: " + httpResponse.getResponseCode());
            return;
        }
                
        resp.setContentType("text/xml");
        resp.getWriter().print(PFIF.twitterToPFIF(results));
    }
}
