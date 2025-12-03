package com.tencent.qqmusic.web.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.qqmusic.web.MainActivity;
import com.tencent.qqmusic.web.R;
import com.tencent.qqmusic.web.broadcastreceiver.MusicNextReceiver;
import com.tencent.qqmusic.web.broadcastreceiver.MusicPlayOrPauseReceiver;
import com.tencent.qqmusic.web.broadcastreceiver.MusicPreReceiver;
import com.tencent.qqmusic.web.entity.MusicInfo;

public class MusicNotificationService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "music_notification_channel";
    private static final String CHANNEL_NAME = "Music Notification";

    private NotificationManager notificationManager;
    private Notification notification;
    private MusicInfo currentMusicInfo;

    // 缓存当前的Bitmap，防止频繁闪烁或重新加载
    private Bitmap currentBitmap = null;
    private String currentBitmapUrl = "";

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicNotificationService getService() {
            return MusicNotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        // 初始构建一个空通知
        notification = createNotification(null, null);
    }

    // ... onStartCommand, onBind, createNotificationChannel 代码保持不变 ...

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback notifications");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotification(MusicInfo info) {
        currentMusicInfo = info;
        updateNotificationContent(info);

        // Android 14+ 前台服务类型适配
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    public void updateNotification(MusicInfo info) {
        currentMusicInfo = info;
        updateNotificationContent(info);
        // 这里不需要单独 notify，因为 updateNotificationContent 内部逻辑会处理
    }

    public void cancelNotification() {
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
        currentMusicInfo = null;
        currentBitmap = null;
    }

    /**
     * 核心修复方法：分离图片加载与通知更新逻辑
     */
    private void updateNotificationContent(MusicInfo info) {
        if (info == null ) return;
        boolean isUrlChanged = false;
        if(info.getMusicIconUrl() != null) {
            // 1. 先用当前缓存的 Bitmap (如果有) 或者默认图构建并显示通知
            // 这样可以保证文字进度条立即更新，不会因为等图片下载而卡顿
            isUrlChanged = !info.getMusicIconUrl().equals(currentBitmapUrl);

            if (isUrlChanged) {
                currentBitmap = null; // URL变了，旧图作废
                currentBitmapUrl = info.getMusicIconUrl();
            }
        }

        // 构建并刷新通知（此时用的是旧图或占位图）
        notification = createNotification(info, currentBitmap);
        notificationManager.notify(NOTIFICATION_ID, notification);

        // 2. 如果 URL 变了，去后台加载新图片
        if (isUrlChanged) {
            new Thread(() -> {
                try {
                    Bitmap bitmap = ImageLoader.getInstance().loadImageSync(info.getMusicIconUrl());
                    if (bitmap != null) {
                        currentBitmap = bitmap;

                        // 【关键点】图片加载完后，必须基于新图片 *重新构建* Notification 对象
                        // 不能复用旧的 builder 或 remoteViews
                        Notification newNotification = createNotification(info, bitmap);

                        // 更新全局变量
                        notification = newNotification;

                        // 再次通知 NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, newNotification);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * 辅助方法：生成一个新的 Notification 对象
     * 每次更新都应该调用此方法生成全新的对象
     */
    private Notification createNotification(MusicInfo info, Bitmap iconBitmap) {
        // 每次都 new 一个新的 RemoteViews，避免对象复用导致的状态混乱
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.music_notification_layout);

        if (info != null) {
            // 更新文字
            remoteViews.setTextViewText(R.id.music_name, info.getMusicName());
            remoteViews.setTextViewText(R.id.music_singer, info.getMusicSinger());
            if (!TextUtils.isEmpty(info.getCurrentLyric())) {
                remoteViews.setTextViewText(R.id.music_current_lyric, info.getCurrentLyric());
            }

            // 更新播放按钮状态
            remoteViews.setImageViewResource(R.id.music_play,
                    info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);

            // 更新进度
            remoteViews.setProgressBar(R.id.music_progress_bar,
                    info.getTotalProgress(), info.getCurrentProgress(), false);

            // 设置点击 Intent (PendingIntent)
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

            remoteViews.setOnClickPendingIntent(R.id.music_play,
                    PendingIntent.getBroadcast(this, 0, new Intent(this, MusicPlayOrPauseReceiver.class), flags));
            remoteViews.setOnClickPendingIntent(R.id.music_next,
                    PendingIntent.getBroadcast(this, 1, new Intent(this, MusicNextReceiver.class), flags));
            remoteViews.setOnClickPendingIntent(R.id.music_pre,
                    PendingIntent.getBroadcast(this, 2, new Intent(this, MusicPreReceiver.class), flags));
        }

        // 设置图片 (如果有传入Bitmap则用Bitmap，否则用默认Logo)
        if (iconBitmap != null) {
            remoteViews.setImageViewBitmap(R.id.music_icon, iconBitmap);
        } else {
            remoteViews.setImageViewResource(R.id.music_icon, R.mipmap.qqmusic_logo);
        }

        // Content Intent
        Intent appIntent = new Intent(this, MainActivity.class);
        if (info != null) {
            appIntent.putExtra("url", info.getCurrentMusicUrl());
        }
        appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 3, appIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build Notification
        Notification.Builder builder;
        builder = new Notification.Builder(this, CHANNEL_ID);

        builder.setSmallIcon(R.drawable.small_qqmusic_icon)
                .setLargeIcon(Icon.createWithResource(this, R.mipmap.qqmusic_logo))
                .setContentIntent(contentIntent)
                .setCustomContentView(remoteViews)     // 折叠视图
                .setCustomBigContentView(remoteViews)  // 展开视图 (重要，Android 12+ 建议设置)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setStyle(new Notification.MediaStyle()); // 设为 MediaStyle

        return builder.build();
    }

    // ... onDestroy, isNotificationShowing 保持不变 ...
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
    }

    public boolean isNotificationShowing() {
        return currentMusicInfo != null;
    }
}