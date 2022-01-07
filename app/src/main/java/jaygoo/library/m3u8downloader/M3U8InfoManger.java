 package jaygoo.library.m3u8downloader;

 import java.io.IOException;
 import jaygoo.library.m3u8downloader.bean.M3U8;
 import jaygoo.library.m3u8downloader.utils.MUtils;













 public class M3U8InfoManger
 {
   private static M3U8InfoManger mM3U8InfoManger;
   private OnM3U8InfoListener onM3U8InfoListener;

   public static M3U8InfoManger getInstance() {
     synchronized (M3U8InfoManger.class) {
       if (mM3U8InfoManger == null) {
         mM3U8InfoManger = new M3U8InfoManger();
       }
     }
     return mM3U8InfoManger;
   }







   public synchronized void getM3U8Info(final String url, OnM3U8InfoListener onM3U8InfoListener) {
     this.onM3U8InfoListener = onM3U8InfoListener;
     onM3U8InfoListener.onStart();
     (new Thread()
       {
         public void run() {
           try {
             M3U8 m3u8 = MUtils.parseIndex(url);
             M3U8InfoManger.this.handlerSuccess(m3u8);
           } catch (IOException e) {
             M3U8InfoManger.this.handlerError(e);
           }
         }
       }).start();
   }







   private void handlerError(Throwable e) {
     this.onM3U8InfoListener.onError(e);
   }






   private void handlerSuccess(M3U8 m3u8) {
     this.onM3U8InfoListener.onSuccess(m3u8);
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\M3U8InfoManger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */