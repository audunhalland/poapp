package no.regnskog.poapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

class DatabaseTest
{
    Context mContext;

    DatabaseTest(Context context)
    {
        mContext = context;
    }

    /*
    SQLiteStatement getInsertProductStatement()
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(mContext);
        SQLiteDatabase db = doh.getWritableDatabase();
        String q = "INSERT INTO product " +
            "(ean, name, po_percent_min, po_percent_max) VALUES (?, ?, ?, ?)";
        return db.compileStatement(q);
    }

    void insertProduct(SQLiteStatement stmt, String ean, String name, int pomin, int pomax)
    {
        stmt.bindString(1, ean);
        stmt.bindString(2, name);
        stmt.bindLong(3, pomin);
        stmt.bindLong(4, pomax);
        stmt.execute();
    }

    public void insertTestProducts()
    {
        SQLiteStatement s = getInsertProductStatement();
        insertProduct(s, "7311041019993", "Batterier", 40, 50);
    }
    */
}
