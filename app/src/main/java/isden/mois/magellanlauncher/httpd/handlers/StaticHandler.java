package isden.mois.magellanlauncher.httpd.handlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;

import static fi.iki.elonen.NanoHTTPD.getMimeTypeForFile;
import static fi.iki.elonen.NanoHTTPD.newChunkedResponse;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

/**
 * Created by isden on 05.01.17.
 */

public class StaticHandler extends DefaultHandler {
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

    private String normalizeUrl(String url) {
        if (url.charAt(0) == '/') {
            url = url.substring(1);
        }

        if (url.startsWith("public/")) {
            return url.substring(7);
        }

        return "index.html";
    }

    public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        File fileOrdirectory = uriResource.initParameter(File.class);
        String uri = normalizeUrl(session.getUri());
        fileOrdirectory = new File(fileOrdirectory, uri);

        if (!fileOrdirectory.exists() || !fileOrdirectory.isFile()) {
            return new RouterNanoHTTPD.Error404UriHandler().get(uriResource, urlParams, session);
        } else {
            try {
                Response response = newChunkedResponse(getStatus(), getMimeTypeForFile(uri), fileToInputStream(fileOrdirectory));
                response.addHeader("Content-Encoding",  "gzip");
                response.addHeader("Cache-Control", "max-age=2592000");
                response.addHeader("Content-Length", "" + fileOrdirectory.length());
                return response;
            } catch (IOException ioe) {
                return newFixedLengthResponse(Status.REQUEST_TIMEOUT, "text/plain", (String) null);
            }
        }
    }

    private BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException {
        return new BufferedInputStream(new FileInputStream(fileOrdirectory));
    }
}
