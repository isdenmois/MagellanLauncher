package isden.mois.magellanlauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.util.Pair;

import isden.mois.magellanlauncher.helpers.DBBooks;
import isden.mois.magellanlauncher.holders.BookTime;
import isden.mois.magellanlauncher.holders.HistoryDetail;
import isden.mois.magellanlauncher.utils.DateKt;

import java.io.File;
import java.util.*;

public class Onyx {
    private static final String CONTENT_URI = "content://com.onyx.android.sdk.OnyxCmsProvider/";

    public static Metadata getCurrentBook(Context ctx) {
        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "current_book"),
                null,
                null,
                null,
                null
        );

        return parseBook(ctx, c);
    }

    public static Metadata getBook(Context ctx, String MD5) {
        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "book"),
                null,
                null,
                new String[]{MD5},
                null
        );

        return parseBook(ctx, c);
    }

    private static Metadata parseBook(Context ctx, Cursor c) {
        Metadata metadata = null;

        if (c != null) {
            if (c.moveToFirst()) {
                metadata = new Metadata();
                metadata.md5 = c.getString(c.getColumnIndex("MD5"));
                metadata.title = c.getString(c.getColumnIndex("Title"));
                metadata.author = c.getString(c.getColumnIndex("Authors"));
                metadata.thumbnail = c.getString(c.getColumnIndex("Thumbnail"));
                metadata.filePath = c.getString(c.getColumnIndex("Location"));

                String progress = c.getString(c.getColumnIndex("Progress"));
                if (progress != null && progress.contains("/")) {
                    String[] progressData = progress.split("/");
                    metadata.progress = Integer.parseInt(progressData[0]);
                    metadata.size = Integer.parseInt(progressData[1]);
                }
            }
            c.close();
        }

        if (metadata != null) {
            Pair<BookTime, Integer> details = getBookDetails(ctx, metadata.md5);
            metadata.time = details.first;
            metadata.readPages = details.second;
        }

        return metadata;
    }

    public static Pair<BookTime, Integer> getBookDetails(Context ctx, String MD5) {
        ArrayList<int[]> speeds = new ArrayList<>();
        int lastProgress = 0;
        BookTime bookTime = new BookTime();
        bookTime.totalTime = 0;
        bookTime.currentTime = 0;
        String[] progressData = null;

        Cursor c = getHistoryCursor(ctx, MD5);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    long time = c.getLong(c.getColumnIndex("Time"));
                    String progress = c.getString(c.getColumnIndex("Progress"));
                    if (progress.contains("/")) {
                        progressData = progress.split("/");
                        int currentProgress = Integer.parseInt(progressData[0]);

                        // Если начал читать заново.
                        if (currentProgress < lastProgress) {
                            int totalProgress = Integer.parseInt(progressData[1]);
                            int currentPercent = 100 * currentProgress / totalProgress;
                            int previousPercent = 100 * lastProgress / totalProgress;
                            int diffPercent = previousPercent - currentPercent;

                            if (currentPercent < 20 && diffPercent > 20) {
                                bookTime.currentTime = 0;
                                speeds = new ArrayList<>();
                                lastProgress = 0;
                            }
                        }

                        bookTime.totalTime += time;
                        bookTime.currentTime += time;
                        int speed;

                        if (currentProgress != lastProgress) {
                            speed = (int) (time / (currentProgress - lastProgress));
                        } else {
                            speed = 0;
                        }

                        // Если были пропущены главы.
                        if (currentProgress > lastProgress && speed > 10000 && speed < 100000) {
                            speeds.add(new int[]{speed, currentProgress - lastProgress});
                        }

                        lastProgress = currentProgress;
                    }
                }
            } finally {
                c.close();
            }
        }

        long bookWeight = 0;
        double bookPages = 0;
        for (int[] speedTime : speeds) {
            bookWeight += (long) speedTime[0] * speedTime[1];
            bookPages += speedTime[1];
        }

        bookTime.speed = bookWeight / bookPages;

        return new Pair<>(bookTime, (int) Math.round(bookPages));
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
            if (c.moveToFirst()) {
                long totalTime = c.getLong(0);
                return DateKt.formatHumanTime(totalTime);
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

    public static List<Metadata> getLastDownloaded(Context ctx, int limit) {
        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "last_downloaded"),
                null,
                null,
                null,
                String.valueOf(limit)
        );

        List<Metadata> metadataList = new ArrayList<>();

        if (c != null) {
            try {
                while (c.moveToNext()) {
                    Metadata metadata = new Metadata();
                    metadata.author = c.getString(c.getColumnIndex("Authors"));
                    metadata.title = c.getString(c.getColumnIndex("Title"));
                    metadata.filePath = c.getString(c.getColumnIndex("Location"));
                    metadata.thumbnail = c.getString(c.getColumnIndex("Thumbnail"));

                    metadataList.add(metadata);
                }
            } finally {
                c.close();
            }
        }

        return metadataList;
    }

    public static Cursor getHistoryCursor(Context ctx, String MD5) {
        if (MD5 == null) {
            return null;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String limit = prefs.getString("history_clean_limit", "20000");

        return ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "library_history"),
                new String[]{"StartTime", "(EndTime - StartTime) AS Time", "Progress"},
                "Progress <> \"5/5\" AND MD5 = ? AND (EndTime - StartTime) > " + limit,
                new String[]{MD5},
                "StartTime"
        );
    }

}
