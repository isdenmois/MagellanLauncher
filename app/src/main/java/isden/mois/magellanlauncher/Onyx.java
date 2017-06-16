package isden.mois.magellanlauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;

import isden.mois.magellanlauncher.helpers.DBBooks;
import isden.mois.magellanlauncher.holders.BookTime;
import isden.mois.magellanlauncher.holders.HistoryDetail;

import java.io.File;
import java.util.*;

public class Onyx {
    private static final String CONTENT_URI = "content://com.onyx.android.sdk.OnyxCmsProvider/";

    public static Metadata getCurrentBook(Context ctx) {
        Metadata metadata = null;
        Cursor c = ctx.getContentResolver().query(
                Uri.parse(CONTENT_URI + "current_book"),
                null,
                null,
                null,
                null
        );

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
            metadata.time = getBookDetails(ctx, metadata.md5);
        }

        return metadata;
    }

    public static BookTime getBookDetails (Context ctx, String MD5) {
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
                        }
                        else {
                            speed = 0;
                        }

                        // Если были пропущены главы.
                        if (currentProgress > lastProgress && speed > 24000 && speed < 100000) {
                            speeds.add(new int[]{ speed,  currentProgress - lastProgress});
                        }

                        lastProgress = currentProgress;
                    }
                }
            }
            finally {
                c.close();
            }
        }

        long bookWeight = 0;
        double bookPages = 0;
        for (int[] speedTime : speeds) {
            bookWeight += (long) speedTime[0] * speedTime[1];
            bookPages += speedTime[1];
        }

        // Для новых книг добавляем среднее значение.
        if (progressData != null) {
            int totalProgress = Integer.parseInt(progressData[1]);
            if (lastProgress * 100 / totalProgress < 20) {
                int additionalPageCount = totalProgress / 15;
                bookPages += additionalPageCount;
                bookWeight += additionalPageCount * 40000;
            }
        }

        bookTime.speed = bookWeight / bookPages;

        if (bookTime.speed < 24000) {
            bookTime.speed = 35000;
        }

        return bookTime;
    }

    public static List<Metadata> getRecentReading(Context ctx, int limit) {
        List<Metadata> result = new LinkedList<Metadata>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String history_clean_limit = prefs.getString("history_clean_limit", "20000");

        StringBuilder builder = new StringBuilder("SELECT ");
        builder.append("m.MD5, Authors, Title, Name, NativeAbsolutePath, m.Progress, ");
        builder.append("MAX(LastAccess) AS LastAccess, SUM(h.EndTime - h.StartTime) as TotalTime, t._data as Thumbnail ");
        builder.append("FROM library_metadata m ");
        builder.append("LEFT JOIN library_history h ON h.MD5 = m.MD5 ");
        builder.append("LEFT JOIN library_thumbnail t ON Source_MD5 = m.MD5 AND Thumbnail_Kind = \"Middle\" ");
        builder.append("WHERE Title IS NOT NULL AND Name LIKE \"%.fb2%\" ");
        builder.append("GROUP BY m.MD5 ");
        builder.append("ORDER BY LastAccess DESC");
        if (limit > 0) {
            builder.append("LIMIT ");
            builder.append(limit);
        }

        String query = builder.toString();
        DBBooks dbBooks = new DBBooks(ctx);
        SQLiteDatabase db = dbBooks.getReadableDatabase();

        try {
            Cursor c = db.rawQuery(query, new String[]{});
            if (c != null && c.moveToFirst()) {
                do {
                    result.add(createMetadata(c));
                } while (c.moveToNext());
                c.close();
            }
        } finally {
            db.close();
        }

        return result;
    }

    private static Metadata createMetadata(Cursor c) {
        Metadata metadata = new Metadata();
        metadata.md5 = c.getString(c.getColumnIndex("MD5"));
        metadata.filename = c.getString(c.getColumnIndex("Name"));
        metadata.title = c.getString(c.getColumnIndex("Title"));
        metadata.author = c.getString(c.getColumnIndex("Authors"));
        metadata.filePath = c.getString(c.getColumnIndex("NativeAbsolutePath"));

        String progressString = c.getString(c.getColumnIndex("Progress"));

        String[] progress;
        if (progressString != null) {
            progress = progressString.split("/");
        }
        else {
            progress = new String[0];
        }

        if (progress.length == 2) {
            metadata.progress = Integer.parseInt(progress[0]);
            metadata.size = Integer.parseInt(progress[1]);
        }

        metadata.lastAccess = c.getLong(c.getColumnIndex("LastAccess"));
        metadata.thumbnail = c.getString(c.getColumnIndex("Thumbnail"));
        metadata.time.currentTime = c.getInt(c.getColumnIndex("TotalTime"));

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
            }
            finally {
                c.close();
            }
        }

        return metadataList;
    }

    public static Cursor getHistoryCursor(Context ctx, String MD5) {
        if (MD5 == null) {
            return  null;
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

    public static HistoryDetail[] getDetailedHistory(Context ctx, Metadata data) {
        HistoryDetail[] historyDetails =  new HistoryDetail[0];

        Cursor c = getHistoryCursor(ctx, data.md5);
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
                        HistoryDetail detail = new HistoryDetail(date, startTime, readTime);
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
