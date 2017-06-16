package isden.mois.magellanlauncher.utils

import android.content.Context
import android.database.Cursor
import android.preference.PreferenceManager
import isden.mois.magellanlauncher.helpers.DBBooks
import isden.mois.magellanlauncher.models.BookMetadata
import isden.mois.magellanlauncher.models.BookTime
import isden.mois.magellanlauncher.models.HistoryDetail
import java.util.*

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

fun getDetailedHistory(ctx: Context, book: BookMetadata): Array<HistoryDetail> {
    var historyDetail = emptyArray<HistoryDetail>()
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    val limit = prefs.getString("history_clean_limit", "20000")


    val builder = StringBuilder("SELECT ")
    builder.append("StartTime, (EndTime - StartTime) AS Time ")
    builder.append("FROM library_history ")
    builder.append("WHERE Progress <> \"5/5\" AND MD5 = ? AND (EndTime - StartTime) > ")
    builder.append(limit)

    val query = builder.toString()
    val dbBooks = DBBooks(ctx)
    val db = dbBooks.readableDatabase

    try {
        val c = db.rawQuery(query, arrayOf(book.md5))
        if (c != null) {
            if (c.moveToFirst()) {
                val dates = Hashtable<String, HistoryDetail>();

                do {
                    val startTime = c.getLong(c.getColumnIndex("StartTime"));
                    val readTime = c.getLong(c.getColumnIndex("Time"));
                    val date = formatDate(startTime);

                    if (dates.containsKey(date)) {
                        val detail = dates[date];
                        detail!!.spent += readTime;
                    }
                    else {
                        val detail = HistoryDetail(date, startTime, readTime);
                        dates[date] = detail
                    }
                } while (c.moveToNext())
                historyDetail += dates.values
            }
        }
    } finally {
        db.close()
    }

    return historyDetail
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
