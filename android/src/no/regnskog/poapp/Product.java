package no.regnskog.poapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class Product
{
    public String ean;
    public String name;
    public long min_po;
    public long max_po;
    public String category;
    public String manufacturer;

    public static Product getFromEAN(Context context, String ean)
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(context);
        SQLiteDatabase db = doh.getWritableDatabase();
        String q =
            "SELECT name, po_percent_min, po_percent_max FROM product " +
            "WHERE ean = ?";
        Cursor c = db.rawQuery(q, new String[]{ean});

        if (c.moveToFirst()) {
            Product p = new Product();
            p.ean = ean;
            p.name = c.getString(0);
            p.min_po = c.getLong(1);
            p.max_po = c.getLong(2);
            p.category = null;
            p.manufacturer = null;
            return p;
        } else {
            return null;
        }

    }
}
