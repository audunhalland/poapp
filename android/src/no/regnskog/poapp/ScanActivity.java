package no.regnskog.poapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.Result;

public class ScanActivity extends Activity
{
    private static final String TAG = "ScanActivity";

    Scanner mScanner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        startScanning();

        Log.d(TAG, "onResume done");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopScanning();
    }

    void startScanning()
    {
        if (mScanner != null) {
            return;
        }

        mScanner = new Scanner(createScannerCallback());
        mScanner.initCamera();

        SurfaceView v = (SurfaceView)findViewById(R.id.preview_view);

        if (v.getHolder().getSurface() != null) {
            mScanner.setPreviewDisplay(v.getHolder());
            mScanner.scan();
        } else {
            v.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {}
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {}

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
                {
                    Log.d(TAG, "surfaceChanged");

                    if (mScanner != null) {
                        mScanner.setPreviewDisplay(holder);
                        mScanner.scan();
                    }
                }
            });
        }
    }

    void stopScanning()
    {
        if (mScanner != null) {
            mScanner.kill();
            mScanner = null;
        }
    }

    AlertDialog makeDialog(String title, String msg)
    {
        AlertDialog d = new AlertDialog.Builder(this).create();
        d.setTitle(title);
        d.setMessage(msg);
        return d;
    }

    ScannerCallback createScannerCallback()
    {
        return new ScannerCallback() {
            public void onSuccess(Result result)
            {
                Intent i = new Intent();
                i.setData(Uri.fromParts("ean13", result.getText(), null));

                setResult(RESULT_OK, i);
                finish();
            }

            public void onError(String msg)
            {
                makeDialog("Error", msg).show();
            }
        };
    }
}
