package no.regnskog.poapp;

import android.test.AndroidTestCase;
import com.google.gson.stream.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import junit.framework.Test;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncTest extends AndroidTestCase
{
    private void syncJSON(String json) throws IOException
    {
        Sync sync = new Sync(getContext());
        sync.sync(new JsonReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes()))));
    }

    private void syncJSONError(String json)
    {
        try {
            syncJSON(json);
            fail("no exception occurred");
        } catch (IOException e) {
        }
    }

    private void syncOneValidProduct(JSONObject obj, String ean) throws IOException
    {
        JSONArray array = new JSONArray();
        array.put(obj);
        syncJSON(array.toString());

        Product p = Product.getFromEAN(getContext(), ean);
        assertNotNull("product not null", p);
        assertEquals(p.ean, ean);
    }

    public void testSyncIllFormattedJson() throws IOException
    {
        syncJSONError("hei");
        syncJSONError("2");
        syncJSONError("[}");
    }

    public void testSyncNoProducts() throws IOException
    {
        syncJSON("[]");
    }

    public void testSyncEmptyProduct() throws IOException
    {
        syncJSON("[{}]");
    }

    public void testSyncLimitedProduct1() throws IOException, JSONException
    {
        String ean = "111";
        JSONObject obj = new JSONObject();
        obj.put("ean", ean);
        syncOneValidProduct(obj, ean);
    }

    public void testSyncLimitedProduct2() throws IOException, JSONException
    {
        String ean = "222";
        JSONObject obj = new JSONObject();
        obj.put("ean", ean);
        obj.put("name", "Test product 2");
        syncOneValidProduct(obj, ean);
    }

    public void testSyncLimitedProduct3() throws IOException, JSONException
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
