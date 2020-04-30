package isden.mois.magellanlauncher.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import isden.mois.magellanlauncher.httpd.handlers.StaticHandler;

public class GetFiles extends AsyncTask<String[], Void, Boolean> implements FilenameFilter {
    private static final String TAG = "GetFiles";
    private static final String[] TYPES = {
            ".js",
            ".css",
            ".html",
            ".svg",
            ".woff2",
            ".jpg",
            ".png",
    };

    private ParentTask parentTask;

    GetFiles(ParentTask parent) {
        this.parentTask = parent;
    }

    @Override
    protected Boolean doInBackground(String[]... params) {
        String[] fileList = params[0];
        if (fileList != null && fileList.length > 0) {
            if (!removePreviousFiles()) {
                return false;
            }

            for (String url : fileList) {
                if (!downloadFile(url)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean completed) {
        if (completed) {
            parentTask.done();
        }
        else {
            parentTask.fail();
        }
    }

    private String getFileName(String url) {
        int index = url.lastIndexOf('/') + 1;
        return url.substring(index);
    }

    private boolean removePreviousFiles() {
        File root = StaticHandler.root;

        for (File file: root.listFiles(this)) {
            if (!file.delete()) {
                Log.e(TAG, "Cannot delete file: " + file);
                return false;
            }
        }

        return true;
    }

    private boolean downloadFile(String urlString) {
        String filename = getFileName(urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream input = urlConnection.getInputStream();
            OutputStream output = new FileOutputStream(new File(StaticHandler.root, filename));

            try {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } finally {
                output.close();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        for (String ext : TYPES) {
            if (name.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }
}
