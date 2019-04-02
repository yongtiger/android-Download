package cc.brainbook.android.download;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.config.Config;
import cc.brainbook.android.download.enumeration.DownloadState;
import cc.brainbook.android.download.listener.DownloadListener;

import static cc.brainbook.android.download.BuildConfig.DEBUG;

public class DownloadHandler extends Handler {
    private static final String TAG = "TAG";

    static final int MSG_PROGRESS = 100;
    ///以下与DownloadState的状态对应
    static final int MSG_STARTED = 1;
    static final int MSG_STOPPED = 2;
    static final int MSG_SUCCEED = 3;
    static final int MSG_FAILED = 4;

    private Config mConfig;
    private DownloadTask mDownloadTask;
    private FileInfo mFileInfo;
    private DownloadListener mDownloadListener;

    DownloadHandler(Config config,
                    DownloadTask downloadTask,
                    FileInfo fileInfo,
                    DownloadListener downloadListener) {
        this.mConfig = config;
        this.mDownloadTask = downloadTask;
        this.mFileInfo = fileInfo;
        this.mDownloadListener = downloadListener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case MSG_PROGRESS:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage()# msg.what = MSG_PROGRESS");

                ///更新进度
                if (mDownloadListener != null) {
                    if (mFileInfo.getState() == DownloadState.STARTED) {
                        final long diffTimeMillis = ((long[]) msg.obj)[0];
                        final long diffFinishedBytes = ((long[]) msg.obj)[1];
                        mDownloadListener.onProgress(mFileInfo, diffTimeMillis, diffFinishedBytes);
                    } else {
                        ///修正进度更新显示的下载速度为0
                        mDownloadListener.onProgress(mFileInfo, 0, 0);
                    }
                }

                break;
            case MSG_STARTED:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage()# msg.what = MSG_STARTED");

                ///更改状态为下载开始（STARTED）
                changeStateToStarted();

                break;
            case MSG_STOPPED:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage()# msg.what = MSG_STOPPED");

                ///重置下载
                mDownloadTask.reset();

                ///更改状态为下载停止（STOPPED）
                changeStateToStopped();

                break;
            case MSG_SUCCEED:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage()# msg.what = MSG_SUCCEED");

                ///更改状态为下载成功（SUCCEED）
                changeStateToSucceed();

                break;
            case MSG_FAILED:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage()# msg.what = MSG_FAILED");

                ///错误的回调接口
                if (mDownloadListener != null) {
                    mDownloadListener.onError(mFileInfo, (Exception) msg.obj);
                }

                ///更改状态为下载失败（FAILED）
                changeStateToFailed();

                break;
        }
    }

    /**
     * 更改状态为下载开始（STARTED）
     */
    private void changeStateToStarted() {
        ///更新文件信息的状态：下载开始（STARTED）
        if (DEBUG) Log.d(TAG, "DownloadHandler# changeStateToStarted()# 更改状态为下载开始（STARTED）");
        mFileInfo.setState(DownloadState.STARTED);

        ///状态变化的回调接口：下载开始（STARTED）
        if (mDownloadListener != null) {
            mDownloadListener.onStateChanged(mFileInfo, DownloadState.STARTED);
        }
    }

    /**
     * 更改状态为下载停止（STOPPED）
     */
    private void changeStateToStopped() {
        ///更新文件信息的状态：下载停止（STOPPED）
        if (DEBUG) Log.d(TAG, "DownloadHandler# changeStateToStopped()# 更改状态为下载停止（STOPPED）");
        mFileInfo.setState(DownloadState.STOPPED);

        ///修正进度更新显示的下载速度为0
        if (mDownloadListener != null) {
            mDownloadListener.onProgress(mFileInfo, 0, 0);
        }

        ///状态变化的回调接口：下载停止（STOPPED）
        if (mDownloadListener != null) {
            mDownloadListener.onStateChanged(mFileInfo, DownloadState.STOPPED);
        }
    }

    /**
     * 更改状态为下载成功（SUCCEED）
     */
    private void changeStateToSucceed() {
        ///更新文件信息的状态：下载成功（SUCCEED）
        if (DEBUG) Log.d(TAG, "DownloadHandler# changeStateToSucceed()# 更改状态为下载成功（SUCCEED）");
        mFileInfo.setState(DownloadState.SUCCEED);

        ///修正进度更新显示的下载速度为0
        if (mDownloadListener != null) {
            mDownloadListener.onProgress(mFileInfo, 0, 0);
        }

        ///状态变化的回调接口：下载成功（SUCCEED）
        if (mDownloadListener != null) {
            mDownloadListener.onStateChanged(mFileInfo, DownloadState.SUCCEED);
        }
    }

    /**
     * 更改状态为下载失败（FAILED）
     */
    private void changeStateToFailed() {
        ///更新文件信息的状态：下载失败（FAILED）
        if (DEBUG) Log.d(TAG, "DownloadHandler# changeStateToInitFailed()# 更改状态为下载失败（FAILED）");
        mFileInfo.setState(DownloadState.FAILED);

        ///修正进度更新显示的下载速度为0
        if (mDownloadListener != null) {
            mDownloadListener.onProgress(mFileInfo,0, 0);
        }

        ///状态变化的回调接口：下载失败（FAILED）
        if (mDownloadListener != null) {
            mDownloadListener.onStateChanged(mFileInfo, DownloadState.FAILED);
        }
    }

}
