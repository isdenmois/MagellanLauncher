package isden.mois.magellanlauncher.httpd.queries;

import android.database.Cursor;

import com.onyx.android.sdk.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by isden on 05.01.17.
 */

public class SQLGet extends SQLQuery {
    public static final String TAG = "SQLGet";

    private Map<String, List<String>> parms;

    public SQLGet(Map<String, List<String>> parms) {
        this.parms = parms;
    }

    @Override
    protected String buildQuery() throws Exception {
        StringBuilder builder = createQueryBuilder();
        appendFrom(builder);
        appendJoins(builder);
        appendWhere(builder);
        appendOrder(builder);
        appendLimit(builder);

        String result = builder.toString();
        return result;
    }

    private StringBuilder createQueryBuilder() {
        StringBuilder builder = new StringBuilder("SELECT ");

        if (parms.containsKey("fields[]")) {
            List<String> fields = parms.get("fields[]");
            builder.append(StringUtils.join(fields, ","));
            builder.append("\n");
        }
        else {
            builder.append("*\n");
        }

        return builder;
    }

    private void appendFrom(StringBuilder builder) throws Exception {
        if (parms.containsKey("table")) {
            String table = parms.get("table").get(0);

            builder.append("FROM ");
            builder.append(table);
            builder.append(" AS tbl\n");
        }
        else {
            throw new Exception("Table field is required");
        }
    }

    private void appendJoins(StringBuilder builder) {
        if (parms.containsKey("joins[]")) {
            List<String> joins = parms.get("joins[]");
            for (String join : joins) {
                String[] joinParams = join.split("|");

                builder.append("LEFT JOIN ");
                builder.append(joinParams[0]);
                builder.append(" AS ");
                builder.append(joinParams[1]);
                builder.append(" ON ");
                builder.append(joinParams[2]);
                builder.append("\n");
            }
        }
    }

    private void appendWhere(StringBuilder builder) {
        if (parms.containsKey("where")) {
            String where = parms.get("where").get(0);

            builder.append("WHERE ");
            builder.append(where);
            builder.append("\n");
        }
    }

    private void appendOrder(StringBuilder builder) {
        if (parms.containsKey("order")) {
            String order = parms.get("order").get(0);

            builder.append("ORDER BY ");
            builder.append(order);
            builder.append("\n");
        }
    }

    private void appendLimit(StringBuilder builder) {
        int limit = 20;
        int offset= 0;

        if (parms.containsKey("limit")) {
            String limitString = parms.get("limit").get(0);
            limit = Integer.valueOf(limitString);
        }

        if (parms.containsKey("offset")) {
            String offsetString = parms.get("offset").get(0);
            offset = Integer.valueOf(offsetString);
        }

        builder.append("LIMIT ");
        builder.append(offset);
        builder.append(", ");
        builder.append(limit);
        builder.append("\n");
    }

    @Override
    protected String processQuery(Cursor cursor) {
        return arrayResult(cursor);
    }
}
