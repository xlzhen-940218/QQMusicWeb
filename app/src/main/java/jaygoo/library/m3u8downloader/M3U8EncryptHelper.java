 package jaygoo.library.m3u8downloader;

 import android.text.TextUtils;
 import java.io.File;
 import jaygoo.library.m3u8downloader.utils.AES128Utils;
 import jaygoo.library.m3u8downloader.utils.MUtils;















 public class M3U8EncryptHelper
 {
   public static void encryptFile(String key, String fileName) throws Exception {
     if (TextUtils.isEmpty(key))
       return;  byte[] bytes = AES128Utils.getAESEncode(key, MUtils.readFile(fileName));
     MUtils.saveFile(bytes, fileName);
   }

   public static void decryptFile(String key, String fileName) throws Exception {
     if (TextUtils.isEmpty(key))
       return;  byte[] bytes = AES128Utils.getAESDecode(key, MUtils.readFile(fileName));
     MUtils.saveFile(bytes, fileName);
   }


   public static String encryptFileName(String key, String str) throws Exception {
     if (TextUtils.isEmpty(key)) return str;
     str = AES128Utils.parseByte2HexStr(AES128Utils.getAESEncode(key, str));
     return str;
   }

   public static String decryptFileName(String key, String str) throws Exception {
     if (TextUtils.isEmpty(key)) return str;
     str = new String(AES128Utils.getAESDecode(key, AES128Utils.parseHexStr2Byte(str)));
     return str;
   }

   public static void encryptTsFilesName(String key, String dirPath) throws Exception {
     if (TextUtils.isEmpty(key))
       return;  File dirFile = new File(dirPath);
     if (dirFile.exists() && dirFile.isDirectory()) {
       File[] files = dirFile.listFiles();
       for (int i = 0; i < files.length; i++) {
         if (!files[i].getName().contains("m3u8")) {
           File renameFile = new File(dirPath, encryptFileName(key, files[i].getName()));
           files[i].renameTo(renameFile);
         }
       }
     }
   }

   public static void decryptTsFilesName(String key, String dirPath) throws Exception {
     if (TextUtils.isEmpty(key))
       return;  File dirFile = new File(dirPath);
     if (dirFile.exists() && dirFile.isDirectory()) {
       File[] files = dirFile.listFiles();
       for (int i = 0; i < files.length; i++) {
         if (!files[i].getName().contains("m3u8")) {
           File renameFile = new File(dirPath, decryptFileName(key, files[i].getName()));
           files[i].renameTo(renameFile);
         }
       }
     }
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\M3U8EncryptHelper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */