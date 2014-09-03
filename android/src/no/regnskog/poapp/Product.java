package no.regnskog.poapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

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
        public long min;
        public long max;
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
            result = 31 * result + (int)min;
            result = 31 * result + (int)max;
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

    private static Ingredient[] getBadIngredientsFromProductId(SQLiteDatabase db, long productId)
    {
        final String q =
            "SELECT ingredient.id, ingredient.min, ingredient.max, " +
            "substance.id, substance.name, substance.info " +
            "FROM ingredient " +
            "JOIN substance ON ingredient.substance_id = substance.id " +
            "JOIN bad_ingredient ON bad_ingredient.ingredient_id = ingredient.id " +
            "WHERE bad_ingredient.product_id = " + productId;

        Cursor c = db.rawQuery(q, new String[]{});

        if (c.moveToFirst()) {
            ArrayList<Ingredient> bi = new ArrayList<Ingredient>();

            do {
                Ingredient ingr = new Ingredient();
                Substance subst = new Substance();
                ingr.id = c.getLong(0);
                ingr.min = c.getLong(1);
                ingr.max = c.getLong(2);
                subst.id = c.getLong(3);
                subst.name = c.getString(4);
                subst.info = c.getString(5);
                ingr.substance = subst;
                bi.add(ingr);
            } while (c.moveToNext());

            return bi.toArray(new Ingredient[bi.size()]);
        } else {
            return new Ingredient[0];
        }
    }

    public static Product getFromEAN(Context context, String ean)
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(context);
        SQLiteDatabase db = doh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name FROM product WHERE ean = ?", new String[]{ean});

        if (c.moveToFirst()) {
            Product p = new Product();
            p.ean = ean;
            p.id = c.getLong(0);
            p.name = c.getString(1);
            p.badIngredients = getBadIngredientsFromProductId(db, p.id);
            p.category = null;
            p.manufacturer = null;
            return p;
        } else {
            return null;
        }
    }

    public static long getProductCount(Context context)
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(context);
        SQLiteDatabase db = doh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT count(*) FROM product", null);

        if (c.moveToFirst()) {
            return c.getLong(0);
        } else {
            assert(false);
            return 0;
        }
    }

    public String toString()
    {
        return "product " + ean + " " + name;
    }
}
