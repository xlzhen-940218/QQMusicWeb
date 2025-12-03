package com.tencent.qqmusic.web.notification;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.qqmusic.web.MainActivity;
import com.tencent.qqmusic.web.R;
import com.tencent.qqmusic.web.entity.MusicInfo;
import com.tencent.qqmusic.web.service.MusicNotificationService;

public class MusicNotificationManager {
    private boolean showing;
    private Activity activity;
    private MusicInfo info;
    private MusicNotificationService notificationService;
    private boolean isBound = false;

    private static volatile MusicNotificationManager instance;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicNotificationService.LocalBinder binder = (MusicNotificationService.LocalBinder) service;
            notificationService = binder.getService();
            isBound = true;
            // 连接成功后，如果之前请求了 show，这里补发
            if (info != null && showing) {
                notificationService.showNotification(info);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
            notificationService = null;
        }
    };

    public static MusicNotificationManager getInstance() {
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

        activity.findViewById(R.id.music_play).setOnClickListener(v -> {
            playOrPause();
        });

        activity.findViewById(R.id.music_next).setOnClickListener(v -> {
            nextMusic();
        });

        activity.findViewById(R.id.music_pre).setOnClickListener(v -> {
            preMusic();
        });

        // 检查通知权限 (Android 13+)
        checkNotificationPermission();

        bindService();
    }

    // 【新增】检查并请求通知权限
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) { // Android 13
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 这里简单请求，建议在 Activity 中处理 onRequestPermissionsResult 回调
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void bindService() {
        if (!isBound) {
            Intent intent = new Intent(activity, MusicNotificationService.class);
            activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        if (isBound) {
            try {
                activity.unbindService(serviceConnection);
            } catch (Exception e) {
                // 忽略已解绑异常
            }
            isBound = false;
            notificationService = null;
        }
    }

    public void show(MusicInfo info) {
        this.info = info;
        this.showing = true; // 标记为显示状态

        // Update UI controls
        ((TextView) activity.findViewById(R.id.music_name)).setText(info.getMusicName());
        ((TextView) activity.findViewById(R.id.music_singer)).setText(info.getMusicSinger());
        if (info.getCurrentLyric() != null && !info.getCurrentLyric().isEmpty()) {
            ((TextView) activity.findViewById(R.id.music_current_lyric)).setText(info.getCurrentLyric());
        }
        ((ImageView) activity.findViewById(R.id.music_play)).setImageResource(
                info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);
        ImageLoader.getInstance().displayImage(info.getMusicIconUrl(),
                (ImageView) activity.findViewById(R.id.music_icon));

        if (isBound && notificationService != null) {
            notificationService.showNotification(info);
        } else {
            // 如果没绑定，尝试绑定，onServiceConnected 会处理 showNotification
            bindService();
        }

        if (activity.findViewById(R.id.music_controls_layout).getVisibility() == View.GONE) {
            activity.findViewById(R.id.music_controls_layout).setVisibility(View.VISIBLE);
        }
    }

    public void updateProgress(MusicInfo info) {
        this.info = info;

        ((TextView) activity.findViewById(R.id.music_name)).setText(info.getMusicName());
        ((TextView) activity.findViewById(R.id.music_singer)).setText(info.getMusicSinger());
        if (info.getCurrentLyric() != null && !info.getCurrentLyric().isEmpty()) {
            ((TextView) activity.findViewById(R.id.music_current_lyric)).setText(info.getCurrentLyric());
        }
        ((ImageView) activity.findViewById(R.id.music_play)).setImageResource(
                info.isPlaying() ? R.drawable.music_pause : R.drawable.music_play);

        if (isBound && notificationService != null) {
            notificationService.updateNotification(info);
        }

        if (activity.findViewById(R.id.music_controls_layout).getVisibility() == View.GONE) {
            activity.findViewById(R.id.music_controls_layout).setVisibility(View.VISIBLE);
        }
    }

    public boolean isShow() {
        return showing;
    }

    public void playOrPause() {
        if (info != null && activity != null) {
            info.setPlaying(!info.isPlaying());
            updateProgress(info);
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).playOrPause(info.isPlaying());
            }
        }
    }

    public void nextMusic() {
        if (info != null && activity != null) {
            info.setPlaying(false);
            updateProgress(info);
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).nextMusic();
            }
        }
    }

    public void preMusic() {
        if (info != null && activity != null) {
            info.setPlaying(false);
            updateProgress(info);
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).preMusic();
            }
        }
    }

    public void cancel() {
        if (isBound && notificationService != null) {
            notificationService.cancelNotification();
        }
        showing = false;
        unbindService();
    }

    public void onDestroy() {
        unbindService();
    }
}