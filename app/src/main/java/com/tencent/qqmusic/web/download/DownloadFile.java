package com.tencent.qqmusic.web.download;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class DownloadFile {
    public static void download(Context context,String downPathUrl,String filename, String inserType,OnFileDownListener listener) {
        Uri uri = null;
        int total = 0;
        int contentLength=0;
        try {
            URL url = new URL(downPathUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30 * 1000);
            InputStream is = conn.getInputStream();

            ContentValues contentValues = new ContentValues();
            if (inserType.equals(Environment.DIRECTORY_PICTURES)) {
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, getMIMEType(filename));
                contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                //只是往 MediaStore 里面插入一条新的记录，MediaStore 会返回给我们一个空的 Content Uri
                //接下来问题就转化为往这个 Content Uri 里面写入
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else if (inserType.equals(Environment.DIRECTORY_MOVIES)) {
                contentValues.put(MediaStore.Video.Media.MIME_TYPE, getMIMEType(filename));
                contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
                //只是往 MediaStore 里面插入一条新的记录，MediaStore 会返回给我们一个空的 Content Uri
                //接下来问题就转化为往这个 Content Uri 里面写入
                uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else if (inserType.equals((Environment.DIRECTORY_MUSIC))) {
                contentValues.put(MediaStore.Audio.Media.MIME_TYPE, getMIMEType(filename));
                contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, filename);
                if (Build.VERSION.SDK_INT>=29){//android 10
                    contentValues.put(MediaStore.Audio.Media.DATE_TAKEN, System.currentTimeMillis());
                }
                //只是往 MediaStore 里面插入一条新的记录，MediaStore 会返回给我们一个空的 Content Uri
                //接下来问题就转化为往这个 Content Uri 里面写入
                uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
            }
            BufferedInputStream inputStream = new BufferedInputStream(is);
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            if (os != null) {
                byte[] buffer = new byte[4096];
                int len;

                contentLength = conn.getContentLength();
                while ((len = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                    total += len;
                    if (listener != null) {
                        listener.onFileDownStatus(DownloadStatus.LOADING, uri, (total * 100 / contentLength), total, contentLength);
                    }
                }
            }

            //oppo手机不会出现在照片里面，但是会出现在图集里面
            if (inserType.equals(DIRECTORY_PICTURES)){//如果是图片
                //扫描到相册
                String[] filePathArray = getPathFromContentUri(uri,context);
                MediaScannerConnection.scanFile(context, new String[] {filePathArray[0]}, new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener(){
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                } );
            }
            os.flush();
            inputStream.close();
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listener != null) {
            listener.onFileDownStatus(DownloadStatus.LOADED, uri, 100, total, contentLength);
        }
    }

    /**
     * @date :2020/3/17 0017
     * @author : gaoxiaoxiong
     * @description:根据文件后缀名获得对应的MIME类型
     * @param fileName 文件名，需要包含后缀.xml类似这样的
     **/
    public static String getMIMEType(String fileName) {
        String type="*/*";
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        /* 获取文件的后缀名*/
        String end=fileName.substring(dotIndex,fileName.length()).toLowerCase();
        if(end=="")return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<getFileMiMeType().length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if(end.equals(getFileMiMeType()[i][0]))
                type = getFileMiMeType()[i][1];
        }
        return type;
    }

    /**
     * @date :2020/3/17 0017
     * @author : gaoxiaoxiong
     * @description:获取文件的mimetype类型
     **/
    public static String[][] getFileMiMeType() {
        String[][] MIME_MapTable = {
                //{后缀名，MIME类型}
                {".3gp", "video/3gpp"},
                {".apk", "application/vnd.android.package-archive"},
                {".asf", "video/x-ms-asf"},
                {".avi", "video/x-msvideo"},
                {".bin", "application/octet-stream"},
                {".bmp", "image/bmp"},
                {".c", "text/plain"},
                {".class", "application/octet-stream"},
                {".conf", "text/plain"},
                {".cpp", "text/plain"},
                {".doc", "application/msword"},
                {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
                {".xls", "application/vnd.ms-excel"},
                {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
                {".exe", "application/octet-stream"},
                {".gif", "image/gif"},
                {".gtar", "application/x-gtar"},
                {".gz", "application/x-gzip"},
                {".h", "text/plain"},
                {".htm", "text/html"},
                {".html", "text/html"},
                {".jar", "application/java-archive"},
                {".java", "text/plain"},
                {".jpeg", "image/jpeg"},
                {".jpg", "image/jpeg"},
                {".js", "application/x-javascript"},
                {".log", "text/plain"},
                {".m3u", "audio/x-mpegurl"},
                {".m4a", "audio/mp4a-latm"},
                {".m4b", "audio/mp4a-latm"},
                {".m4p", "audio/mp4a-latm"},
                {".m4u", "video/vnd.mpegurl"},
                {".m4v", "video/x-m4v"},
                {".mov", "video/quicktime"},
                {".mp2", "audio/x-mpeg"},
                {".mp3", "audio/x-mpeg"},
                {".mp4", "video/mp4"},
                {".mpc", "application/vnd.mpohun.certificate"},
                {".mpe", "video/mpeg"},
                {".mpeg", "video/mpeg"},
                {".mpg", "video/mpeg"},
                {".mpg4", "video/mp4"},
                {".mpga", "audio/mpeg"},
                {".msg", "application/vnd.ms-outlook"},
                {".ogg", "audio/ogg"},
                {".pdf", "application/pdf"},
                {".png", "image/png"},
                {".pps", "application/vnd.ms-powerpoint"},
                {".ppt", "application/vnd.ms-powerpoint"},
                {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
                {".prop", "text/plain"},
                {".rc", "text/plain"},
                {".rmvb", "audio/x-pn-realaudio"},
                {".rtf", "application/rtf"},
                {".sh", "text/plain"},
                {".tar", "application/x-tar"},
                {".tgz", "application/x-compressed"},
                {".txt", "text/plain"},
                {".wav", "audio/x-wav"},
                {".wma", "audio/x-ms-wma"},
                {".wmv", "audio/x-ms-wmv"},
                {".wps", "application/vnd.ms-works"},
                {".xml", "text/plain"},
                {".z", "application/x-compress"},
                {".zip", "application/x-zip-compressed"},
                {"", "*/*"}
        };
        return MIME_MapTable;
    }

    /**
     * @date 创建时间:2020/4/1 0001
     * @auther gaoxiaoxiong
     * @Descriptiion 通过Uri 获取 filePath  fileName
     **/
    @SuppressLint("Range")
    public static String[] getPathFromContentUri(Uri contentUri, Context context) {
        if (contentUri == null) {
            return null;
        }
        String filePath;
        String fileName;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        cursor.moveToFirst();
        filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
        fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
        cursor.close();
        String[] strings = new String[]{filePath,fileName};
        return strings;
    }
}
