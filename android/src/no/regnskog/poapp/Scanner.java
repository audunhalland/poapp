package no.regnskog.poapp;

import java.util.concurrent.CountDownLatch;

import android.hardware.Camera;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 *  Scanner represents one "scan" lifecycle:
 *  1. open camera
 *  2. look for code
 *  3. publish code
 *
 *  Must be cleaned up on activity pause
 */
public class Scanner
{
    private static final String TAG = "Scanner";

    private static final int MSG_INIT_CAMERA = 0;
    private static final int MSG_SET_PREVIEW_DISPLAY = 1;
    private static final int MSG_SCAN = 2;
    private static final int MSG_SCAN_SUCCEEDED = 3;
    private static final int MSG_SCAN_UNRECOGNIZED = 4;
    private static final int MSG_SCAN_ERROR = 5;
    private static final int MSG_CAMERA_ERROR = 6;
    private static final int MSG_KILL = 7;

    ScannerCallback mCallback;
    Camera mCamera;

    Handler mUIHandler;
    Handler mBGHandler;

    Runnable mBGRunnable;
    Thread mBGThread;
    final CountDownLatch mBGHandlerLatch;
    
    public Scanner(ScannerCallback callback) {
        mCallback = callback;
        mBGHandlerLatch = new CountDownLatch(1);

        Log.d(TAG, "Creating runnable");

        mBGRunnable = new Runnable() {
            public void run()
            {
                Log.d(TAG, "BG Runnable started");

                Looper.prepare();
                mBGHandler = createBGHandler();
                Log.d(TAG, "BG counting down...");
                mBGHandlerLatch.countDown();
                Log.d(TAG, "BG looping...");
                Looper.loop();
            }
        };

        mBGThread = new Thread(mBGRunnable);
        mBGThread.start();

        Log.d(TAG, "Thread started");

        mUIHandler = createUIHandler();
    }

    /**
     *  Before we can communicate with the background thread, we
     *  need to wait until its handler is created.
     */
    void awaitBGInit()
    {
        try {
            mBGHandlerLatch.await();
        } catch (InterruptedException e) {
        }
    }

    public void initCamera()
    {
        awaitBGInit();
        Message.obtain(mBGHandler, MSG_INIT_CAMERA).sendToTarget();
    }

    /**
     *  Sets the scanner preview display
     */
    public void setPreviewDisplay(SurfaceHolder holder)
    {
        awaitBGInit();
        Message.obtain(mBGHandler, MSG_SET_PREVIEW_DISPLAY, holder).sendToTarget();
    }

    public void scan()
    {
        /* TODO: fetch scanning frame */

        Message.obtain(mBGHandler, MSG_SCAN).sendToTarget();
    }

    public void kill()
    {
        awaitBGInit();

        /* send the kill signal which we will synchronize */
        Message.obtain(mBGHandler, MSG_KILL).sendToTarget();

        try {
            /* 1 sec max */
            mBGThread.join(1000L);
        } catch (InterruptedException e) {}
    }

    /**
     *  Make the handler that dispatches messages on the UI thread
     */
    private Handler createUIHandler()
    {
        return new Handler() {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                case MSG_SCAN_SUCCEEDED:
                    /* found something */
                    break;
                case MSG_SCAN_UNRECOGNIZED:
                    /* retry */
                    scan();
                    break;
                case MSG_CAMERA_ERROR:
                    /* generic error that should be displayed in UI */
                    mCallback.onError("Camera error");
                }
            }
        };
    }

    /**
     *  Make the handler that dispatches messages on the scanner background thread
     */
    private Handler createBGHandler()
    {
        /*final Scanner scn = this;*/

        Log.d(TAG, "createDBHandler");

        return new Handler() {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                case MSG_INIT_CAMERA:
                    initCameraBG();
                    break;
                case MSG_SET_PREVIEW_DISPLAY:
                    setPreviewDisplayBG((SurfaceHolder)msg.obj);
                    break;
                case MSG_SCAN:
                    scanBG();
                    break;
                case MSG_KILL:
                    killBG();
                    Looper.myLooper().quit();
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void initCameraBG()
    {
        Log.d(TAG, "BG: initializing camera");

        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "failed to open Camera");
            e.printStackTrace();

            Message.obtain(mUIHandler, MSG_CAMERA_ERROR).sendToTarget();
        }
    }

    private void killBG()
    {
        Log.d(TAG, "BG: killing camera");

        if (mCamera != null) {
            mCamera.release();
        }
    }

    private void setPreviewDisplayBG(SurfaceHolder holder)
    {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Message.obtain(mUIHandler, MSG_CAMERA_ERROR).sendToTarget();
            return;
        }

        // send message to ourselves to start scanning
        Message.obtain(mUIHandler, MSG_SCAN).sendToTarget();
    }

    private void scanBG()
    {
        if (mCamera == null) {
            Message.obtain(mUIHandler, MSG_SCAN_ERROR);
        }

        /* work work work */
        boolean yes = false;

        if (yes) {
            Message.obtain(mUIHandler, MSG_SCAN_SUCCEEDED);
        } else {
            Message.obtain(mUIHandler, MSG_SCAN_UNRECOGNIZED);
        }
    }
}
