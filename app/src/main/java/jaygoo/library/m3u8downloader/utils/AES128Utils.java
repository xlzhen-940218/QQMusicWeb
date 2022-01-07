 package jaygoo.library.m3u8downloader.utils;

 import javax.crypto.Cipher;
 import javax.crypto.KeyGenerator;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;













 public class AES128Utils
 {
   public static final String ENCODING = "UTF-8";

   public static String parseByte2HexStr(byte[] buf) {
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < buf.length; i++) {
       String hex = Integer.toHexString(buf[i] & 0xFF);
       if (hex.length() == 1) {
         hex = '0' + hex;
       }
       sb.append(hex.toUpperCase());
     }
     return sb.toString();
   }





   public static byte[] parseHexStr2Byte(String hexStr) {
     if (hexStr.length() < 1)
       return null;
     byte[] result = new byte[hexStr.length() / 2];
     for (int i = 0; i < hexStr.length() / 2; i++) {
       int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
       int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
       result[i] = (byte)(high * 16 + low);
     }
     return result;
   }







   public static String getAESKey() throws Exception {
     KeyGenerator kg = KeyGenerator.getInstance("AES");
     kg.init(128);
     SecretKey sk = kg.generateKey();
     byte[] b = sk.getEncoded();
     return parseByte2HexStr(b);
   }








   public static byte[] getAESEncode(String base64Key, String text) throws Exception {
     return getAESEncode(base64Key, text.getBytes());
   }








   public static byte[] getAESEncode(String base64Key, byte[] bytes) throws Exception {
     if (base64Key == null) return bytes;
     byte[] key = parseHexStr2Byte(base64Key);
     SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
     Cipher cipher = Cipher.getInstance("AES");
     cipher.init(1, sKeySpec);
     return cipher.doFinal(bytes);
   }








   public static byte[] getAESDecode(String base64Key, String text) throws Exception {
     return getAESDecode(base64Key, text.getBytes());
   }








   public static byte[] getAESDecode(String base64Key, byte[] bytes) throws Exception {
     if (base64Key == null) return bytes;
     byte[] key = parseHexStr2Byte(base64Key);
     SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");
     Cipher cipher = Cipher.getInstance("AES");
     cipher.init(2, sKeySpec);
     return cipher.doFinal(bytes);
   }
 }


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloade\\utils\AES128Utils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */