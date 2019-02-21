package cc.brainbook.study.mydownload.threadhandler.simple.interfaces;

import cc.brainbook.study.mydownload.threadhandler.simple.bean.FileInfo;

/**
 * 下载进度监听器
 */
public interface OnProgressListener {

    /**
     * 下载进度的监听回调方法
     *
     * @param fileInfo
     */
    void onProgress(FileInfo fileInfo);

}
