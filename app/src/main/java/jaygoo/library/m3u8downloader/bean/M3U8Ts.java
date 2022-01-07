package jaygoo.library.m3u8downloader.bean;

import androidx.annotation.NonNull;

import jaygoo.library.m3u8downloader.utils.MD5Utils;


public class M3U8Ts
        implements Comparable<M3U8Ts> {
    private String url;
    private String filePath;
    private long fileSize;
    private float seconds;

    public M3U8Ts(String url, float seconds) {
        this.url = url;
        this.seconds = seconds;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getSeconds() {
        return this.seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    public String obtainEncodeTsFileName() {
        if (this.url == null) return "error.ts";
        return MD5Utils.encode(this.url).concat(".ts");
    }

    public String obtainFullUrl(String hostUrl) {
        if (this.url == null) {
            return null;
        }
        if (this.url.startsWith("http"))
            return this.url;
        if (this.url.startsWith("//")) {
            return "http:".concat(this.url);
        }
        return hostUrl.concat(this.url);
    }


    public String toString() {
        return this.url + " (" + this.seconds + "sec)";
    }


    public long getLongDate() {
        try {
            return Long.parseLong(this.url.substring(0, this.url.lastIndexOf(".")));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }


    public int compareTo(@NonNull M3U8Ts o) {
        return this.url.compareTo(o.url);
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}


/* Location:              C:\Users\Xiong\Desktop\m3u8.jar!\jaygoo\library\m3u8downloader\bean\M3U8Ts.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */