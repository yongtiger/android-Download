package cc.brainbook.study.mydownload.threadhandler.simple.interfaces;

import cc.brainbook.study.mydownload.threadhandler.simple.bean.FileInfo;

/**
 * 下载回调
 */
public interface DownloadCallback {

    /**
     * 下载开始的回调方法
     *
     * @param fileInfo
     */
    void onStart(FileInfo fileInfo);

    /**
     * 下载停止的回调方法
     *
     * @param fileInfo
     */
    void onStop(FileInfo fileInfo);

    /**
     * 下载完成的回调方法
     *
     * @param fileInfo
     */
    void onComplete(FileInfo fileInfo);

}
