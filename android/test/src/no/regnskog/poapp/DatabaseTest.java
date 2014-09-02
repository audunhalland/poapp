package no.regnskog.poapp;

import android.test.AndroidTestCase;
import junit.framework.Test;

public class DatabaseTest extends AndroidTestCase
{
    public void testProductGetFromEAN()
    {
        Product p = Product.getFromEAN(getContext(), "");
        assertNull("assert no product found", p);
    }
}
