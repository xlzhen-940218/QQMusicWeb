 package jaygoo.library.m3u8downloader.utils;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 
 
 
 
 
 
 
 
 
 
 public class MD5Utils
 {
   public static String encode(String str) {
     try {
       MessageDigest md = MessageDigest.getInstance("MD5");
       
       md.update(str.getBytes());
 
       
       return (new BigInteger(1, md.digest())).toString(16);
     } catch (Exception e) {
       e.printStackTrace();
       
       return str;
     } 
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloade\\utils\MD5Utils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */