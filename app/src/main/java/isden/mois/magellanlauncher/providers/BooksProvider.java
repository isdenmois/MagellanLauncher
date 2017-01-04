package isden.mois.magellanlauncher.providers;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.onyx.android.data.OnyxCmsProvider;

import isden.mois.magellanlauncher.helpers.DBBooks;

public class BooksProvider extends OnyxCmsProvider {
    public static final int DB_VERSION = 13;
    static final String AUTHORITY = "com.onyx.android.sdk.OnyxCmsProvider";
    static final String DOWNLOADED_PATH = "last_downloaded";
    static final String CURRENT_BOOK_PATH = "current_book";

    static final int URI_DOWNLOADED = 1;
    static final int URI_CURRENT_BOOK = 2;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DOWNLOADED_PATH, URI_DOWNLOADED);
        uriMatcher.addURI(AUTHORITY, CURRENT_BOOK_PATH, URI_CURRENT_BOOK);
    }

    @Override
    public boolean onCreate() {
        this.mDefaultDBHelper = new DBBooks(getContext(), DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("QUERY", "query, " + uri.toString() + ", sort: " + sortOrder);
        switch (uriMatcher.match(uri)) {
            case URI_DOWNLOADED:
                return getDownloadedBooks(sortOrder);

            case URI_CURRENT_BOOK:
                return getCurrentBook();

        }
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getDownloadedBooks (String limit) {
        SQLiteDatabase db = mDefaultDBHelper.getReadableDatabase();
        String query = DOWNLOADED_QUERY;
        String[] params;

        if (limit != null) {
            query += " LIMIT ?";
            params = new String[]{limit};
        }
        else {
            params = new String[]{};
        }

        return db.rawQuery(query, params);
    }

    private Cursor getCurrentBook () {
        SQLiteDatabase db = mDefaultDBHelper.getReadableDatabase();
        return db.rawQuery(CURRENT_BOOK_QUERY, new String[] {});
    }

    private final static String DOWNLOADED_QUERY = "SELECT Title, Authors, Location, _data AS Thumbnail " +
            "FROM library_metadata m " +
            "LEFT JOIN library_thumbnail t ON MD5 = Source_MD5 AND Thumbnail_Kind = \"Middle\" " +
            "WHERE Status IS NULL OR Status = 0 " +
            "GROUP BY Title, Authors " +
            "ORDER BY LastModified DESC";

    private final static String CURRENT_BOOK_QUERY = "SELECT MD5, Title, Authors, Location, Progress, _data AS Thumbnail " +
            "FROM library_metadata m " +
            "LEFT JOIN library_thumbnail t ON MD5 = Source_MD5 AND Thumbnail_Kind = \"Middle\" " +
            "ORDER BY LastAccess DESC " +
            "LIMIT 1";
}
