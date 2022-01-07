package com.tencent.qqmusic.web.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.qqmusic.web.notification.MusicNotificationManager;

public class MusicPreReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MusicNotificationManager.getInstance().preMusic();
    }
}
