 package jaygoo.library.m3u8downloader.bean;

 import jaygoo.library.m3u8downloader.utils.MUtils;









 public class M3U8Task
 {
   private String url;
   private int state = 0;
   private long speed;
   private float progress;
   private M3U8 m3U8;

   private M3U8Task() {}

   public M3U8Task(String url) {
     this.url = url;
   }


   public boolean equals(Object obj) {
     if (obj instanceof M3U8Task) {
       M3U8Task m3U8Task = (M3U8Task)obj;
       if (this.url != null && this.url.equals(m3U8Task.getUrl())) return true;
     }
     return false;
   }

   public String getFormatSpeed() {
     if (this.speed == 0L) return "";
     return MUtils.formatFileSize(this.speed) + "/s";
   }

   public String getFormatTotalSize() {
     if (this.m3U8 == null) return "";
     return this.m3U8.getFormatFileSize();
   }

   public float getProgress() {
     return this.progress;
   }

   public void setProgress(float progress) {
     this.progress = progress;
   }

   public String getUrl() {
     return this.url;
   }

   public void setUrl(String url) {
     this.url = url;
   }

   public int getState() {
     return this.state;
   }

   public void setState(int state) {
     this.state = state;
   }

   public long getSpeed() {
     return this.speed;
   }

   public void setSpeed(long speed) {
     this.speed = speed;
   }

   public M3U8 getM3U8() {
     return this.m3U8;
   }

   public void setM3U8(M3U8 m3U8) {
     this.m3U8 = m3U8;
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\bean\M3U8Task.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */