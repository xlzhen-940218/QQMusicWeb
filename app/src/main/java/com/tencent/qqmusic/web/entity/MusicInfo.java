package com.tencent.qqmusic.web.entity;

public class MusicInfo {
    private String currentMusicUrl;
    private String musicName;
    private String musicSinger;
    private String musicIconUrl;
    private String currentLyric;
    private int totalProgress;
    private int currentProgress;
    private boolean playing;

    public String getCurrentLyric() {
        return currentLyric;
    }

    public void setCurrentLyric(String currentLyric) {
        this.currentLyric = currentLyric;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicSinger() {
        return musicSinger;
    }

    public void setMusicSinger(String musicSinger) {
        this.musicSinger = musicSinger;
    }

    public String getMusicIconUrl() {
        return musicIconUrl;
    }

    public void setMusicIconUrl(String musicIconUrl) {
        this.musicIconUrl = musicIconUrl;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int totalProgress) {
        this.totalProgress = totalProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public String getCurrentMusicUrl() {
        return currentMusicUrl;
    }

    public void setCurrentMusicUrl(String currentMusicUrl) {
        this.currentMusicUrl = currentMusicUrl;
    }
}
