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
        return HTTPD.serveSQL(new SQLGet(session.getParameters()), uriResource);
    }
}
