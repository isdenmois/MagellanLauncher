package isden.mois.magellanlauncher.httpd.queries;

import android.database.Cursor;

import static isden.mois.magellanlauncher.Constants.SUCCESS_MESSAGE;

/**
 * Created by isden on 05.01.17.
 */

public class SetBookStatus extends SQLQuery {
    private int status;
    private String MD5;

    public SetBookStatus(int status, String MD5) {
        this.status = status;
        this.MD5 = MD5;
    }

    @Override
    protected String buildQuery() throws Exception {
        return "UPDATE library_metadata SET Status = \"" + status + "\" WHERE MD5 = \"" + MD5 + "\"";
    }

    @Override
    protected String processQuery(Cursor cursor) throws Exception {
        cursor.moveToFirst();
        return SUCCESS_MESSAGE;
    }
}
