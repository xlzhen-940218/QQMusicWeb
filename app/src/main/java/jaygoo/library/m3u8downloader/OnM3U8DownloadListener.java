package jaygoo.library.m3u8downloader;

import jaygoo.library.m3u8downloader.bean.M3U8Task;

public abstract class OnM3U8DownloadListener {
  public void onDownloadItem(M3U8Task task, long itemFileSize, int totalTs, int curTs) {}
  
  public void onDownloadSuccess(M3U8Task task) {}
  
  public void onDownloadPause(M3U8Task task) {}
  
  public void onDownloadPending(M3U8Task task) {}
  
  public void onDownloadProgress(M3U8Task task) {}
  
  public void onDownloadPrepare(M3U8Task task) {}
  
  public void onDownloadError(M3U8Task task, Throwable errorMsg) {}
}


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\OnM3U8DownloadListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */