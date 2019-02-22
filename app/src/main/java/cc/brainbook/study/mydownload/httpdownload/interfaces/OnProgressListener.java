package cc.brainbook.study.mydownload.httpdownload.interfaces;

import cc.brainbook.study.mydownload.httpdownload.bean.FileInfo;

/**
 * 下载进度监听器
 */
public interface OnProgressListener {
    /**
     * 下载进度的监听回调方法
     *
     * @param fileInfo
     * @param diffTimeMillis
     * @param diffFinishedBytes
     */
    void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes);

}
