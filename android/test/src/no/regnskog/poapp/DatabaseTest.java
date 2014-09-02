package no.regnskog.poapp;

import android.test.AndroidTestCase;

public class DatabaseTest extends AndroidTestCase
{
    public void testProductGetFromEAN()
    {
        Product p = Product.getFromEAN(getContext(), "[invalid_ean]");
        assertNull("assert no product found", p);
    }
}
