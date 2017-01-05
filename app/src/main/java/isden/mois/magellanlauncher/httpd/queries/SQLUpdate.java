package isden.mois.magellanlauncher.httpd.queries;

import android.database.Cursor;

import com.alibaba.fastjson.JSONObject;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import static isden.mois.magellanlauncher.Constants.SUCCESS_MESSAGE;

/**
 * Created by isden on 05.01.17.
 */

public class SQLUpdate extends SQLQuery {
    private JSONObject params;
    private String table;

    public SQLUpdate(String table, JSONObject params) {
        this.table = table;
        this.params = params;
    }

    @Override
    protected String buildQuery() throws Exception {
        List<String> fields = new LinkedList<>();
        String where = null;

        for (String field : params.keySet()) {
            if (field.equals("where")) {
                where = params.getString(field);
            }
            else {
                fields.add(field + "=" + params.getString(field));
            }
        }

        StringBuilder b = new StringBuilder("UPDATE ");
        b.append(table);
        b.append('\n');
        b.append("SET ");
        b.append(StringUtils.join(fields, ","));
        b.append('\n');

        if (where != null) {
            b.append("WHERE ");
            b.append(where);
        }
        else {
            throw new Exception("Field where is required");
        }

        return b.toString();
    }

    @Override
    protected String processQuery(Cursor cursor) throws Exception {
        cursor.moveToFirst();
        return SUCCESS_MESSAGE;
    }
}
