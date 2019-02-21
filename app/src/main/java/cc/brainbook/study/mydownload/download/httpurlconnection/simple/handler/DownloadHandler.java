package cc.brainbook.study.mydownload.download.httpurlconnection.simple.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.DownloadCallback;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.OnProgressListener;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;

public class DownloadHandler extends Handler {
    private static final String TAG = "TAG";
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_COMPLETE = 3;
    public static final int MSG_PROGRESS = 4;

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
            case MSG_START:
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = MSG_START");

                ///设置下载开始时间
                mFileInfo.setStartTimeMillis(System.currentTimeMillis());

                ///下载完成回调接口DownloadCallback
                if (mDownloadCallback != null) {
                    mDownloadCallback.onStart(mFileInfo);
                }

                break;
            case MSG_STOP:
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = MSG_STOP");

                ///设置下载停止时间
                mFileInfo.setEndTimeMillis(System.currentTimeMillis());

                ///重置下载速度为0
                if (mOnProgressListener != null) {
                    mFileInfo.setDiffTimeMillis(0);
                    mFileInfo.setDiffFinishedBytes(0);
                    mOnProgressListener.onProgress(mFileInfo);
                }

                ///下载完成回调接口DownloadCallback
                if (mDownloadCallback != null) {
                    mDownloadCallback.onStop(mFileInfo);
                }

                break;
            case MSG_COMPLETE:
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = MSG_COMPLETE");

                ///设置下载完成时间
                mFileInfo.setEndTimeMillis(System.currentTimeMillis());

                ///重置下载速度为0
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
            case MSG_PROGRESS:
                Log.d(TAG, "DownloadHandler#handleMessage(): msg.what = MSG_PROGRESS");

                ///下载进度回调接口DownloadCallback
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mFileInfo);
                }

                break;
        }
        super.handleMessage(msg);
    }

}
