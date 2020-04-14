package isden.mois.magellanlauncher.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.preference.PreferenceManager
import android.util.Log
import isden.mois.magellanlauncher.helpers.DBBooks
import isden.mois.magellanlauncher.models.BookMetadata
import isden.mois.magellanlauncher.models.BookTime
import isden.mois.magellanlauncher.models.HistoryDetail
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by isden on 16.06.17.
 */

fun getRecentReading(ctx: Context, limit: Int = 0): List<BookMetadata> {
    val result = LinkedList<BookMetadata>()

    val builder = StringBuilder("SELECT ")
    builder.append("m.MD5, Authors, Title, Name, NativeAbsolutePath, m.Progress, ")
    builder.append("MAX(LastAccess) AS LastAccess, SUM(h.EndTime - h.StartTime) AS TotalTime,")
    builder.append("t._data as Thumbnail, MIN(h.StartTime) AS FirstTime ")
    builder.append("FROM library_metadata m ")
    builder.append("LEFT JOIN library_history h ON h.MD5 = m.MD5 ")
    builder.append("LEFT JOIN library_thumbnail t ON Source_MD5 = m.MD5 AND Thumbnail_Kind = \"Middle\" ")
    builder.append("WHERE Title IS NOT NULL AND Name LIKE \"%.fb2%\" ")
    builder.append("GROUP BY m.MD5 ")
    builder.append("ORDER BY LastAccess DESC")
    if (limit > 0) {
        builder.append("LIMIT ")
        builder.append(limit)
    }

    val query = builder.toString()
    val dbBooks = DBBooks(ctx)
    val db = dbBooks.readableDatabase

    try {
        val c = db.rawQuery(query, emptyArray<String>())
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    result.add(createMetadata(c))
                } while (c.moveToNext())
            }
            c.close()
        }
    } finally {
        db.close()
    }

    return result
}

fun getBooks(ctx: Context, status: String, order: String): List<BookMetadata> {
    val result = ArrayList<BookMetadata>()
    val query = "SELECT m.MD5, Authors, Title, Name, NativeAbsolutePath, m.Progress, " +
            "MAX(LastAccess) AS LastAccess, SUM(h.EndTime - h.StartTime) AS TotalTime, " +
            "t._data as Thumbnail, MIN(h.StartTime) AS FirstTime " +
            "FROM library_metadata m " +
            "LEFT JOIN library_history h ON h.MD5 = m.MD5 " +
            "LEFT JOIN library_thumbnail t ON Source_MD5 = m.MD5 AND Thumbnail_Kind = \"Middle\" " +
            "WHERE Title IS NOT NULL AND (Name LIKE \"%.fb2\" OR Name LIKE \"%.epub\") " +
            "AND ${status} " +
            "GROUP BY m.MD5 " +
            "ORDER BY ${order} DESC"
    val dbBooks = DBBooks(ctx)
    val db = dbBooks.readableDatabase

    try {
        val c = db.rawQuery(query, emptyArray<String>())
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    result.add(createMetadata(c))
                } while (c.moveToNext())
            }
            c.close()
        }
    } finally {
        db.close()
    }

    return result
}

fun getDetailedHistory(ctx: Context, md5: String): Array<HistoryDetail> {
    var historyDetails = emptyArray<HistoryDetail>()
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    val limit = prefs.getString("history_clean_limit", "20000").toInt()

    val builder = StringBuilder("SELECT ")
    builder.append("StartTime, (EndTime - StartTime) AS Time, Progress ")
    builder.append("FROM library_history ")
    builder.append("WHERE Progress <> \"5/5\" AND MD5 = ? ")
    builder.append("ORDER BY EndTime")

    val query = builder.toString()
    val dbBooks = DBBooks(ctx)
    val db = dbBooks.readableDatabase

    try {
        val c = db.rawQuery(query, arrayOf(md5))
        if (c != null) {
            if (c.moveToFirst()) {
                val dates = Hashtable<String, HistoryDetail>();
                var lastProgress = 0;

                do {
                    val startTime = c.getLong(c.getColumnIndex("StartTime"));
                    val readTime = c.getLong(c.getColumnIndex("Time"));
                    val progressStr = c.getString(c.getColumnIndex("Progress"))
                    val date = formatDate(startTime);
                    val progress = Integer.parseInt(progressStr.substring(0, progressStr.indexOf('/')))
                    val pages = progress - lastProgress
                    val speed = if (pages > 0) readTime / pages else 0

                    Log.d("HISTORY", "readTime: " + readTime + "; lastProgress: " + lastProgress + "; progress: " + progress + "; pages: " + pages + "; speed: " + speed)

                    if (readTime > limit && progress > lastProgress && speed > 15000 && speed < 100000) {
                        if (dates.containsKey(date)) {
                            val detail = dates[date];
                            detail!!.progress = progress;
                            detail.spent += readTime;
                            detail.pages += pages
                        } else {
                            val detail = HistoryDetail(date, startTime, readTime, progress, pages, 0);
                            dates[date] = detail
                        }
                    }

                    lastProgress = progress
                } while (c.moveToNext())
                historyDetails += dates.values
            }
        }
    } finally {
        db.close()
    }

    for (detail in historyDetails) {
        detail.speed = if (detail.pages > 0) Math.round((detail.spent / detail.pages).toDouble()).toInt() else 0
    }

    Arrays.sort(historyDetails, Comparator<HistoryDetail> { h1, h2 ->
        if (h1 == null && h2 == null) {
            return@Comparator 0
        }
        if (h1 == null) {
            return@Comparator 1
        }
        if (h2 == null) {
            return@Comparator -1
        }

        val t1 = h1.timestamp
        val t2 = h2.timestamp
        if (t1 < t2) -1 else if (t1 == t2) 0 else 1
    })

    return historyDetails
}

fun createMetadata(c: Cursor): BookMetadata {
    val md5 = c.getString(c.getColumnIndex("MD5"))
    val filename = c.getString(c.getColumnIndex("Name"))
    val title = c.getString(c.getColumnIndex("Title"))
    val author = c.getString(c.getColumnIndex("Authors"))
    val filePath = c.getString(c.getColumnIndex("NativeAbsolutePath"))
    val progress = c.getString(c.getColumnIndex("Progress"))
    val lastAccess = c.getLong(c.getColumnIndex("LastAccess"))
    val thumbnail = c.getString(c.getColumnIndex("Thumbnail"))
    val currentTime = c.getLong(c.getColumnIndex("TotalTime"))
    val firstTime = c.getLong(c.getColumnIndex("FirstTime"))

    val time = BookTime()
    time.currentTime = currentTime
    time.totalTime = time.totalTime

    return BookMetadata(md5, author, title, filename, filePath, lastAccess, time, thumbnail, progress, firstTime)
}

fun changeStatus(ctx: Context, book: BookMetadata, status: Int) {
    val dbBooks = DBBooks(ctx)
    val db = dbBooks.readableDatabase

    try {
        val values = ContentValues()
        values.put("Status", status)

        db.update("library_metadata", values, "MD5 = ?", arrayOf(book.md5))
    } finally {
        db.close()
    }
}
