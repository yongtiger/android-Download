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
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = DOWNLOAD_COMPLETE");

                ///设置下载完成时间
                mFileInfo.setEndTimeMillis(System.currentTimeMillis());

                ///设置下载速度为0
                if (mOnProgressListener != null) {
                    mFileInfo.setDiffTimeMillis(0);
                    mFileInfo.setDiffFinishedBytes(0);
                    mOnProgressListener.onProgress(mFileInfo);
                }

                ///下载完成回调接口DownloadCallback
                if (mDownloadCallback != null) {
                    mDownloadCallback.onComplete(mFileInfo);
                }

                break;
            case DOWNLOAD_PROGRESS:
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = DOWNLOAD_PROGRESS");

                ///下载进度回调接口DownloadCallback
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mFileInfo);
                }

                break;
        }
        super.handleMessage(msg);
    }
}
