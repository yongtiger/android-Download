package cc.brainbook.android.download.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cc.brainbook.android.download.interfaces.DownloadEvent;
import cc.brainbook.android.download.interfaces.OnProgressListener;
import cc.brainbook.android.download.bean.FileInfo;

public class DownloadHandler extends Handler {
    private static final String TAG = "TAG";

    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_COMPLETE = 3;
    public static final int MSG_PROGRESS = 4;

    private FileInfo mFileInfo;
    private DownloadEvent mDownloadEvent;
    private OnProgressListener mOnProgressListener;

    public DownloadHandler(FileInfo mFileInfo,
                           DownloadEvent mDownloadEvent,
                           OnProgressListener mOnProgressListener) {
        this.mFileInfo = mFileInfo;
        this.mDownloadEvent = mDownloadEvent;
        this.mOnProgressListener = mOnProgressListener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START:
                Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_START");

                ///下载事件接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onStart(mFileInfo);
                }

                break;
            case MSG_STOP:
                Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_STOP");

                ///重置下载速度为0
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mFileInfo, 0, 0);
                }

                ///下载完成回调接口DownloadCallback
                if (mDownloadEvent != null) {
                    mDownloadEvent.onStop(mFileInfo);
                }

                break;
            case MSG_COMPLETE:
                Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_COMPLETE");

                ///重置下载速度为0
                if (mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mFileInfo, 0, 0);
                }

                ///下载完成回调接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onComplete(mFileInfo);
                }

                break;
            case MSG_PROGRESS:
                Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_PROGRESS");

                ///下载进度回调接口DownloadEvent
                if (mOnProgressListener != null) {
                    long diffTimeMillis = ((long[]) msg.obj)[0];
                    long diffFinishedBytes = ((long[]) msg.obj)[1];
                    mOnProgressListener.onProgress(mFileInfo, diffTimeMillis, diffFinishedBytes);
                }

                break;
        }
        super.handleMessage(msg);
    }

}
