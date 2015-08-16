package isden.mois.magellanlauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import isden.mois.magellanlauncher.holders.HistoryDetail;

import java.io.File;
import java.util.*;

public class Onyx {
    private static final String CONTENT_URI = "content://com.onyx.android.sdk.OnyxCmsProvider/";

    public static List<Metadata> getRecentReading(Context ctx, int limit) {
        List<Metadata> result = new LinkedList<Metadata>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String history_clean_limit = prefs.getString("history_clean_limit", "20000");

        // Cause ContentProvider is Facade it impossible use JOIN's
        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "library_metadata"),
                new String[]{
                        "MD5",
                        "Authors",
                        "Title",
                        "Name",
                        "NativeAbsolutePath",
                        "Progress",
                        "MAX(LastAccess) AS LastAccess"
                },
                "Title IS NOT NULL) GROUP BY (MD5",
                null,
                "LastAccess DESC " + (limit <= 0 ? "" : "LIMIT " + limit)
        );

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String md5 = c.getString(c.getColumnIndex("MD5"));
                    Cursor hc = ctx.getContentResolver().query(
                            Uri.parse(CONTENT_URI + "library_history"),
                            new String[]{"SUM(EndTime - StartTime) AS Time"},
                            "MD5 = ? AND (EndTime - StartTime) > " + history_clean_limit,
                            new String[]{md5},
                            null
                    );
                    Cursor tc = ctx.getContentResolver().query(
                            Uri.parse(CONTENT_URI + "library_thumbnail"),
                            null,
                            "Source_MD5 = ? AND Thumbnail_Kind = ?",
                            new String[]{md5, "Middle"},
                            null
                    );
                    result.add(createMetadata(c, hc, tc));

                    if (hc != null) {
                        hc.close();
                    }
                    if (tc != null) {
                        tc.close();
                    }
                } while (c.moveToNext());
            }
            c.close();
        }

        return result;
    }

    private static Metadata createMetadata(Cursor c, Cursor hc, Cursor tc) {
        Metadata metadata = new Metadata();
        metadata.md5 = c.getString(c.getColumnIndex("MD5"));
        metadata.filename = c.getString(c.getColumnIndex("Name"));
        metadata.title = c.getString(c.getColumnIndex("Title"));
        metadata.author = c.getString(c.getColumnIndex("Authors"));
        metadata.filePath = c.getString(c.getColumnIndex("NativeAbsolutePath"));

        String[] progress = c.getString(c.getColumnIndex("Progress")).split("/");
        if (progress.length == 2) {
            metadata.progress = Integer.parseInt(progress[0]);
            metadata.size = Integer.parseInt(progress[1]);
        }

        if (tc != null && tc.moveToFirst()) {
            metadata.thumbnail = tc.getString(tc.getColumnIndex("_data"));
        }
        if (hc != null && hc.moveToFirst()) {
            metadata.totalTime = hc.getLong(hc.getColumnIndex("Time"));
        }

        metadata.lastAccess = c.getLong(c.getColumnIndex("LastAccess"));

        return metadata;
    }

    public static Bitmap getThumbnail(Metadata metadata) {
        if (metadata.thumbnail != null) {
            File thumbFile = new File(metadata.thumbnail);
            if (thumbFile.exists()) {
                return BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
            }
        }
        return null;
    }

    public static String getTotalTime(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String limit = prefs.getString("history_clean_limit", "20000");

        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "library_history"),
                new String[]{"SUM(EndTime - StartTime) AS Time"},
                "(EndTime - StartTime) > " + limit,
                null,
                null
        );

        if (c != null) {
            if(c.moveToFirst()) {
                long totalTime = c.getLong(0);
                return IsdenTools.prettyTime(totalTime);
            }
            c.close();
        }
        return "0";
    }

    public static void cleanDirtyHistory(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String limit = prefs.getString("history_clean_limit", "20000");

        ctx.getContentResolver().delete(
            Uri.parse(CONTENT_URI + "library_history"),
            "(EndTime - StartTime) < " + limit,
            null
        );
    }

    public static long getFirstTime(Context ctx, Metadata data) {
        if (data.md5 == null) {
            return 0;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String limit = prefs.getString("history_clean_limit", "20000");

        Cursor c = ctx.getContentResolver().query(
            Uri.parse(CONTENT_URI + "library_history"),
            new String[]{"StartTime"},
            "MD5 = ? AND (EndTime - StartTime) > " + limit,
            new String[]{data.md5},
            null
        );

        long time = 0;

        if (c != null) {
            if (c.moveToFirst()) {
                time = c.getLong(c.getColumnIndex("StartTime"));
            }
            c.close();
        }

        return time;
    }

    public static HistoryDetail[] getDetailedHistory(Context ctx, Metadata data) {
        if (data.md5 == null) {
            return new HistoryDetail[0];
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String limit = prefs.getString("history_clean_limit", "20000");

        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "library_history"),
                new String[]{"StartTime", "(EndTime - StartTime) AS Time"},
                "MD5 = ? AND (EndTime - StartTime) > " + limit,
                new String[]{data.md5},
                null
        );

        HistoryDetail[] historyDetails =  new HistoryDetail[0];

        if (c != null) {
            if (c.moveToFirst()) {
                Hashtable<String, HistoryDetail> dates = new Hashtable<String, HistoryDetail>();

                do {
                    long startTime = c.getLong(c.getColumnIndex("StartTime"));
                    long readTime = c.getLong(c.getColumnIndex("Time"));
                    String date = IsdenTools.formatDate(startTime);
                    if (dates.containsKey(date)) {
                        HistoryDetail detail = dates.get(date);
                        detail.spent += readTime;
                    }
                    else {
                        HistoryDetail detail = new HistoryDetail(date, readTime);
                        dates.put(date, detail);
                    }
                } while (c.moveToNext());
                Collection<HistoryDetail> details = dates.values();
                dates = null;

                historyDetails = details.toArray(new HistoryDetail[details.size()]);
            }
            c.close();
        }

        return historyDetails;
    }
}
