package cc.brainbook.android.download.listener;

///\com\amazonaws\mobileconnectors\s3\transferutility\DownloadListener.java

import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.enumeration.DownloadState;

/**
 * Listener interface for download state and progress changes.
 * All callbacks will be invoked on the main thread.
 */
public interface DownloadListener {
    /**
     * 状态变化的事件
     *
     * @param fileInfo
     * @param state
     */
    void onStateChanged(FileInfo fileInfo, DownloadState state);

    /**
     * 进度的监听回调方法
     *
     * @param fileInfo              文件信息，比如用fileInfo.getFinishedTimeMillis()获取实时的耗时
     * @param diffTimeMillis        配合文件信息获取下载网速speed
     * @param diffFinishedBytes     配合文件信息获取下载进度progress
     */
    void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes);

    /**
     * 错误的事件
     *
     * @param fileInfo
     * @param e
     */
    void onError(FileInfo fileInfo, Exception e);
}
