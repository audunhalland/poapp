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

    private void syncOneValidProduct(JSONObject obj, String ean)
    {
        syncJSON(oneSizedArray(obj).toString());

        Product p = Product.getFromEAN(getContext(), ean);
        assertNotNull(p);
        assertEquals(p.ean, ean);
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
        obj.put("name", "Test product 2");
        obj.put("bi", bi);
        syncOneValidProduct(obj, ean);
    }
}
