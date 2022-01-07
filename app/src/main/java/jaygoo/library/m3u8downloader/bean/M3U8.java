 package jaygoo.library.m3u8downloader.bean;

 import java.util.ArrayList;
 import java.util.List;
 import jaygoo.library.m3u8downloader.utils.MUtils;












 public class M3U8
 {
   private String basePath;
   private String m3u8FilePath;
   private String dirFilePath;
   private long fileSize;
   private long totalTime;
   private List<M3U8Ts> tsList = new ArrayList<>();

   public String getBasePath() {
     return this.basePath;
   }

   public void setBasePath(String basePath) {
     this.basePath = basePath;
   }

   public String getM3u8FilePath() {
     return this.m3u8FilePath;
   }

   public void setM3u8FilePath(String m3u8FilePath) {
     this.m3u8FilePath = m3u8FilePath;
   }

   public String getDirFilePath() {
     return this.dirFilePath;
   }

   public void setDirFilePath(String dirFilePath) {
     this.dirFilePath = dirFilePath;
   }

   public long getFileSize() {
     this.fileSize = 0L;
     for (M3U8Ts m3U8Ts : this.tsList) {
       this.fileSize += m3U8Ts.getFileSize();
     }
     return this.fileSize;
   }

   public String getFormatFileSize() {
     this.fileSize = getFileSize();
     if (this.fileSize == 0L) return "";
     return MUtils.formatFileSize(this.fileSize);
   }

   public void setFileSize(long fileSize) {
     this.fileSize = fileSize;
   }

   public List<M3U8Ts> getTsList() {
     return this.tsList;
   }

   public void setTsList(List<M3U8Ts> tsList) {
     this.tsList = tsList;
   }

   public void addTs(M3U8Ts ts) {
     this.tsList.add(ts);
   }

   public long getTotalTime() {
     this.totalTime = 0L;
     for (M3U8Ts m3U8Ts : this.tsList) {
       this.totalTime += (int)(m3U8Ts.getSeconds() * 1000.0F);
     }
     return this.totalTime;
   }


   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("basePath: " + this.basePath);
     sb.append("\nm3u8FilePath: " + this.m3u8FilePath);
     sb.append("\ndirFilePath: " + this.dirFilePath);
     sb.append("\nfileSize: " + getFileSize());
     sb.append("\nfileFormatSize: " + MUtils.formatFileSize(this.fileSize));
     sb.append("\ntotalTime: " + this.totalTime);

     for (M3U8Ts ts : this.tsList) {
       sb.append("\nts: " + ts);
     }
     return sb.toString();
   }


   public boolean equals(Object obj) {
     if (obj instanceof M3U8) {
       M3U8 m3U8 = (M3U8)obj;
       if (this.basePath != null && this.basePath.equals(m3U8.basePath)) return true;
     }
     return false;
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\bean\M3U8.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */