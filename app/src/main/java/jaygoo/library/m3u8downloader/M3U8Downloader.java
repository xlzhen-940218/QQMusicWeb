 package jaygoo.library.m3u8downloader;

 import android.text.TextUtils;
 import androidx.annotation.Nullable;
 import java.io.File;
 import java.util.List;
 import jaygoo.library.m3u8downloader.bean.M3U8;
 import jaygoo.library.m3u8downloader.bean.M3U8Task;
 import jaygoo.library.m3u8downloader.utils.M3U8Log;
 import jaygoo.library.m3u8downloader.utils.MUtils;


















 public class M3U8Downloader
 {
   private long currentTime;
   private M3U8Task currentM3U8Task;
   private DownloadQueue downLoadQueue = new DownloadQueue();
   private M3U8DownloadTask m3U8DownLoadTask = new M3U8DownloadTask();
   private OnM3U8DownloadListener onM3U8DownloadListener;

   private static class SingletonHolder {
     static M3U8Downloader instance = new M3U8Downloader();
   }

   public static M3U8Downloader getInstance() {
     return SingletonHolder.instance;
   }






   private boolean isQuicklyClick() {
     boolean result = false;
     if (System.currentTimeMillis() - this.currentTime <= 100L) {
       result = true;
       M3U8Log.d("is too quickly click!");
     }
     this.currentTime = System.currentTimeMillis();
     return result;
   }





   private void downloadNextTask() {
     startDownloadTask(this.downLoadQueue.poll());
   }

   private void pendingTask(M3U8Task task) {
     task.setState(-1);
     if (this.onM3U8DownloadListener != null) {
       this.onM3U8DownloadListener.onDownloadPending(task);
     }
   }








   public void download(String url) {
     if (TextUtils.isEmpty(url) || isQuicklyClick())
       return;  M3U8Task task = new M3U8Task(url);
     if (this.downLoadQueue.contains(task)) {
       task = this.downLoadQueue.getTask(url);
       if (task.getState() == 5 || task.getState() == 4) {
         startDownloadTask(task);
       } else {
         pause(url);
       }
     } else {
       this.downLoadQueue.offer(task);
       startDownloadTask(task);
     }
   }








   public void pause(String url) {
     if (TextUtils.isEmpty(url))
       return;  M3U8Task task = this.downLoadQueue.getTask(url);
     if (task != null) {
       task.setState(5);

       if (this.onM3U8DownloadListener != null) {
         this.onM3U8DownloadListener.onDownloadPause(task);
       }

       if (url.equals(this.currentM3U8Task.getUrl())) {
         this.m3U8DownLoadTask.stop();
         downloadNextTask();
       } else {
         this.downLoadQueue.remove(task);
       }
     }
   }





   public void pause(List<String> urls) {
     if (urls == null || urls.size() == 0)
       return;  boolean isCurrentTaskPause = false;
     for (String url : urls) {
       if (this.downLoadQueue.contains(new M3U8Task(url))) {
         M3U8Task task = this.downLoadQueue.getTask(url);
         if (task != null) {
           task.setState(5);
           if (this.onM3U8DownloadListener != null) {
             this.onM3U8DownloadListener.onDownloadPause(task);
           }
           if (task.equals(this.currentM3U8Task)) {
             this.m3U8DownLoadTask.stop();
             isCurrentTaskPause = true;
           }
           this.downLoadQueue.remove(task);
         }
       }
     }
     if (isCurrentTaskPause) startDownloadTask(this.downLoadQueue.peek());

   }





   public boolean checkM3U8IsExist(String url) {
     try {
       return this.m3U8DownLoadTask.getM3u8File(url).exists();
     } catch (Exception e) {
       M3U8Log.e(e.getMessage());

       return false;
     }
   }





   public String getM3U8Path(String url) {
     return this.m3U8DownLoadTask.getM3u8File(url).getPath();
   }

   public boolean isRunning() {
     return this.m3U8DownLoadTask.isRunning();
   }







   public boolean isCurrentTask(String url) {
     return (!TextUtils.isEmpty(url) && this.downLoadQueue
       .peek() != null && this.downLoadQueue
       .peek().getUrl().equals(url));
   }


   public void setOnM3U8DownloadListener(OnM3U8DownloadListener onM3U8DownloadListener) {
     this.onM3U8DownloadListener = onM3U8DownloadListener;
   }

   public void setEncryptKey(String encryptKey) {
     this.m3U8DownLoadTask.setEncryptKey(encryptKey);
   }

   public String getEncryptKey() {
     return this.m3U8DownLoadTask.getEncryptKey();
   }

   private void startDownloadTask(M3U8Task task) {
     if (task == null)
       return;  pendingTask(task);
     if (!this.downLoadQueue.isHead(task)) {
       M3U8Log.d("start download task, but task is running: " + task.getUrl());

       return;
     }
     if (task.getState() == 5) {
       M3U8Log.d("start download task, but task has pause: " + task.getUrl());
       return;
     }
     try {
       this.currentM3U8Task = task;
       M3U8Log.d("====== start downloading ===== " + task.getUrl());
       this.m3U8DownLoadTask.download(task.getUrl(), this.onTaskDownloadListener);
     } catch (Exception e) {
       M3U8Log.e("startDownloadTask Error:" + e.getMessage());
     }
   }





   public void cancel(String url) {
     pause(url);
   }





   public void cancel(List<String> urls) {
     pause(urls);
   }





   public void cancelAndDelete(final String url, @Nullable final OnDeleteTaskListener listener) {
     pause(url);
     if (listener != null) {
       listener.onStart();
     }
     (new Thread(new Runnable()
         {
           public void run() {
             boolean isDelete = MUtils.clearDir(new File(MUtils.getSaveFileDir(url)));
             if (listener != null) {
               if (isDelete) {
                 listener.onSuccess();
               } else {
                 listener.onFail();
               }
             }
           }
         })).start();
   }






   public void cancelAndDelete(final List<String> urls, @Nullable final OnDeleteTaskListener listener) {
     pause(urls);
     if (listener != null) {
       listener.onStart();
     }
     (new Thread(new Runnable()
         {
           public void run() {
             boolean isDelete = true;
             for (String url : urls) {
               isDelete = (isDelete && MUtils.clearDir(new File(MUtils.getSaveFileDir(url))));
             }
             if (listener != null) {
               if (isDelete) {
                 listener.onSuccess();
               } else {
                 listener.onFail();
               }
             }
           }
         })).start();
   }

   private OnTaskDownloadListener onTaskDownloadListener = new OnTaskDownloadListener()
     {
       private long lastLength;
       private float downloadProgress;

       public void onStartDownload(int totalTs, int curTs) {
         M3U8Log.d("onStartDownload: " + totalTs + "|" + curTs);

         M3U8Downloader.this.currentM3U8Task.setState(2);
         this.downloadProgress = 1.0F * curTs / totalTs;
       }


       public void onDownloading(long totalFileSize, long itemFileSize, int totalTs, int curTs) {
         if (!M3U8Downloader.this.m3U8DownLoadTask.isRunning())
           return;  M3U8Log.d("onDownloading: " + totalFileSize + "|" + itemFileSize + "|" + totalTs + "|" + curTs);

         this.downloadProgress = 1.0F * curTs / totalTs;

         if (M3U8Downloader.this.onM3U8DownloadListener != null) {
           M3U8Downloader.this.onM3U8DownloadListener.onDownloadItem(M3U8Downloader.this.currentM3U8Task, itemFileSize, totalTs, curTs);
         }
       }


       public void onSuccess(M3U8 m3U8) {
         M3U8Downloader.this.m3U8DownLoadTask.stop();
         M3U8Downloader.this.currentM3U8Task.setM3U8(m3U8);
         M3U8Downloader.this.currentM3U8Task.setState(3);
         if (M3U8Downloader.this.onM3U8DownloadListener != null) {
           M3U8Downloader.this.onM3U8DownloadListener.onDownloadSuccess(M3U8Downloader.this.currentM3U8Task);
         }
         M3U8Log.d("m3u8 Downloader onSuccess: " + m3U8);
         M3U8Downloader.this.downloadNextTask();
       }



       public void onProgress(long curLength) {
         if (curLength - this.lastLength > 0L) {
           M3U8Downloader.this.currentM3U8Task.setProgress(this.downloadProgress);
           M3U8Downloader.this.currentM3U8Task.setSpeed(curLength - this.lastLength);
           if (M3U8Downloader.this.onM3U8DownloadListener != null) {
             M3U8Downloader.this.onM3U8DownloadListener.onDownloadProgress(M3U8Downloader.this.currentM3U8Task);
           }
           this.lastLength = curLength;
         }
       }


       public void onStart() {
         M3U8Downloader.this.currentM3U8Task.setState(1);
         if (M3U8Downloader.this.onM3U8DownloadListener != null) {
           M3U8Downloader.this.onM3U8DownloadListener.onDownloadPrepare(M3U8Downloader.this.currentM3U8Task);
         }
         M3U8Log.d("onDownloadPrepare: " + M3U8Downloader.this.currentM3U8Task.getUrl());
       }


       public void onError(Throwable errorMsg) {
         if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")) {
           M3U8Downloader.this.currentM3U8Task.setState(6);
         } else {
           M3U8Downloader.this.currentM3U8Task.setState(4);
         }
         if (M3U8Downloader.this.onM3U8DownloadListener != null) {
           M3U8Downloader.this.onM3U8DownloadListener.onDownloadError(M3U8Downloader.this.currentM3U8Task, errorMsg);
         }
         M3U8Log.e("onError: " + errorMsg.getMessage());
         M3U8Downloader.this.downloadNextTask();
       }
     };

   private M3U8Downloader() {}
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\M3U8Downloader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */