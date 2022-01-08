package com.tencent.qqmusic.web;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.qqmusic.web.config.JsConfig;
import com.tencent.qqmusic.web.download.DownloadFile;
import com.tencent.qqmusic.web.download.DownloadStatus;
import com.tencent.qqmusic.web.entity.MusicInfo;
import com.tencent.qqmusic.web.notification.MusicNotificationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;
import jaygoo.library.m3u8downloader.OnM3U8DownloadListener;
import jaygoo.library.m3u8downloader.bean.M3U8Task;

public class MainActivity extends AppCompatActivity {
    private FrameLayout videoPlayerLayout;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private WebView webView;
    private ProgressDialog progressDialog;
    private String upCover = "";
    private String songName = "";
    private String singerName = "";
    private String mvName = "";
    private boolean playing;
    private int progress;
    private int total;
    private String cover = "";
    private String currentLyric = "";

    private List<String> queue = new LinkedList<>();
    private List<String> resourceUrls = new LinkedList<>();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        MusicNotificationManager.getInstance().init(this);

        M3U8DownloaderConfig
                .build(getApplicationContext())
                .setSaveDir("/storage/emulated/0/Android/media/" + getPackageName() + "/m3u8/");
        M3U8Downloader.getInstance().setOnM3U8DownloadListener(new OnM3U8DownloadListener() {
            @Override
            public void onDownloadProgress(M3U8Task task) {
                super.onDownloadProgress(task);
                progressDialog.setMessage((task.getProgress() * 100) + "%");
            }

            @Override
            public void onDownloadError(M3U8Task task, Throwable errorMsg) {
                super.onDownloadError(task, errorMsg);
                progressDialog.dismiss();
            }

            @Override
            public void onDownloadSuccess(M3U8Task task) {
                super.onDownloadSuccess(task);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(task.getM3U8().getTsList().get(0).getFilePath());

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp2t");
                        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, mvName + ".ts");
                        contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                        //只是往 MediaStore 里面插入一条新的记录，MediaStore 会返回给我们一个空的 Content Uri
                        //接下来问题就转化为往这个 Content Uri 里面写入
                        Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                        try {
                            FileInputStream inputStream = new FileInputStream(file);
                            OutputStream os = getContentResolver().openOutputStream(uri);
                            if (os != null) {
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    os.write(buffer, 0, len);
                                }
                            }
                            os.flush();
                            inputStream.close();
                            os.close();
                            file.delete();//拷贝出来后把目录内的删除掉
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();

                                new Share2.Builder(MainActivity.this)
                                        // 指定分享的文件类型
                                        .setContentType(ShareContentType.VIDEO)
                                        // 设置要分享的文件 Uri
                                        .setShareFileUri(uri)
                                        // 设置分享选择器的标题
                                        .setTitle(mvName)
                                        .build()
                                        // 发起分享
                                        .shareBySystem();
                            }
                        });
                    }
                }).start();


            }
        });

        videoPlayerLayout = findViewById(R.id.video_player_layout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Downloading...");
        webView = findViewById(R.id.web_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.requestFocus();
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSaveFormData(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAppCacheEnabled(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgentString("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");

        if (getIntent().getStringExtra("url") != null) {
            webView.loadUrl(getIntent().getStringExtra("url"));
            queue.add(getIntent().getStringExtra("url"));
        } else {
            webView.loadUrl("https://i.y.qq.com/n2/m/index.html");
            queue.add("https://i.y.qq.com/n2/m/index.html");
        }
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().startsWith("https://i.y.qq.com/")
                        || request.getUrl().toString().startsWith("https://y.qq.com/")) {

                    view.loadUrl(request.getUrl().toString());
                }
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://i.y.qq.com/")
                        || url.startsWith("https://y.qq.com/")) {

                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (url.contains(".m4a") || url.contains(".mp3") || url.contains(".mp4")) {
                    if (url.contains(".m4a") || url.contains(".mp3")) {
                        resourceUrls.add(url);
                    } else {
                        resourceUrls.add(url);
                    }
                }

                if (url.contains(".m3u8")) {
                    resourceUrls.add(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!queue.contains(url))
                    queue.add(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                resourceUrls.clear();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage.message().startsWith(getPackageName() + ":")) {
                    String message = consoleMessage.message().replace(getPackageName() + ":", "");
                    switch (message.split(":")[0]) {
                        case "songname":
                            songName = message.split(":")[1].trim();

                            break;
                        case "singername":
                            singerName = message.split(":")[1].trim();
                            break;
                        case "mvname":
                            mvName = message.split(":")[1].trim();
                            break;
                        case "playstate":
                            playing = "play".equals(message.split(":")[1]);
                            break;
                        case "progress":
                            progress = (int) Float.parseFloat(message.split(":")[1]);
                            break;
                        case "total":
                            total = (int) Float.parseFloat(message.split(":")[1]);
                            break;
                        case "cover":
                            cover = message.substring(message.indexOf(":") + 1);
                            if (TextUtils.isEmpty(upCover))
                                upCover = cover;
                            break;
                        case "lyric":
                            currentLyric = message.substring(message.indexOf(":") + 1);
                            break;
                    }
                    Log.v("onConsoleMessage", consoleMessage.message());
                    if ((!TextUtils.isEmpty(songName) && !TextUtils.isEmpty(singerName)
                            && !TextUtils.isEmpty(cover) && !MusicNotificationManager.getInstance().isShow()) || !upCover.equals(cover)) {
                        upCover = cover;
                        MusicInfo info = new MusicInfo();
                        info.setCurrentMusicUrl(webView.getUrl());
                        info.setMusicIconUrl(cover);
                        info.setPlaying(playing);
                        info.setCurrentProgress(progress);
                        info.setTotalProgress(total);
                        info.setMusicSinger(singerName);
                        info.setMusicName(songName);
                        info.setCurrentLyric(currentLyric);
                        MusicNotificationManager.getInstance().show(info);
                    } else if (progress > 0 && total > 0) {
                        MusicInfo info = new MusicInfo();
                        info.setPlaying(playing);
                        info.setCurrentProgress(progress);
                        info.setTotalProgress(total);
                        info.setMusicSinger(singerName);
                        info.setMusicName(songName);
                        info.setCurrentLyric(currentLyric);
                        info.setCurrentMusicUrl(webView.getUrl());
                        MusicNotificationManager.getInstance().updateProgress(info);
                    }
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                fullScreen();
                webView.setVisibility(View.GONE);
                videoPlayerLayout.setVisibility(View.VISIBLE);
                videoPlayerLayout.addView(view);
                customViewCallback = callback;
                super.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                fullScreen();
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                webView.setVisibility(View.VISIBLE);
                videoPlayerLayout.removeAllViews();
                videoPlayerLayout.setVisibility(View.GONE);
                super.onHideCustomView();
            }
        });

        webView.postDelayed(loadJSRunnable, 1000);

        findViewById(R.id.music_download).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }
            checkDownload();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkDownload();
        }
    }

    private void checkDownload() {
        if (resourceUrls.size() == 0) {
            Snackbar.make(webView, R.string.audio_or_video_not_found, 100).show();
            return;
        }
        int index = resourceUrls.size() - 1;
        checkDownloadIndex(index);

    }

    private void checkDownloadIndex(int index) {
        if (index < 0)
            return;

        String url = resourceUrls.get(index);
        if (url.contains(".m4a") || url.contains(".mp3") || url.contains(".mp4")) {
            String ext = "";
            boolean audioOrVideo = false;
            if (url.contains(".m4a")) {
                ext = ".m4a";
                audioOrVideo = true;
            } else if (url.contains(".mp3")) {
                ext = ".mp3";
                audioOrVideo = true;
            } else if (url.contains(".mp4")) {
                ext = ".mp4";
                audioOrVideo = false;
            }

            String finalExt = ext;
            boolean finalAudioOrVideo = audioOrVideo;
            String finalUrl = url;
            Snackbar.make(webView, String.format(getString(audioOrVideo ?
                    R.string.check_music : R.string.check_video), url), 5000)
                    .setAction(R.string.download, v1 -> {
                        downloadFile(finalUrl, finalAudioOrVideo ? Environment.DIRECTORY_MUSIC : Environment.DIRECTORY_MOVIES
                                , finalAudioOrVideo ? songName + " · " + singerName : mvName, finalExt);
                    }).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    checkDownloadIndex(index - 1);
                }
            }).show();
        }

        if (url.contains(".m3u8")) {
            String finalUrl1 = url;
            Snackbar.make(webView, String.format(getString(R.string.check_video), url), 5000)
                    .setAction(R.string.download, v1 -> {
                        progressDialog.show();
                        M3U8Downloader.getInstance().download(finalUrl1);
                    }).addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    checkDownloadIndex(index - 1);
                }
            }).show();
        }
    }

    private void downloadFile(String url, String type, String filename, String ext) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadFile.download(MainActivity.this, url, filename + ext
                        , type
                        , (status, uri, proGress, currentDownProGress, totalProGress) -> {
                            runOnUiThread(() -> {
                                progressDialog.setMessage(proGress + "%");
                                if (status == DownloadStatus.LOADED) {
                                    progressDialog.dismiss();
                                    new Share2.Builder(MainActivity.this)
                                            // 指定分享的文件类型
                                            .setContentType(type.equals(Environment.DIRECTORY_MUSIC) ? ShareContentType.AUDIO : ShareContentType.VIDEO)
                                            // 设置要分享的文件 Uri
                                            .setShareFileUri(uri)
                                            // 设置分享选择器的标题
                                            .setTitle(filename + ext)
                                            .build()
                                            // 发起分享
                                            .shareBySystem();
                                }
                            });

                        });
            }
        }).start();
    }

    private void fullScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private Runnable loadJSRunnable = new Runnable() {
        @Override
        public void run() {
            webView.evaluateJavascript(JsConfig.build(getPackageName()), new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
            webView.postDelayed(loadJSRunnable, 1000);
        }
    };

    @Override
    public void onBackPressed() {
        if (queue.size() > 1) {
            if (queue.get(queue.size() - 1).contains("taoge.html") || queue.get(queue.size() - 1).contains("playsong.html")) {
                findViewById(R.id.music_controls_layout).setVisibility(View.GONE);
                MusicNotificationManager.getInstance().cancel();
            }
            queue.remove(queue.get(queue.size() - 1));//先去掉当前页
            webView.loadUrl(queue.remove(queue.size() - 1));
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void playOrPause(boolean playing) {
        webView.evaluateJavascript("var audios = document.getElementsByTagName('audio');\n" +
                "audios[audios.length - 1]." + (playing ? "play" : "pause") + "();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }

    public void nextMusic() {
        webView.evaluateJavascript(String.format(JsConfig.taogeNextOrPreJs, "+"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });

        webView.evaluateJavascript(String.format(JsConfig.playsongNextOrPreJs, "+"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }

    public void preMusic() {
        webView.evaluateJavascript(String.format(JsConfig.taogeNextOrPreJs, "-"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });

        webView.evaluateJavascript(String.format(JsConfig.playsongNextOrPreJs, "-"), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }
}