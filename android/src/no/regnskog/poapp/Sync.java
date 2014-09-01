package no.regnskog.poapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;
import android.util.Log;
import com.google.gson.stream.JsonReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

class Sync {
    private static final String TAG = "Sync";

    Context mContext;
    SQLiteDatabase mDatabase;

    Map<String, Product.Substance> mSubstances;
    Map<Product.Ingredient, Product.Ingredient> mIngredients;

    public Sync(Context c)
    {
        mContext = c;
        mSubstances = new HashMap<String, Product.Substance>();
        mIngredients = new HashMap<Product.Ingredient, Product.Ingredient>();
    }

    private Product.Substance getSubstance(String name)
    {
        Product.Substance s = mSubstances.get(name);
        if (s != null) {
            return s;
        } else {
            s = new Product.Substance();
            s.name = name;
            mSubstances.put(name, s);
            return s;
        }
    }

    private Product.Ingredient readIngredient(JsonReader reader) throws IOException
    {
        reader.beginObject();

        Product.Ingredient i = new Product.Ingredient();

        while (reader.hasNext()) {
            String property = reader.nextName();

            if (property.equals("subst")) {
                i.substance = getSubstance(reader.nextString());
            } else if (property.equals("min")) {
                i.percentMin = reader.nextInt();
            } else if (property.equals("max")) {
                i.percentMax = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();

        Product.Ingredient existing = mIngredients.get(i);
        if (existing != null) {
            return existing;
        } else {
            Log.d(TAG, "new ingredient: " + i.substance.name);
            mIngredients.put(i, i);
            return i;
        }
    }

    private Product readProduct(JsonReader reader) throws IOException
    {
        Product p = new Product();
        reader.beginObject();

        while (reader.hasNext()) {
            String property = reader.nextName();

            if (property.equals("ean")) {
                p.ean = reader.nextString();
            } else if (property.equals("name")) {
                p.name = reader.nextString();
            } else if (property.equals("bi")) {
                reader.beginArray();
                while (reader.hasNext()) {
                    Product.Ingredient i = readIngredient(reader);
                }
                reader.endArray();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return p;
    }

    private void sync(JsonReader reader) throws IOException
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(mContext);
        mDatabase = doh.getWritableDatabase();

        mDatabase.beginTransaction();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                Product p = readProduct(reader);
            }
            reader.endArray();
        } catch (SQLException e) {
            Log.e(TAG, "sql exception: " + e.toString());
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void perform()
    {
        if (mContext.deleteDatabase("db")) {
            Log.d(TAG, "database deleted");
        }

        try {
            URL url = new URL("http://audunhalland.com/podb/po.php");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            sync(new JsonReader(new InputStreamReader(conn.getInputStream())));
        } catch (IOException e) {
            Log.e(TAG, "IO exception: " + e.toString());
        }
    }

    public void performAsync()
    {
    }
};
