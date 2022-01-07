 package jaygoo.library.m3u8downloader;

 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
 import androidx.annotation.VisibleForTesting;
 import java.lang.ref.WeakReference;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;

















 public class WeakHandler
 {
   private final Handler.Callback mCallback;
   private final ExecHandler mExec;
   private Lock mLock = new ReentrantLock(); @VisibleForTesting
   final ChainedRef mRunnables = new ChainedRef(this.mLock, null);










   public WeakHandler() {
     this.mCallback = null;
     this.mExec = new ExecHandler();
   }











   public WeakHandler(@Nullable Handler.Callback callback) {
     this.mCallback = callback;
     this.mExec = new ExecHandler(new WeakReference<>(callback));
   }






   public WeakHandler(@NonNull Looper looper) {
     this.mCallback = null;
     this.mExec = new ExecHandler(looper);
   }








   public WeakHandler(@NonNull Looper looper, @NonNull Handler.Callback callback) {
     this.mCallback = callback;
     this.mExec = new ExecHandler(looper, new WeakReference<>(callback));
   }












   public final boolean post(@NonNull Runnable r) {
     return this.mExec.post(wrapRunnable(r));
   }


















   public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
     return this.mExec.postAtTime(wrapRunnable(r), uptimeMillis);
   }




















   public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
     return this.mExec.postAtTime(wrapRunnable(r), token, uptimeMillis);
   }


















   public final boolean postDelayed(Runnable r, long delayMillis) {
     return this.mExec.postDelayed(wrapRunnable(r), delayMillis);
   }
















   public final boolean postAtFrontOfQueue(Runnable r) {
     return this.mExec.postAtFrontOfQueue(wrapRunnable(r));
   }




   public final void removeCallbacks(Runnable r) {
     WeakRunnable runnable = this.mRunnables.remove(r);
     if (runnable != null) {
       this.mExec.removeCallbacks(runnable);
     }
   }






   public final void removeCallbacks(Runnable r, Object token) {
     WeakRunnable runnable = this.mRunnables.remove(r);
     if (runnable != null) {
       this.mExec.removeCallbacks(runnable, token);
     }
   }










   public final boolean sendMessage(Message msg) {
     return this.mExec.sendMessage(msg);
   }








   public final boolean sendEmptyMessage(int what) {
     return this.mExec.sendEmptyMessage(what);
   }










   public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
     return this.mExec.sendEmptyMessageDelayed(what, delayMillis);
   }










   public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
     return this.mExec.sendEmptyMessageAtTime(what, uptimeMillis);
   }













   public final boolean sendMessageDelayed(Message msg, long delayMillis) {
     return this.mExec.sendMessageDelayed(msg, delayMillis);
   }



















   public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
     return this.mExec.sendMessageAtTime(msg, uptimeMillis);
   }













   public final boolean sendMessageAtFrontOfQueue(Message msg) {
     return this.mExec.sendMessageAtFrontOfQueue(msg);
   }





   public final void removeMessages(int what) {
     this.mExec.removeMessages(what);
   }






   public final void removeMessages(int what, Object object) {
     this.mExec.removeMessages(what, object);
   }






   public final void removeCallbacksAndMessages(Object token) {
     this.mExec.removeCallbacksAndMessages(token);
   }





   public final boolean hasMessages(int what) {
     return this.mExec.hasMessages(what);
   }





   public final boolean hasMessages(int what, Object object) {
     return this.mExec.hasMessages(what, object);
   }

   public final Looper getLooper() {
     return this.mExec.getLooper();
   }


   private WeakRunnable wrapRunnable(@NonNull Runnable r) {
     if (r == null) {
       throw new NullPointerException("Runnable can't be null");
     }
     ChainedRef hardRef = new ChainedRef(this.mLock, r);
     this.mRunnables.insertAfter(hardRef);
     return hardRef.wrapper;
   }

   private static class ExecHandler extends Handler {
     private final WeakReference<Handler.Callback> mCallback;

     ExecHandler() {
       this.mCallback = null;
     }

     ExecHandler(WeakReference<Handler.Callback> callback) {
       this.mCallback = callback;
     }

     ExecHandler(Looper looper) {
       super(looper);
       this.mCallback = null;
     }

     ExecHandler(Looper looper, WeakReference<Handler.Callback> callback) {
       super(looper);
       this.mCallback = callback;
     }


     public void handleMessage(@NonNull Message msg) {
       if (this.mCallback == null) {
         return;
       }
       Handler.Callback callback = this.mCallback.get();
       if (callback == null) {
         return;
       }
       callback.handleMessage(msg);
     }
   }

   static class WeakRunnable implements Runnable {
     private final WeakReference<Runnable> mDelegate;
     private final WeakReference<WeakHandler.ChainedRef> mReference;

     WeakRunnable(WeakReference<Runnable> delegate, WeakReference<WeakHandler.ChainedRef> reference) {
       this.mDelegate = delegate;
       this.mReference = reference;
     }


     public void run() {
       Runnable delegate = this.mDelegate.get();
       WeakHandler.ChainedRef reference = this.mReference.get();
       if (reference != null) {
         reference.remove();
       }
       if (delegate != null) {
         delegate.run();
       }
     }
   }

   static class ChainedRef
   {
     @Nullable
     ChainedRef next;
     @Nullable
     ChainedRef prev;
     @NonNull
     final Runnable runnable;
     @NonNull
     final WeakHandler.WeakRunnable wrapper;
     @NonNull
     Lock lock;

     public ChainedRef(@NonNull Lock lock, @NonNull Runnable r) {
       this.runnable = r;
       this.lock = lock;
       this.wrapper = new WeakHandler.WeakRunnable(new WeakReference<>(r), new WeakReference<>(this));
     }

     public WeakHandler.WeakRunnable remove() {
       this.lock.lock();
       try {
         if (this.prev != null) {
           this.prev.next = this.next;
         }
         if (this.next != null) {
           this.next.prev = this.prev;
         }
         this.prev = null;
         this.next = null;
       } finally {
         this.lock.unlock();
       }
       return this.wrapper;
     }

     public void insertAfter(@NonNull ChainedRef candidate) {
       this.lock.lock();
       try {
         if (this.next != null) {
           this.next.prev = candidate;
         }

         candidate.next = this.next;
         this.next = candidate;
         candidate.prev = this;
       } finally {
         this.lock.unlock();
       }
     }

     @Nullable
     public WeakHandler.WeakRunnable remove(Runnable obj) {
       this.lock.lock();
       try {
         ChainedRef curr = this.next;
         while (curr != null) {
           if (curr.runnable == obj) {
             return curr.remove();
           }
           curr = curr.next;
         }
       } finally {
         this.lock.unlock();
       }
       return null;
     }
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\WeakHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */