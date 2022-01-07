package jaygoo.library.m3u8downloader;

import jaygoo.library.m3u8downloader.bean.M3U8;

interface OnTaskDownloadListener extends BaseListener {
  void onStartDownload(int paramInt1, int paramInt2);
  
  void onDownloading(long paramLong1, long paramLong2, int paramInt1, int paramInt2);
  
  void onSuccess(M3U8 paramM3U8);
  
  void onProgress(long paramLong);
  
  void onStart();
  
  void onError(Throwable paramThrowable);
}


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\OnTaskDownloadListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */