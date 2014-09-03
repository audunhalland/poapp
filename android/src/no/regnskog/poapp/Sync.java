package no.regnskog.poapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.util.Log;
import android.os.Handler;
import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Sync
{
    private static final String TAG = "Sync";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 20000;

    URL mURL;
    HttpURLConnection mConnection;

    Context mContext;
    SQLiteDatabase mDatabase;

    SQLiteStatement mSubstanceStmt;
    SQLiteStatement mIngredientStmt;
    SQLiteStatement mProductStmt;
    SQLiteStatement mBadIngrStmt;

    Map<String, Product.Substance> mSubstances;
    Map<Product.Ingredient, Product.Ingredient> mIngredients;

    public Sync(Context c)
    {
        mContext = c;
        try {
            mURL = new URL("http://audunhalland.com/podb/po.php");
        } catch (Exception e) {
            assert(false);
        }
        mSubstances = new HashMap<String, Product.Substance>();
        mIngredients = new HashMap<Product.Ingredient, Product.Ingredient>();
    }

    private void saveSubstance(Product.Substance s)
    {
        mSubstanceStmt.bindString(1, s.name);
        mSubstanceStmt.bindString(2, s.info);
        s.id = mSubstanceStmt.executeInsert();
    }

    private void saveIngredient(Product.Ingredient i)
    {
        mIngredientStmt.bindLong(1, i.min);
        mIngredientStmt.bindLong(2, i.max);
        mIngredientStmt.bindLong(3, i.substance.id);
        i.id = mIngredientStmt.executeInsert();
    }

    private void saveProduct(Product p)
    {
        mProductStmt.bindString(1, p.ean);
        mProductStmt.bindString(2, p.name);
        p.id = mProductStmt.executeInsert();

        if (p.badIngredients != null) {
            for (int i = 0; i < p.badIngredients.length; ++i) {
                mBadIngrStmt.bindLong(1, p.id);
                mBadIngrStmt.bindLong(2, p.badIngredients[i].id);
                mBadIngrStmt.execute();
            }
        }
    }

    private Product.Substance getSubstance(String name)
    {
        Product.Substance s = mSubstances.get(name);
        if (s != null) {
            return s;
        } else {
            s = new Product.Substance();
            s.name = name;
            s.info = "";
            saveSubstance(s);
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
                i.min = reader.nextInt();
            } else if (property.equals("max")) {
                i.max = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();

        Product.Ingredient existing = mIngredients.get(i);
        if (existing != null) {
            return existing;
        } else {
            mIngredients.put(i, i);
            saveIngredient(i);
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
                ArrayList<Product.Ingredient> bi = new ArrayList<Product.Ingredient>();
                reader.beginArray();
                while (reader.hasNext()) {
                    bi.add(readIngredient(reader));
                }
                reader.endArray();
                p.badIngredients = bi.toArray(new Product.Ingredient[bi.size()]);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return p;
    }

    private void openDB()
    {
        DatabaseOpenHelper doh = new DatabaseOpenHelper(mContext);
        mDatabase = doh.getWritableDatabase();

        mSubstanceStmt = mDatabase.compileStatement
            ("INSERT INTO substance (name, info) VALUES (?, ?)");
        mIngredientStmt = mDatabase.compileStatement
            ("INSERT INTO ingredient (min, max, substance_id) VALUES (?, ?, ?)");
        mProductStmt = mDatabase.compileStatement
            ("INSERT INTO product (ean, name) VALUES (?, ?)");
        mBadIngrStmt = mDatabase.compileStatement
            ("INSERT INTO bad_ingredient (product_id, ingredient_id) VALUES (?, ?)");
    }

    /**
     *  Delete all rows from database
     */
    private void deleteAll()
    {
        mDatabase.delete("bad_ingredient", "", null);
        mDatabase.delete("ingredient", "", null);
        mDatabase.delete("substance", "", null);
        mDatabase.delete("product", "", null);
    }

    /**
     *  Sync top level json description document
     *  format is array of products
     */
    private boolean sync(JsonReader reader) throws IOException
    {
        boolean result = false;
        openDB();

        Log.d(TAG, "sql: begin transaction, network bound");
        mDatabase.beginTransaction();

        deleteAll();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                saveProduct(readProduct(reader));
            }
            reader.endArray();
            mDatabase.setTransactionSuccessful();
            result = true;
        } catch (SQLException e) {
            Log.e(TAG, "sql exception: " + e.toString());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "illegal argument (can't put into database): " + e.toString());
        } finally {
            mDatabase.endTransaction();
            Log.d(TAG, "sql: transaction ended");
        }

        return result;
    }

    /**
     *  Get the json input stream
     */
    protected InputStream getInputStream() throws IOException
    {
        mConnection = (HttpURLConnection)mURL.openConnection();
        mConnection.setConnectTimeout(CONNECT_TIMEOUT);
        mConnection.setReadTimeout(READ_TIMEOUT);
        return mConnection.getInputStream();
    }

    /**
     *  Perform sync synchronously
     */
    public boolean perform()
    {
        try {
            return sync(new JsonReader(new InputStreamReader(getInputStream())));
        } catch (IOException e) {
            Log.e(TAG, "IO exception: " + e.toString());
            return false;
        }
    }
};
