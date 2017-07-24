package isden.mois.magellanlauncher.tasks.sync

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import isden.mois.magellanlauncher.helpers.DBBooks
import isden.mois.magellanlauncher.tasks.SyncTask

/**
 * Created by isden on 24.07.17.
 */
class UploadBookMetadata(val MD5: String): SyncTask {

    override fun execute(ctx: Context) {
        val dbBooks = DBBooks(ctx)
        try {
            val db = dbBooks.readableDatabase

            try {
                insertBook(db)
            } finally {
                db.close()
            }
        }
        finally {
            dbBooks.close()
        }
    }

    fun insertBook(db: SQLiteDatabase) {
        val query = "SELECT Title, Authors, Status, Size, LastModified, Progress FROM library_metadata WHERE MD5 = ?"

        val c = db.rawQuery(query, arrayOf(MD5))
        if (c != null) {
            try {
                c.moveToFirst()
                val body = JSONObject()
                body.put("md5", MD5)
                body.put("title", c.getString(c.getColumnIndex("Title")))
                body.put("author", c.getString(c.getColumnIndex("Authors")))
                body.put("status", c.getInt(c.getColumnIndex("Status")))
                body.put("size", c.getInt(c.getColumnIndex("Size")))
                body.put("created", c.getLong(c.getColumnIndex("LastModified")))
                body.put("progress", c.getString(c.getColumnIndex("Progress")))

                val (request, response, result) = Fuel
                        .post("http://10.0.0.50:5000/books/new")
                        .header("Content-Type" to "application/json")
                        .body(body.toJSONString())
                        .response()
            } finally {
                c.close()
            }
        }
    }
}

fun createUploadBookMetadata(ctx: Context): List<SyncTask> {
    val dbBooks = DBBooks(ctx)
    try {
        val db = dbBooks.readableDatabase

        try {
            val response = Fuel
                    .post("http://10.0.0.50:5000/books/diff")
                    .header("Content-Type" to "application/json")
                    .body(md5List(db).toJSONString())
                    .responseJson()

            val md5List = response.third.get().array()
            val result:MutableList<SyncTask> = mutableListOf()

            repeat(md5List.length()) { i ->
                result.add(UploadBookMetadata(md5List.getString(i)))
            }

            return result
        } finally {
            db.close()
        }
    }
    finally {
        dbBooks.close()
    }
}

fun md5List(db: SQLiteDatabase): JSONArray {
    val result = JSONArray()
    val query = """SELECT DISTINCT MD5
    FROM library_metadata
    WHERE name LIKE '%.fb2' AND Title IS NOT NULL AND Authors IS NOT NULL"""

    val c = db.rawQuery(query, arrayOf<String>())

    if (c != null) {
        try {
            while (c.moveToNext()) {
                result.add(c.getString(0))
            }

            return result
        } finally {
            c.close()
        }
    }

    return result
}
