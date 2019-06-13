package com.mpstats.mpstats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mpstats.mpstats.Data.MPStatsData;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            String referrer = null;
            referrer = extras.getString("referrer");
            MPStatsData.setInstallReferrer(referrer);
        }
        catch (Exception e) {

        }
    }
}