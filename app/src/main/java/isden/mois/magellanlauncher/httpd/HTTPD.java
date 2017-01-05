package isden.mois.magellanlauncher.httpd;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import java.io.File;

import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import isden.mois.magellanlauncher.Constants;
import isden.mois.magellanlauncher.httpd.handlers.SQLHandler;
import isden.mois.magellanlauncher.httpd.handlers.StaticHandler;
import isden.mois.magellanlauncher.httpd.queries.SQLQuery;

/**
 * Created by isden on 05.01.17.
 */

public class HTTPD extends RouterNanoHTTPD {
    private final static int PORT = 8080;
    private File root;
    private Context ctx;

    public HTTPD(Context context) {
        super(PORT);
        this.ctx = context;
        this.root = context.getFilesDir().getAbsoluteFile();

        this.addMappings();
    }

    @Override
    public void addMappings() {
        setNotFoundHandler(Error404UriHandler.class);

        addRoute("/", StaticHandler.class, this.root);
        addRoute("/api/sql", SQLHandler.class, this.ctx);
        addRoute("/public/(.)+", StaticHandler.class, this.root);
        addRoute("/(.)+", StaticHandler.class, this.root);
    }

    public static Response serveSQL(SQLQuery query, RouterNanoHTTPD.UriResource uriResource) {
        Context ctx = uriResource.initParameter(Context.class);

        try {
            String result = query.execute(ctx);
            return newFixedLengthResponse(Status.OK, Constants.JSON, result);
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return newFixedLengthResponse(Status.BAD_REQUEST, Constants.JSON, obj.toString());
        }
    }
}
