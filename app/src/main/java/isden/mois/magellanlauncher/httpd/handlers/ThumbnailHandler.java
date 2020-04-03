package isden.mois.magellanlauncher.httpd.handlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import static fi.iki.elonen.NanoHTTPD.newChunkedResponse;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static fi.iki.elonen.router.RouterNanoHTTPD.normalizeUri;
import static fi.iki.elonen.router.RouterNanoHTTPD.getMimeTypeForFile;

/**
 * Created by isden on 16.06.17.
 */
public class ThumbnailHandler extends RouterNanoHTTPD.DefaultHandler {
    @Override
    public String getText() {
        throw new IllegalStateException("this method should not be called");
    }

    @Override
    public String getMimeType() {
        throw new IllegalStateException("this method should not be called");
    }

    @Override
    public Response.IStatus getStatus() {
        return Response.Status.OK;
    }

    public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String baseUri = uriResource.getUri();
        String realUri = normalizeUri(session.getUri());
        for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++) {
            if (baseUri.charAt(index) != realUri.charAt(index)) {
                realUri = normalizeUri(realUri.substring(index));
                break;
            }
        }
        File file = new File("/", realUri);


        if (!file.exists() || !file.isFile()) {
            return new Error404UriHandler().get(uriResource, urlParams, session);
        } else {
            try {
                Response response =  newChunkedResponse(getStatus(), getMimeTypeForFile(file.getName()), fileToInputStream(file));

                response.addHeader("Cache-Control", "max-age=2592000");

                return response;
            } catch (IOException ioe) {
                return newFixedLengthResponse(Status.REQUEST_TIMEOUT, "text/plain", (String) null);
            }
        }
    }

    protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException {
        return new BufferedInputStream(new FileInputStream(fileOrdirectory));
    }
}
