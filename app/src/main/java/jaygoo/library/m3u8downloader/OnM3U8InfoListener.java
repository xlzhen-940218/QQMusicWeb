package jaygoo.library.m3u8downloader;

import jaygoo.library.m3u8downloader.bean.M3U8;

public interface OnM3U8InfoListener extends BaseListener {
  void onStart();
  
  void onSuccess(M3U8 paramM3U8);
  
  void onError(Throwable paramThrowable);
}


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\OnM3U8InfoListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */