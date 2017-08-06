package isden.mois.magellanlauncher.tasks.sync

import android.content.Context
import com.alibaba.fastjson.JSONObject
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import isden.mois.magellanlauncher.helpers.DBBooks
import isden.mois.magellanlauncher.tasks.SyncTask

/**
 * Created by isden on 06.08.17.
 */

class UploadHistory(
    val MD5: String,
    val startTime: Long,
    val endTime: Long,
    val progress: String
): SyncTask {

    override fun execute(ctx: Context) {
        val body = JSONObject()
        body.put("md5", MD5)
        body.put("startTime", startTime)
        body.put("endTime", endTime)
        body.put("progress", this.progress)

        val (request, response, result) = Fuel
                .post("http://192.168.58.1:5000/api/history/new")
                .header("Content-Type" to "application/json")
                .body(body.toJSONString())
                .response()
    }
}

fun createUploadHistory(ctx: Context): List<SyncTask> {
    val response = Fuel
            .post("http://192.168.58.1:5000/api/history/diff")
            .header("Content-Type" to "application/json")
            .responseJson()
    val datetime = response.third.get().obj().getString("datetime")

    val dbBooks = DBBooks(ctx)
    try {
        val db = dbBooks.readableDatabase

        try {
            val result:MutableList<SyncTask> = mutableListOf()
            val query = """SELECT DISTINCT MD5, StartTime, EndTime, Progress
                            FROM library_history
                            WHERE StartTime > ? AND Progress <> '5/5'
                            ORDER BY StartTime"""

            val c = db.rawQuery(query, arrayOf<String>(datetime))

            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        result.add(UploadHistory(
                            c.getString(c.getColumnIndex("MD5")),
                            c.getLong(c.getColumnIndex("StartTime")),
                            c.getLong(c.getColumnIndex("EndTime")),
                            c.getString(c.getColumnIndex("Progress"))
                        ))
                    }

                    return result
                } finally {
                    c.close()
                }
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
