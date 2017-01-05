package isden.mois.magellanlauncher.httpd;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import isden.mois.magellanlauncher.Constants;
import isden.mois.magellanlauncher.httpd.handlers.BookHandler;
import isden.mois.magellanlauncher.httpd.handlers.SQLHandler;
import isden.mois.magellanlauncher.httpd.handlers.StaticHandler;
import isden.mois.magellanlauncher.httpd.queries.SQLQuery;

/**
 * Created by isden on 05.01.17.
 */

public class HTTPD extends RouterNanoHTTPD {
    public final static int PORT = 8080;
    private static final int REQUEST_BUFFER_LEN = 512;

    private File root;
    private Context ctx;
    private String destination;

    public HTTPD(Context context) {
        super(PORT);
        this.ctx = context;
        this.root = context.getFilesDir().getAbsoluteFile();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.destination = prefs.getString("library_path", "/flash/Books");

        this.addMappings();
    }

    @Override
    public void addMappings() {
        setNotFoundHandler(Error404UriHandler.class);

        addRoute("/", StaticHandler.class, this.root);
        addRoute("/api/sql", SQLHandler.class, this.ctx);
        addRoute("/api/sql/:table", SQLHandler.class, this.ctx);
        addRoute("/api/book", BookHandler.class, this.ctx, destination);
        addRoute("/api/book/:MD5", BookHandler.class, this.ctx, destination);

        addRoute("/public/(.)+", StaticHandler.class, this.root);
    }

    public static Response badRequest(String error) {
        JSONObject obj = new JSONObject();
        obj.put("error", error);
        return newFixedLengthResponse(Status.BAD_REQUEST, Constants.JSON, obj.toString());
    }

    public static Response success() {
        return newFixedLengthResponse(Status.OK, Constants.JSON, Constants.SUCCESS_MESSAGE);
    }

    public static Response serveSQL(SQLQuery query, RouterNanoHTTPD.UriResource uriResource) {
        Context ctx = uriResource.initParameter(Context.class);

        try {
            String result = query.execute(ctx);
            return newFixedLengthResponse(Status.OK, Constants.JSON, result);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    public static JSONObject parseJSONParams(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> headers = session.getHeaders();

        long size = Long.parseLong(headers.get("content-length"));
        ByteArrayOutputStream baos = null;
        DataOutput requestDataOutput = null;

        // Store the request in memory.
        baos = new ByteArrayOutputStream();
        requestDataOutput = new DataOutputStream(baos);

        // Read all the body.
        byte[] buf = new byte[REQUEST_BUFFER_LEN];
        int rlen = 0;
        while (size > 0) {
            rlen = session.getInputStream().read(buf, 0, (int) Math.min(size, REQUEST_BUFFER_LEN));
            size -= rlen;
            if (rlen > 0) {
                requestDataOutput.write(buf, 0, rlen);
            }
        }

        return JSON.parseObject(baos.toString());
    }
}
