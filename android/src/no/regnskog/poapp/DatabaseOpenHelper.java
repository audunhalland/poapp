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
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   // BUG: multiple ean per product?
                   "ean TEXT NOT NULL, " +
                   "name TEXT NOT NULL, " +
                   "category_id INTEGER, " +
                   "manufacturer_id INTEGER " +
                   ")");
        db.execSQL("CREATE TABLE ingredient (" +
                   "id INTEGER PRIMARY KEY, " +
                   "min INTEGER NOT NULL, " +
                   "max INTEGER NOT NULL, " +
                   "substance_id INTEGER NOT NULL," +
                   "FOREIGN KEY (substance_id) REFERENCES substance(id)" +
                   ")");
        db.execSQL("CREATE TABLE substance (" +
                   "id INTEGER PRIMARY KEY, " +
                   "name TEXT, " +
                   "info TEXT " +
                   ")");
        db.execSQL("CREATE TABLE bad_ingredient (" +
                   "product_id INTEGER NOT NULL, " +
                   "ingredient_id INTEGER NOT NULL, " +
                   "PRIMARY KEY (product_id, ingredient_id), " +
                   "FOREIGN KEY (product_id) REFERENCES product(id), " +
                   "FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)" +
                   ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
