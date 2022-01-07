 package jaygoo.library.m3u8downloader.utils;
 
 import android.util.Log;
 import jaygoo.library.m3u8downloader.M3U8DownloaderConfig;
 
 
 
 
 
 
 
 
 
 
 public class M3U8Log
 {
   private static String TAG = "M3U8Log";
   
   public static void d(String msg) {
     if (M3U8DownloaderConfig.isDebugMode()) Log.d(TAG, msg); 
   }
   
   public static void e(String msg) {
     if (M3U8DownloaderConfig.isDebugMode()) Log.e(TAG, msg); 
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloade\\utils\M3U8Log.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */