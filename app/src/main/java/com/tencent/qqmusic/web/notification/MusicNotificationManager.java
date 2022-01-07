package com.tencent.qqmusic.web.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.qqmusic.web.MainActivity;
import com.tencent.qqmusic.web.R;
import com.tencent.qqmusic.web.broadcastreceiver.MusicNextReceiver;
import com.tencent.qqmusic.web.broadcastreceiver.MusicPlayOrPauseReceiver;
import com.tencent.qqmusic.web.broadcastreceiver.MusicPreReceiver;
import com.tencent.qqmusic.web.entity.MusicInfo;

public class MusicNotificationManager {
    private boolean showing;
    private NotificationManager manager;
    private Notification notification;

    private Activity activity;

    private MusicInfo info;

    private static volatile MusicNotificationManager instance;

    public static MusicNotificationManager getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (MusicNotificationManager.class) {
                if (instance == null)
                    instance = new MusicNotificationManager();
            }
        }
        return instance;
    }

    public MusicNotificationManager() {
    }

    public void init(Activity activity) {
        this.activity = activity;
        manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(this.getClass().getName(), this.getClass().getSimpleName(), NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(this.getClass().getName());
        }

        builder.setSmallIcon(R.drawable.small_qqmusic_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(activity, R.mipmap.qqmusic_logo));
        }

        notification = builder.build();
        notification.contentView = new RemoteViews(activity.getPackageName(), R.layout.music_notification_layout);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;


        notification.priority = Notification.PRIORITY_LOW;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;


        notification.contentView.setOnClickPendingIntent(R.id.music_play
                , PendingIntent.getBroadcast(activity, 0, new Intent(activity, MusicPlayOrPauseReceiver.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT));

        activity.findViewById(R.id.music_play).setOnClickListener(v->{
            playOrPause();
        });

        notification.contentView.setOnClickPendingIntent(R.id.music_next
                , PendingIntent.getBroadcast(activity, 0, new Intent(activity, MusicNextReceiver.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT));

        activity.findViewById(R.id.music_next).setOnClickListener(v->{
            nextMusic();
        });

        Intent preIntent = new Intent(activity, MusicPlayOrPauseReceiver.class);
        preIntent.putExtra("type", "pre");
        notification.contentView.setOnClickPendingIntent(R.id.music_pre
                , PendingIntent.getBroadcast(activity, 0, new Intent(activity, MusicPreReceiver.class)
                        , PendingIntent.FLAG_UPDATE_CURRENT));

        activity.findViewById(R.id.music_pre).setOnClickListener(v->{
            preMusic();
        });
    }

    public void show(MusicInfo info) {
        this.info = info;
        ((TextView) activity.findViewById(R.id.music_name)).setText(info.getMusicName());
        ((TextView) activity.findViewById(R.id.music_singer)).setText(info.getMusicSinger());

        notification.contentView.setTextViewText(R.id.music_name, info.getMusicName());
        notification.contentView.setTextViewText(R.id.music_singer, info.getMusicSinger());
        if (!TextUtils.isEmpty(info.getCurrentLyric())) {
            notification.contentView.setTextViewText(R.id.music_current_lyric, info.getCurrentLyric());
            ((TextView) activity.findViewById(R.id.music_current_lyric)).setText(info.getCurrentLyric());
        }
        notification.contentView.setImageViewResource(R.id.music_play
                , info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);
        ((ImageView) activity.findViewById(R.id.music_play)).setImageResource(info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(info.getMusicIconUrl());
                notification.contentView.setImageViewBitmap(R.id.music_icon, bitmap);
                manager.notify(0, notification);
            }
        }).start();
        ImageLoader.getInstance().displayImage(info.getMusicIconUrl(), (ImageView) activity.findViewById(R.id.music_icon));
        notification.contentView.setProgressBar(R.id.music_progress_bar, info.getTotalProgress()
                , info.getCurrentProgress(), false);

        manager.notify(0, notification);
        showing = true;

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("url", info.getCurrentMusicUrl());
        notification.contentIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(activity.findViewById(R.id.music_controls_layout).getVisibility() == View.GONE){
            activity.findViewById(R.id.music_controls_layout).setVisibility(View.VISIBLE);
        }
    }

    public void updateProgress(MusicInfo info) {
        this.info = info;

        notification.contentView.setTextViewText(R.id.music_name, info.getMusicName());
        notification.contentView.setTextViewText(R.id.music_singer, info.getMusicSinger());
        ((TextView) activity.findViewById(R.id.music_name)).setText(info.getMusicName());
        ((TextView) activity.findViewById(R.id.music_singer)).setText(info.getMusicSinger());
        if (!TextUtils.isEmpty(info.getCurrentLyric())) {
            notification.contentView.setTextViewText(R.id.music_current_lyric, info.getCurrentLyric());
            ((TextView) activity.findViewById(R.id.music_current_lyric)).setText(info.getCurrentLyric());
        }
        notification.contentView.setImageViewResource(R.id.music_play
                , info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);
        ((ImageView) activity.findViewById(R.id.music_play)).setImageResource(info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);

        notification.contentView.setProgressBar(R.id.music_progress_bar, info.getTotalProgress()
                , info.getCurrentProgress(), false);

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("url", info.getCurrentMusicUrl());
        notification.contentIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.notify(0, notification);
        if(activity.findViewById(R.id.music_controls_layout).getVisibility() == View.GONE){
            activity.findViewById(R.id.music_controls_layout).setVisibility(View.VISIBLE);
        }
    }

    public boolean isShow() {
        return showing;
    }

    public void playOrPause() {
        if (info != null) {
            info.setPlaying(!info.isPlaying());
            updateProgress(info);
            ((MainActivity) activity).playOrPause(info.isPlaying());
        }

    }

    public void nextMusic() {
        if (info != null) {
            info.setPlaying(false);
            updateProgress(info);
            ((MainActivity) activity).nextMusic();
        }
    }

    public void preMusic() {
        if (info != null) {
            info.setPlaying(false);
            updateProgress(info);
            ((MainActivity) activity).preMusic();
        }
    }

    public void cancel() {

        manager.cancel(0);
    }
}
