package no.regnskog.poapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;

import com.google.zxing.Result;

public class ScanActivity extends Activity implements SurfaceHolder.Callback
{
    private static final String TAG = "ScanActivity";

    Scanner mScanner;

    /** Called when the activity is first created. */
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

        mScanner = new Scanner(createScannerCallback());
        mScanner.initCamera();

        SurfaceView v = (SurfaceView)findViewById(R.id.preview_view);
        v.getHolder().addCallback(this);

        Log.d(TAG, "onResume done");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopScanning();
    }

    void stopScanning()
    {
        if (mScanner != null) {
            mScanner.kill();
            mScanner = null;
        }
    }

    void showDialog(String text)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(text);
        alertDialog.setMessage(text);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
            }
        });
        //alertDialog.setIcon(R.drawable.icon);
        alertDialog.show();
    }

    ScannerCallback createScannerCallback()
    {
        final ScanActivity sa = this;

        return new ScannerCallback() {
            public void onSuccess(Result result)
            {
                showDialog("found: " + result.getText());
                stopScanning();
            }

            public void onError(String msg)
            {
                showDialog(msg);
            }
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d(TAG, "surfaceChanged");

        mScanner.setPreviewDisplay(holder);
        mScanner.scan();
    }
}
