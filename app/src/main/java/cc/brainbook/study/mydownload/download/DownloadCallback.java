package cc.brainbook.study.mydownload.download;

public interface DownloadCallback {
    void onProgress(long progress, long total);
    void onComplete();
}
