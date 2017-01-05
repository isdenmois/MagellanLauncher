package isden.mois.magellanlauncher.httpd.handlers;

/**
 * Created by isden on 05.01.17.
 */

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import isden.mois.magellanlauncher.httpd.HTTPD;
import isden.mois.magellanlauncher.httpd.queries.SQLGet;
import isden.mois.magellanlauncher.httpd.queries.SQLQuery;
import isden.mois.magellanlauncher.httpd.queries.SQLUpdate;

import static isden.mois.magellanlauncher.httpd.HTTPD.serveSQL;

public class SQLHandler extends RouterNanoHTTPD.DefaultHandler {
    @Override
    public String getText() {
        throw new IllegalStateException("this method should not be called");
    }

    @Override
    public String getMimeType() {
        throw new IllegalStateException("this method should not be called");
    }

    @Override
    public IStatus getStatus() {
        return Status.OK;
    }

    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        return serveSQL(new SQLGet(session.getParameters()), uriResource);
    }

    public Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        if (method.equals("PATCH")) {
            SQLQuery query = null;

            try {
                query = new SQLUpdate(urlParams.get("table"), HTTPD.parseJSONParams(session));
            } catch (Exception e) {
                return HTTPD.badRequest(e.getMessage());
            }

            return serveSQL(query, uriResource);
        }

        return super.other(method, uriResource, urlParams, session);
    }
}
