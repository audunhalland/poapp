package no.regnskog.poapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class Product
{
    public static class Substance {
        long id;
        String name;
        String info;

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (!(obj instanceof Substance)) return false;

            Substance s2 = (Substance)obj;
            return name.equals(s2.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class Ingredient {
        public long id;
        public int min;
        public int max;
        public Substance substance;

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (!(obj instanceof Ingredient)) return false;

            Ingredient i2 = (Ingredient)obj;
            return min == i2.min && max == i2.max && substance == i2.substance;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + min;
            result = 31 * result + max;
            result = 31 * result + substance.hashCode();
            return result;
        }
    }

    public long id;
    public String ean;
    public String name;
    public Ingredient[] badIngredients;
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
            //p.min_po = c.getLong(1);
            //p.max_po = c.getLong(2);
            p.category = null;
            p.manufacturer = null;
            return p;
        } else {
            return null;
        }

    }

    public String toString()
    {
        return "product " + ean + " " + name;
    }
}
