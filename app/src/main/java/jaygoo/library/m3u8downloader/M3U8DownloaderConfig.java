 package jaygoo.library.m3u8downloader;

 import android.content.Context;
 import android.os.Environment;
 import java.io.File;
 import jaygoo.library.m3u8downloader.utils.SPHelper;











 public class M3U8DownloaderConfig
 {
   private static final String TAG_SAVE_DIR = "TAG_SAVE_DIR_M3U8";
   private static final String TAG_THREAD_COUNT = "TAG_THREAD_COUNT_M3U8";
   private static final String TAG_CONN_TIMEOUT = "TAG_CONN_TIMEOUT_M3U8";
   private static final String TAG_READ_TIMEOUT = "TAG_READ_TIMEOUT_M3U8";
   private static final String TAG_DEBUG = "TAG_DEBUG_M3U8";

   public static M3U8DownloaderConfig build(Context context) {
     SPHelper.init(context);
     return new M3U8DownloaderConfig();
   }

   public M3U8DownloaderConfig setSaveDir(String saveDir) {
     SPHelper.putString("TAG_SAVE_DIR_M3U8", saveDir);
     return this;
   }

   public static String getSaveDir() {
     return SPHelper.getString("TAG_SAVE_DIR_M3U8", Environment.getExternalStorageDirectory().getPath() + File.separator + "M3u8Downloader");
   }

   public M3U8DownloaderConfig setThreadCount(int threadCount) {
     if (threadCount > 5) threadCount = 5;
     if (threadCount <= 0) threadCount = 1;
     SPHelper.putInt("TAG_THREAD_COUNT_M3U8", threadCount);
     return this;
   }

   public static int getThreadCount() {
     return SPHelper.getInt("TAG_THREAD_COUNT_M3U8", 3);
   }

   public M3U8DownloaderConfig setConnTimeout(int connTimeout) {
     SPHelper.putInt("TAG_CONN_TIMEOUT_M3U8", connTimeout);
     return this;
   }

   public static int getConnTimeout() {
     return SPHelper.getInt("TAG_CONN_TIMEOUT_M3U8", 10000);
   }

   public M3U8DownloaderConfig setReadTimeout(int readTimeout) {
     SPHelper.putInt("TAG_READ_TIMEOUT_M3U8", readTimeout);
     return this;
   }

   public static int getReadTimeout() {
     return SPHelper.getInt("TAG_READ_TIMEOUT_M3U8", 1800000);
   }


   public M3U8DownloaderConfig setDebugMode(boolean debug) {
     SPHelper.putBoolean("TAG_DEBUG_M3U8", debug);
     return this;
   }

   public static boolean isDebugMode() {
     return SPHelper.getBoolean("TAG_DEBUG_M3U8", false);
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\M3U8DownloaderConfig.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */