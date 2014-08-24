package no.regnskog.poapp;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

class DatabaseOpenHelper extends SQLiteOpenHelper
{
    private static final int DB_VERSION = 1;

    public DatabaseOpenHelper(Context ctx)
    {
        super(ctx, "db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE product (" +
                   "ean TEXT PRIMARY KEY, " +
                   "name TEXT NOT NULL, " +
                   "po_percent_min INTEGER NOT NULL, " +
                   "po_percent_max INTEGER NOT NULL, " +
                   "category_id INTEGER, " +
                   "manufacturer_id INTEGER " +
                   ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
