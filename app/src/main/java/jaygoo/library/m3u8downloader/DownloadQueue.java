 package jaygoo.library.m3u8downloader;

 import java.util.ArrayList;
 import java.util.List;
 import jaygoo.library.m3u8downloader.bean.M3U8Task;













 class DownloadQueue
 {
   private List<M3U8Task> queue = new ArrayList<>();






   public void offer(M3U8Task task) {
     this.queue.add(task);
   }





   public M3U8Task poll() {
     try {
       if (this.queue.size() >= 2) {
         this.queue.remove(0);
         return this.queue.get(0);
       }  if (this.queue.size() == 1) {
         this.queue.remove(0);
       }
     } catch (Exception exception) {}

     return null;
   }





   public M3U8Task peek() {
     try {
       if (this.queue.size() >= 1) {
         return this.queue.get(0);
       }
     } catch (Exception exception) {}

     return null;
   }






   public boolean remove(M3U8Task task) {
     if (contains(task)) {
       return this.queue.remove(task);
     }
     return false;
   }






   public boolean contains(M3U8Task task) {
     return this.queue.contains(task);
   }






   public M3U8Task getTask(String url) {
     try {
       for (int i = 0; i < this.queue.size(); i++) {
         if (((M3U8Task)this.queue.get(i)).getUrl().equals(url)) {
           return this.queue.get(i);
         }
       }
     } catch (Exception exception) {}


     return null;
   }

   public boolean isEmpty() {
     return (size() == 0);
   }

   public int size() {
     return this.queue.size();
   }

   public boolean isHead(String url) {
     return isHead(new M3U8Task(url));
   }

   public boolean isHead(M3U8Task task) {
     return task.equals(peek());
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\DownloadQueue.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */