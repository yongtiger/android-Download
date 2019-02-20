package cc.brainbook.study.mydownload.download.httpurlconnection.simple.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.DownloadCallback;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.OnProgressListener;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;

public class DownloadHandler extends Handler {
    private static final String TAG = "TAG";
    public static final int DOWNLOAD_COMPLETE = 1;
    public static final int DOWNLOAD_PROGRESS = 2;

    private FileInfo mFileInfo;
    private DownloadCallback mDownloadCallback;
    private OnProgressListener mOnProgressListener;

    public DownloadHandler(FileInfo mFileInfo, DownloadCallback mDownloadCallback, OnProgressListener mOnProgressListener) {
        this.mFileInfo = mFileInfo;
        this.mDownloadCallback = mDownloadCallback;
        this.mOnProgressListener = mOnProgressListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case DOWNLOAD_COMPLETE:
                Log.d(TAG, "DownloadTask#handleMessage(): msg.what = DOWNLOAD_COMPLETE");

                ///设置下载完成时间
                mFileInfo.setEndTimeMillis(System.currentTimeMillis());

                ///下载完成回调接口DownloadCallback
                if (mDownloadCallback != null) {
                    mDownloadCallback.onComplete(mFileInfo);
                }

                break;
            case DOWNLOAD_PROGRESS:
                Log.d(TAG, "DownloadTask#handleMessage(): msg.what = DOWNLOAD_PROGRESS");

                ///下载进度回调接口DownloadCallback
                if (mOnProgressListener != null) {
                    ///获取已经下载完的字节数、下载文件的总字节数、下载进度的时间（毫秒）、下载进度的下载字节数
                    mOnProgressListener.onProgress(mFileInfo);
                }

                break;
        }
        super.handleMessage(msg);
    }
}
