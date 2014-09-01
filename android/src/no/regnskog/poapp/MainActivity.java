package no.regnskog.poapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";

    static final int SCAN_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
        case SCAN_REQUEST:
            handleScanResult(resultCode, data);
            break;
        }
    }

    public void scanClicked(View view)
    {
        startActivityForResult(new Intent(this, ScanActivity.class),
                               SCAN_REQUEST);
    }

    private void showProduct(String ean, Product p)
    {
        AlertDialog d = new AlertDialog.Builder(this).create();
        if (p != null) {
            d.setTitle(p.name);
            //d.setMessage("Innhold: " + Long.toString(p.min) + "-" + Long.toString(p.max) + "%");
        } else {
            d.setTitle("Unknown product");
            d.setMessage(ean);
        }

        d.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

        d.show();
    }

    private void handleScanResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String ean = uri.getSchemeSpecificPart();
            showProduct(ean, Product.getFromEAN(this, ean));
        } else {
        }
    }

    public void updateClicked(View view)
    {
        //DatabaseTest t = new DatabaseTest(this);
        //t.insertTestProducts();
        Sync sync = new Sync(this);
        sync.perform();
    }
}
