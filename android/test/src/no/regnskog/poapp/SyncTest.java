package no.regnskog.poapp;

import android.test.AndroidTestCase;
import com.google.gson.stream.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import junit.framework.Test;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncTest extends AndroidTestCase
{
    private boolean syncJSON(final String json)
    {
        Sync sync = new Sync(getContext()) {
            @Override
            protected InputStream getInputStream()
            {
                return new ByteArrayInputStream(json.getBytes());
            }
        };
        return sync.perform();
    }

    private void syncJSONError(String json)
    {
        assertFalse(syncJSON(json));
    }

    private JSONArray oneSizedArray(JSONObject obj)
    {
        JSONArray array = new JSONArray();
        array.put(obj);
        return array;
    }

    private Product syncOneValidProduct(JSONObject obj, String ean)
    {
        syncJSON(oneSizedArray(obj).toString());

        assertEquals(Product.getProductCount(getContext()), 1);

        Product p = Product.getFromEAN(getContext(), ean);
        assertNotNull(p);
        assertEquals(p.ean, ean);
        return p;
    }

    private void syncOneInvalidProduct(JSONObject obj)
    {
        assertFalse(syncJSON(oneSizedArray(obj).toString()));
    }

    public void testSyncIllFormattedJson()
    {
        syncJSONError("hei");
        syncJSONError("2");
        syncJSONError("[}");
    }

    public void testSyncNoProducts()
    {
        syncJSON("[]");
    }

    public void testSyncEmptyProduct()
    {
        syncJSONError("[{}]");
    }

    public void testSyncLimitedProduct1() throws JSONException
    {
        String ean = "111";
        JSONObject obj = new JSONObject();
        obj.put("ean", ean);
        // this can't be saved because of missing name
        syncOneInvalidProduct(obj);
    }

    public void testSyncLimitedProduct2() throws JSONException
    {
        String ean = "222";
        JSONObject obj = new JSONObject();
        obj.put("ean", ean);
        obj.put("name", "Test product 2");
        // This must work
        syncOneValidProduct(obj, ean);
    }

    public void testSyncLimitedProduct3() throws JSONException
    {
        String ean = "333";
        JSONObject obj = new JSONObject();
        JSONArray bi = new JSONArray();
        obj.put("ean", ean);
        obj.put("name", "Test product 3");
        obj.put("bi", bi);
        syncOneValidProduct(obj, ean);
    }

    public void testProduct1() throws JSONException
    {
        final String ean = "111";
        final String subst = "subst";
        JSONObject obj = new JSONObject();

        {
            JSONArray bi = new JSONArray();
            obj.put("ean", ean);
            obj.put("name", "Test product 3");

            {
                JSONObject ingr = new JSONObject();
                ingr.put("subst", subst);
                ingr.put("min", 40);
                ingr.put("max", 50);
                bi.put(ingr);
            }

            obj.put("bi", bi);
        }

        Product p = syncOneValidProduct(obj, ean);
        assertNotNull(p.badIngredients);
        assertEquals(p.badIngredients.length, 1);

        Product.Ingredient i1 = p.badIngredients[0];

        assertNotNull(i1);
        assertEquals(i1.min, 40);
        assertEquals(i1.max, 50);
        assertNotNull(i1.substance);
        assertEquals(i1.substance.name, subst);
    }

    public void testProduct2() throws JSONException
    {
        final String ean = "111";
        JSONObject obj = new JSONObject();
        final int ningr = 5;

        {
            JSONArray bi = new JSONArray();
            obj.put("ean", ean);
            obj.put("name", "Multiple bi product");

            for (int i = 0; i < ningr; ++i) {
                JSONObject ingr = new JSONObject();
                ingr.put("subst", "Substance " + i);
                ingr.put("min", i * 10);
                ingr.put("min", (i + 1) * 10);
                bi.put(ingr);
            }

            obj.put("bi", bi);
        }

        Product p = syncOneValidProduct(obj, ean);
        assertNotNull(p.badIngredients);
        assertEquals(p.badIngredients.length, ningr);
    }

    public void testFailingSync() throws JSONException
    {
        // insert a product
        testProduct1();

        // then test a failing sync
        Sync sync = new Sync(getContext()) {
            @Override
            protected InputStream getInputStream()
            {
                return new InputStream() {
                    public int read() throws IOException
                    {
                        throw new IOException();
                    }
                };
            }
        };
        assertFalse(sync.perform());

        // product1 should still be available
        Product p = Product.getFromEAN(getContext(), "111");
        assertNotNull(p);
        assertEquals(p.ean, "111");
    }
}
