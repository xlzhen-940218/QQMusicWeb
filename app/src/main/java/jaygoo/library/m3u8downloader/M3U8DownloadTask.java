package jaygoo.library.m3u8downloader;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jaygoo.library.m3u8downloader.bean.M3U8;
import jaygoo.library.m3u8downloader.bean.M3U8Ts;
import jaygoo.library.m3u8downloader.utils.M3U8Log;
import jaygoo.library.m3u8downloader.utils.MUtils;


class M3U8DownloadTask {
    private static final int WHAT_ON_ERROR = 1001;
    private static final int WHAT_ON_PROGRESS = 1002;
    private static final int WHAT_ON_SUCCESS = 1003;
    private static final int WHAT_ON_START_DOWNLOAD = 1004;
    private OnTaskDownloadListener onTaskDownloadListener;
    private String encryptKey = null;
    private String m3u8FileName = "local.m3u8";

    private String saveDir;

    private volatile int curTs = 0;

    private volatile int totalTs = 0;

    private volatile String tsFilePath;

    private volatile long itemFileSize = 0L;

    private volatile long totalFileSize = 0L;


    private volatile boolean isStartDownload = true;

    private long curLength = 0L;


    private boolean isRunning = false;


    private int threadCount = 3;


    private int readTimeout = 1800000;


    private int connTimeout = 10000;

    private Timer netSpeedTimer;

    private ExecutorService executor;

    private M3U8 currentM3U8;

    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    M3U8DownloadTask.this.onTaskDownloadListener.onError((Throwable) msg.obj);
                    break;

                case 1004:
                    M3U8DownloadTask.this.onTaskDownloadListener.onStartDownload(M3U8DownloadTask.this.totalTs, M3U8DownloadTask.this.curTs);
                    break;

                case 1002:
                    M3U8DownloadTask.this.onTaskDownloadListener.onDownloading(M3U8DownloadTask.this.totalFileSize, M3U8DownloadTask.this.itemFileSize, M3U8DownloadTask.this.totalTs, M3U8DownloadTask.this.curTs);
                    break;

