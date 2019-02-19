package cc.brainbook.study.mydownload.download;

public interface DownloadCallback {
    /**
     * 下载进度的回调方法
     *
     * @param finishedBytes         已经下载完的字节数
     * @param totalBytes            下载文件的总字节数
     * @param diffTimeMillis        下载进度的时间（毫秒）
     * @param diffFinishedBytes     下载进度的下载字节数
     */
    void onProgress(long finishedBytes, long totalBytes, long diffTimeMillis, long diffFinishedBytes);

    /**
     * 下载完成的回调方法
     */
    void onComplete();
}
