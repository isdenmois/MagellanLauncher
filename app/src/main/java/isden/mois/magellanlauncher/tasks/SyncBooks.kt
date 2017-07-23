package isden.mois.magellanlauncher.tasks

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.github.kittinunf.fuel.Fuel
import isden.mois.magellanlauncher.helpers.DBBooks
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.android.extension.responseJson

/**
 * Created by isden on 18.07.17.
 */
fun syncBooks(ctx: Context): Int {
    val dbBooks = DBBooks(ctx)
    try {
        val db = dbBooks.readableDatabase

        try {
            val response = Fuel
                    .post("http://10.0.0.50:5000/books/diff")
                    .header("Content-Type" to "application/json")
                    .body(md5List(db).toJSONString())
                    .responseJson()

            val result = response.third.get().array()
            repeat(result.length()) { i ->
                insertBook(db, result.getString(i))
            }

            return result.length()
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

fun insertBook(db: SQLiteDatabase, md5: String) {
    val query = "SELECT Title, Authors, Status, Size, LastModified, Progress FROM library_metadata WHERE MD5 = ?"

    val c = db.rawQuery(query, arrayOf(md5))
    if (c != null) {
        try {
            c.moveToFirst()
            val body = JSONObject()
            body.put("md5", md5)
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
