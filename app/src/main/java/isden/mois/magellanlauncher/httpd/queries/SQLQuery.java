package isden.mois.magellanlauncher.httpd.queries;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import isden.mois.magellanlauncher.helpers.DBBooks;

/**
 * Created by isden on 05.01.17.
 */

abstract public class SQLQuery {
    protected String[] params;

    public String execute(Context context) throws Exception {
        DBBooks dbBooks = new DBBooks(context);
        try {
            String query = buildQuery();
            if (query == null || query.equals("")) {
                return "[]";
            }

            SQLiteDatabase db = dbBooks.getReadableDatabase();

            try {
                Cursor c = db.rawQuery(query, params);

                if (c != null) {
                    try {
                        return processQuery(c);
                    } finally {
                        c.close();
                    }
                }
            } finally {
                db.close();
            }
        } finally {
            dbBooks.close();
        }

        return "[]";
    }

    protected String arrayResult(Cursor c) {
        JSONArray result = new JSONArray();
        String[] columns = c.getColumnNames();
        int columnCount = columns.length;

        while (c.moveToNext()) {
            JSONObject row = new JSONObject();
            for (int i = 0; i < columnCount; i++) {
                row.put(columns[i], c.getString(i));
            }

            result.add(row);
        }

        return result.toJSONString();
    }

    abstract protected String buildQuery() throws Exception;
    abstract protected String processQuery(Cursor cursor) throws Exception;
}
