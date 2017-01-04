package isden.mois.magellanlauncher.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.onyx.android.data.OnyxCmsProvider;

import isden.mois.magellanlauncher.providers.BooksProvider;

public class DBBooks extends OnyxCmsProvider.DefaultDBHelper {
    public static final int STATUS_BOOK_IN_PLANS = 0;
    public static final int STATUS_BOOK_COMPLETED = 1;
    private int version;

    public DBBooks(Context context) {
        super(context, BooksProvider.DB_VERSION);
        this.version = BooksProvider.DB_VERSION;
    }

    public DBBooks(Context context, int version) {
        super(context, version);
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        onUpgrade(db, 12, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion > 12 ? 12 : newVersion);
        if (oldVersion < 12) {
            oldVersion = 12;
        }

        switch (newVersion){
            case 13:
                switch (oldVersion) {
                    case 12:
                        upgradeFrom12To13(db);
                }
        }
    }

    private void upgradeFrom12To13(SQLiteDatabase db) {
        addColumn(db, "library_metadata", "Status", "INTEGER");
        addColumn(db, "library_metadata", "Year", "INTEGER");
        db.execSQL("CREATE INDEX metadata_md5_idx ON library_metadata(MD5)");
        db.execSQL("CREATE INDEX history_md5_idx ON library_history(MD5)");
    }
}
