package isden.mois.magellanlauncher.httpd.handlers;

/**
 * Created by isden on 05.01.17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.onyx.android.sdk.data.cms.OnyxCmsCenter;
import com.onyx.android.sdk.data.cms.OnyxMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import isden.mois.magellanlauncher.httpd.HTTPD;
import isden.mois.magellanlauncher.httpd.queries.SetBookStatus;

public class BookHandler extends RouterNanoHTTPD.DefaultHandler {
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

    public Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Context context = uriResource.initParameter(Context.class);
        String uploadDir = uriResource.initParameter(1, String.class);

        // Update charset.
        Map<String, String> headers = session.getHeaders();
        String contentType = headers.get("content-type");
        contentType = contentType.replaceFirst("form-data;", "form-data; charset=utf-8;");
        headers.put("content-type", contentType);

        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
            insertBook(session.getParameters(), files, context, uploadDir);

            return HTTPD.success();
        } catch (Exception e) {
            return HTTPD.badRequest(e.getMessage());
        }
    }

    public Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Context context = uriResource.initParameter(Context.class);
        String MD5 = urlParams.get("MD5");
        OnyxMetadata metadata = OnyxCmsCenter.getMetadataByMD5(context, MD5);

        if (metadata != null) {
            File book = new File(metadata.getLocation());
            OnyxCmsCenter.deleteBook(context, metadata);
            if (book.exists()) {
                book.delete();
            }
        }
        else {
            return HTTPD.badRequest("Book is not found");
        }

        return HTTPD.success();
    }

    private void insertBook(Map<String, List<String>> params, Map<String, String> files, Context ctx, String uploadDir) throws Exception {
        String author = params.get("author").get(0);
        String title = params.get("title").get(0);
        String fileName = params.get("file").get(0);
        String imageKey = params.containsKey("image") ? "image" : "image1";

        File file = new File(files.get("file"));

        ArrayList<String> authors = new ArrayList<>();
        authors.add(author);

        File to = new File(uploadDir, fileName);
        copy(file, to);

        OnyxMetadata metadata = OnyxCmsCenter.getMetadata(ctx, to);

        if (metadata == null) {
            metadata = OnyxMetadata.createFromFile(to);
            metadata.setTitle(title);
            metadata.setAuthors(authors);

            OnyxCmsCenter.insertMetadata(ctx, metadata);
        }
        else {
            metadata.setTitle(title);
            metadata.setAuthors(authors);

            OnyxCmsCenter.updateMetadata(ctx, metadata);
        }

        String image = null;
        if (files.containsKey(imageKey)) {
            image = files.get(imageKey);
        }

        if (image != null) {
            setThumbnail(metadata, image, ctx);
        }

        removeTemp(files.values());

        new SetBookStatus(0, metadata.getMD5()).execute(ctx);
    }

    private void setThumbnail(OnyxMetadata metadata, String image, Context ctx) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(image, options);

        if (bitmap != null) {
            OnyxCmsCenter.insertThumbnail(ctx, metadata, bitmap);
        }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void removeTemp(Collection<String> paths) {
        for (String path : paths) {
            File f = new File(path);

            if (f.exists()) {
                try {
                    f.delete();
                } catch (RuntimeException e) {
                    Log.e("HTTPD", e.toString());
                }
            }
        }
    }
}
