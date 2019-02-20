package cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;

/**
 * 下载回调
 */
public interface DownloadCallback {

    /**
     * 下载完成的回调方法
     *
     * @param fileInfo
     */
    void onComplete(FileInfo fileInfo);

}
