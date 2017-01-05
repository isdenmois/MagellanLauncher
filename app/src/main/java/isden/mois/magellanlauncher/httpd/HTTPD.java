package isden.mois.magellanlauncher.httpd;

import android.content.Context;

import java.io.File;

import fi.iki.elonen.router.RouterNanoHTTPD;
import isden.mois.magellanlauncher.httpd.handlers.SQLHandler;
import isden.mois.magellanlauncher.httpd.handlers.StaticHandler;

/**
 * Created by isden on 05.01.17.
 */

public class HTTPD extends RouterNanoHTTPD {
    private final static int PORT = 8080;
    private File root;

    public HTTPD(Context context) {
        super(PORT);
        this.root = context.getFilesDir().getAbsoluteFile();

        this.addMappings();
    }

    @Override
    public void addMappings() {
        setNotFoundHandler(Error404UriHandler.class);

        addRoute("/", StaticHandler.class, this.root);
        addRoute("/api/sql", SQLHandler.class);
        addRoute("/public/(.)+", StaticHandler.class, this.root);
        addRoute("/(.)+", StaticHandler.class, this.root);
    }
}