                case 1003:
                    if (M3U8DownloadTask.this.netSpeedTimer != null) {
                        M3U8DownloadTask.this.netSpeedTimer.cancel();
                    }
                    M3U8DownloadTask.this.onTaskDownloadListener.onSuccess(M3U8DownloadTask.this.currentM3U8);
                    break;
            }

            return true;
        }
    });

    public M3U8DownloadTask() {

        this.connTimeout = M3U8DownloaderConfig.getConnTimeout();

        this.readTimeout = M3U8DownloaderConfig.getReadTimeout();

        this.threadCount = M3U8DownloaderConfig.getThreadCount();
    }


    public void download(String url, OnTaskDownloadListener onTaskDownloadListener) {

        this.saveDir = MUtils.getSaveFileDir(url);

        M3U8Log.d("start download ,SaveDir: " + this.saveDir);

        this.onTaskDownloadListener = onTaskDownloadListener;

        if (!isRunning()) {

            getM3U8Info(url);
        } else {

            handlerError(new Throwable("Task running"));
        }
    }


    public void setEncryptKey(String encryptKey) {

        this.encryptKey = encryptKey;
    }

    public String getEncryptKey() {

        return this.encryptKey;
    }


    public boolean isRunning() {

        return this.isRunning;
    }


    private void getM3U8Info(String url) {

        M3U8InfoManger.getInstance().getM3U8Info(url, new OnM3U8InfoListener() {
            public void onSuccess(final M3U8 m3U8) {

                M3U8DownloadTask.this.currentM3U8 = m3U8;

                (new Thread() {
                    public void run() {
                        try {

                            M3U8DownloadTask.this.startDownload(m3U8);

                            if (M3U8DownloadTask.this.executor != null) {

                                M3U8DownloadTask.this.executor.shutdown();
                            }

                            while (M3U8DownloadTask.this.executor != null && !M3U8DownloadTask.this.executor.isTerminated()) {

                                Thread.sleep(100L);
                            }

                            if (M3U8DownloadTask.this.isRunning) {

                                File m3u8File = MUtils.createLocalM3U8(new File(M3U8DownloadTask.this.saveDir), M3U8DownloadTask.this.m3u8FileName, M3U8DownloadTask.this.currentM3U8);

                                M3U8DownloadTask.this.currentM3U8.setM3u8FilePath(m3u8File.getPath());

                                M3U8DownloadTask.this.currentM3U8.setDirFilePath(M3U8DownloadTask.this.saveDir);

                                M3U8DownloadTask.this.currentM3U8.getFileSize();

                                M3U8DownloadTask.this.mHandler.sendEmptyMessage(1003);

                                M3U8DownloadTask.this.isRunning = false;
                            }

                        } catch (InterruptedIOException e) {

                            return;

                        } catch (IOException e) {

                            M3U8DownloadTask.this.handlerError(e);
                            return;

                        } catch (InterruptedException e) {

                            M3U8DownloadTask.this.handlerError(e);
                            return;

                        } catch (Exception e) {

                            M3U8DownloadTask.this.handlerError(e);
                        }
                    }

                }).start();
            }


            public void onStart() {

                M3U8DownloadTask.this.onTaskDownloadListener.onStart();
            }


            public void onError(Throwable errorMsg) {

                M3U8DownloadTask.this.handlerError(errorMsg);
            }
        });
    }


    private void startDownload(M3U8 m3U8) {

        final File dir = new File(this.saveDir);


        if (!dir.exists()) {

            dir.mkdirs();
        }

        this.totalTs = m3U8.getTsList().size();

        if (this.executor != null) {

            this.executor.shutdownNow();
        }











        M3U8Log.d("executor is shutDown ! Downloading !");


        this.curTs = 1;

        this.isRunning = true;

        this.isStartDownload = true;

        this.executor = null;


        this.executor = Executors.newFixedThreadPool(this.threadCount);

        final String basePath = m3U8.getBasePath();

        this.netSpeedTimer = new Timer();

        this.netSpeedTimer.schedule(new TimerTask() {
            public void run() {

                M3U8DownloadTask.this.onTaskDownloadListener.onProgress(M3U8DownloadTask.this.curLength);
            }
        }, 0L, 1500L);


        for (M3U8Ts m3U8Ts : m3U8.getTsList()) {

            this.executor.execute(new Runnable() {
                public void run() {
                    File file;
                    try {

                        String fileName = M3U8EncryptHelper.encryptFileName(M3U8DownloadTask.this.encryptKey, m3U8Ts.obtainEncodeTsFileName());

                        file = new File(dir + File.separator + fileName);

                    } catch (Exception e) {

                        file = new File(dir + File.separator + m3U8Ts.getUrl());
                    }


                    if (!file.exists()) {


                        FileOutputStream fos = null;

                        InputStream inputStream = null;
                        try {

                            URL url = new URL(m3U8Ts.obtainFullUrl(basePath));

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            conn.setConnectTimeout(M3U8DownloadTask.this.connTimeout);

                            conn.setReadTimeout(M3U8DownloadTask.this.readTimeout);

                            if (conn.getResponseCode() == 200) {

                                if (M3U8DownloadTask.this.isStartDownload) {

                                    M3U8DownloadTask.this.isStartDownload = false;

                                    M3U8DownloadTask.this.mHandler.sendEmptyMessage(1004);
                                }

                                inputStream = conn.getInputStream();

                                fos = new FileOutputStream(file);

                                int len = 0;

                                byte[] buf = new byte[8388608];

                                while ((len = inputStream.read(buf)) != -1) {

                                    M3U8DownloadTask.this.curLength = M3U8DownloadTask.this.curLength + len;

                                    fos.write(buf, 0, len);
                                }
                            } else {

                                M3U8DownloadTask.this.handlerError(new Throwable(String.valueOf(conn.getResponseCode())));
                            }

                        } catch (MalformedURLException e) {

                            M3U8DownloadTask.this.handlerError(e);

                        } catch (IOException e) {

                            M3U8DownloadTask.this.handlerError(e);

                        } catch (Exception e) {

                            M3U8DownloadTask.this.handlerError(e);
                        } finally {


                            if (inputStream != null) {
                                try {

                                    inputStream.close();

                                } catch (IOException iOException) {
                                }
                            }


                            if (fos != null) {
                                try {

                                    fos.close();

                                } catch (IOException iOException) {
                                }
                            }
                        }



                        M3U8DownloadTask.this.itemFileSize = file.length();
                        M3U8DownloadTask.this.tsFilePath = file.getAbsolutePath();

                        m3U8Ts.setFileSize(M3U8DownloadTask.this.itemFileSize);
                        m3U8Ts.setFilePath(M3U8DownloadTask.this.tsFilePath);
                        M3U8DownloadTask.this.mHandler.sendEmptyMessage(1002);

                        M3U8DownloadTask.this.curTs++;
                    } else {

                        M3U8DownloadTask.this.curTs++;

                        M3U8DownloadTask.this.itemFileSize = file.length();
                        M3U8DownloadTask.this.tsFilePath = file.getAbsolutePath();

                        m3U8Ts.setFileSize(M3U8DownloadTask.this.itemFileSize);
                        m3U8Ts.setFilePath(M3U8DownloadTask.this.tsFilePath);
                    }
                }
            });
        }
    }


    private void handlerError(Throwable e) {

        if (!"Task running".equals(e.getMessage())) {

            stop();
        }


        if ("thread interrupted".equals(e.getMessage())) {
            return;
        }

        Message msg = Message.obtain();

        msg.obj = e;

        msg.what = 1001;

        this.mHandler.sendMessage(msg);
    }


    public void stop() {

        if (this.netSpeedTimer != null) {

            this.netSpeedTimer.cancel();

            this.netSpeedTimer = null;
        }

        this.isRunning = false;

        if (this.executor != null) {

            this.executor.shutdownNow();
        }
    }

    public File getM3u8File(String url) {
        try {

            return new File(MUtils.getSaveFileDir(url), this.m3u8FileName);

        } catch (Exception e) {

            M3U8Log.e(e.getMessage());


            return null;
        }
    }
}


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\M3U8DownloadTask.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */