package isden.mois.magellanlauncher.providers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.onyx.android.data.OnyxCmsProvider;
import com.onyx.android.sdk.data.cms.OnyxThumbnail;
import com.onyx.android.sdk.device.EnvironmentUtil;

import java.io.File;

import isden.mois.magellanlauncher.helpers.DBBooks;

public class BooksProvider extends OnyxCmsProvider {
    public static final int DB_VERSION = 13;
    static final String AUTHORITY = "com.onyx.android.sdk.OnyxCmsProvider";
    static final String DOWNLOADED_PATH = "last_downloaded";
    static final String CURRENT_BOOK_PATH = "current_book";
    static final String BOOK_PATH = "book";

    static final int URI_DOWNLOADED = 1;
    static final int URI_CURRENT_BOOK = 2;
    static final int URI_BOOK = 3;
    static final int THUMBNAILS = 9;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DOWNLOADED_PATH, URI_DOWNLOADED);
        uriMatcher.addURI(AUTHORITY, CURRENT_BOOK_PATH, URI_CURRENT_BOOK);
        uriMatcher.addURI(AUTHORITY, BOOK_PATH, URI_BOOK);
        uriMatcher.addURI(AUTHORITY, OnyxThumbnail.DB_TABLE_NAME, THUMBNAILS);
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

            case URI_BOOK:
                return getBook(selectionArgs[0]);

        }
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String dst_table = null;
        String dst_null_column_hack = null;
        Uri dst_content_uri = null;

        final int match_result = uriMatcher.match(uri);
        if (match_result == THUMBNAILS) {
            String thumbnail_file = null;
            dst_table = OnyxThumbnail.DB_TABLE_NAME;
            dst_null_column_hack = OnyxThumbnail.Columns.SOURCE_MD5;
            dst_content_uri = OnyxThumbnail.CONTENT_URI;

            thumbnail_file = values.getAsString(OnyxThumbnail.Columns.SOURCE_MD5);
            String kind = values.getAsString(OnyxThumbnail.Columns.THUMBNAIL_KIND);
            thumbnail_file = getThumbnailFile(thumbnail_file, kind);
            Log.d("TAG", "creating thumbnail file: " + thumbnail_file);

            if (!new File(thumbnail_file).getParentFile().exists()) {
                Log.w("TAG", "Unable to create new file: " + thumbnail_file);
                return null;
            }

            values.put(OnyxThumbnail.Columns._DATA, thumbnail_file);
        } else {
            return super.insert(uri, values);
        }

        SQLiteDatabase db = mDefaultDBHelper.getWritableDatabase();
        long id = db.insert(dst_table, dst_null_column_hack, values);
        if (id < 0) {
            return null;
        }

        Uri ret = ContentUris.withAppendedId(dst_content_uri, id);
        this.getContext().getContentResolver().notifyChange(ret, null);

        return ret;
    }

    public static String getThumbnailFile(String sourceMD5, String thumbnailKind) {
        String thumbnail_folder = ".thumbnails";
        String preferred_extension = ".jpg";

        return EnvironmentUtil.getExternalStorageDirectory() + File.separator + thumbnail_folder + File.separator + sourceMD5 + "." + thumbnailKind + preferred_extension;
    }

    public static File getThumbnailFile(Context context, String sourceMD5) {
        String thumbnailKind = "Original";
        File f = new File(getThumbnailFile(sourceMD5, thumbnailKind));

        if (f.exists()) {
            return f;
        }

        return new File(getThumbnailFile(context, sourceMD5, thumbnailKind));
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

    private Cursor getBook(String MD5) {
        SQLiteDatabase db = mDefaultDBHelper.getReadableDatabase();
        return db.rawQuery(BOOK_QUERY, new String[]{MD5});
    }

    private final static String DOWNLOADED_QUERY = "SELECT Title, Authors, Location, _data AS Thumbnail " +
            "FROM library_metadata m " +
            "LEFT JOIN library_thumbnail t ON MD5 = Source_MD5 AND Thumbnail_Kind = \"Middle\" " +
            "WHERE (Status = 0 OR Status IS NULL) AND (Name LIKE \"%.fb2\" OR Name LIKE \"%.epub\" OR Name LIKE \"%.fb2.zip\")" +
            "GROUP BY Title, Authors " +
            "ORDER BY LastModified DESC";

    private final static String CURRENT_BOOK_QUERY = "SELECT MD5, Title, Authors, Location, Progress, _data AS Thumbnail " +
            "FROM library_metadata m " +
            "LEFT JOIN library_thumbnail t ON MD5 = Source_MD5 AND Thumbnail_Kind = \"Middle\" " +
            "ORDER BY LastAccess DESC " +
            "LIMIT 1";

    private final static String BOOK_QUERY = "SELECT MD5, Title, Authors, Location, Progress, _data AS Thumbnail " +
            "FROM library_metadata m " +
            "LEFT JOIN library_thumbnail t ON MD5 = Source_MD5 AND Thumbnail_Kind = \"Middle\" " +
            "WHERE MD5 = ?";
}
