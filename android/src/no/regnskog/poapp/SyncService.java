package no.regnskog.poapp;

import android.app.IntentService;
import android.content.Intent;

public class SyncService extends IntentService
{
    public SyncService()
    {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent i)
    {
        Sync sync = new Sync(this);
        boolean result = sync.perform();
        Intent broadcast;

        if (result) {
            broadcast = new Intent(Constants.BROADCAST_SYNC_SUCCESS);
        } else {
            broadcast = new Intent(Constants.BROADCAST_SYNC_FAILED);
        }

        sendBroadcast(broadcast);
    }
}
