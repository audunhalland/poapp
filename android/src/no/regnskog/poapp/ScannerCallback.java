package no.regnskog.poapp;

import com.google.zxing.Result;

public interface ScannerCallback
{
    void onSuccess(Result result);
    void onError(String msg);
}
