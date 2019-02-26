package cc.brainbook.study.mydownload.httpdownload;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cc.brainbook.study.mydownload.httpdownload.bean.FileInfo;
import cc.brainbook.study.mydownload.httpdownload.config.Config;
import cc.brainbook.study.mydownload.httpdownload.exception.DownloadException;
import cc.brainbook.study.mydownload.httpdownload.handler.DownloadHandler;
import cc.brainbook.study.mydownload.httpdownload.interfaces.DownloadEvent;
import cc.brainbook.study.mydownload.httpdownload.interfaces.OnProgressListener;
import cc.brainbook.study.mydownload.httpdownload.util.Util;

public class DownloadTask {
    private static final String TAG = "TAG";

    /**
     * 持有Activity的引用
     *
     * 注意：可能带来的内存泄漏问题！
     * 当Activity退出后，而子线程仍继续运行，此时如果GC，因为子线程仍持有Activity的引用mContext，导致Activity无法被回收，就会发生内存泄漏！
     * 通用解决方式：在子线程设置停止标志（并且声明为volatile），Activity退出时置位该标志使得子线程终止运行。
     *
     * https://blog.csdn.net/changlei_shennan/article/details/44039905
     */
    private Context mContext;

    private FileInfo mFileInfo;

    private Config mConfig = new Config();

    private DownloadHandler mHandler;

    public DownloadTask(Context context) {
        mContext = context;
        mFileInfo = new FileInfo();
    }

    public DownloadTask setFileUrl(String fileUrl) {
        mFileInfo.setFileUrl(fileUrl);
        return this;
    }
    public DownloadTask setFileName(String fileName) {
        mFileInfo.setFileName(fileName);
        return this;
    }
    public DownloadTask setSavePath(String savePath) {
        mFileInfo.setSavePath(savePath);
        return this;
    }
    public DownloadTask setConnectTimeout(int connectTimeout) {
        mConfig.connectTimeout = connectTimeout;
        return this;
    }
    public DownloadTask setBufferSize(int bufferSize) {
        mConfig.bufferSize = bufferSize;
        return this;
    }
    public DownloadTask setProgressInterval(int progressInterval) {
        mConfig.progressInterval = progressInterval;
        return this;
    }
    private OnProgressListener mOnProgressListener;
    public DownloadTask setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
        return this;
    }
    private DownloadEvent mDownloadEvent;
    public DownloadTask setDownloadEvent(DownloadEvent downloadEvent) {
        mDownloadEvent = downloadEvent;
        return this;
    }

    /**
     * 开始下载
     */
    public void start() {
        Log.d(TAG, "DownloadTask# start(): ");

        ///避免重复启动下载线程
        if (mFileInfo.getStatus() != FileInfo.FILE_STATUS_START) {
            ///更新下载文件状态：下载开始
            mFileInfo.setStatus(FileInfo.FILE_STATUS_START);

            ///重置已经下载字节数
            mFileInfo.setFinishedBytes(0);

            ///检验参数
            if (TextUtils.isEmpty(mFileInfo.getFileUrl())) {
                throw new DownloadException(DownloadException.EXCEPTION_FILE_URL_NULL, "The file url cannot be null.");
            }
            if (TextUtils.isEmpty(mFileInfo.getSavePath())) {
                mFileInfo.setSavePath(Util.getDefaultFilesDirPath(mContext));
            } else {
                if (!Util.mkdirs(mFileInfo.getSavePath())) {
                    throw new DownloadException(DownloadException.EXCEPTION_SAVE_PATH_MKDIR, "The save path cannot be made: " + mFileInfo.getSavePath());
                }
            }

            ///避免start时重复创建Handler对象
            if (mHandler == null) {
                mHandler = new DownloadHandler(mFileInfo, mDownloadEvent, mOnProgressListener);
            }

            new DownloadThread(mConfig, mFileInfo, mHandler, mOnProgressListener != null).start();
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        Log.d(TAG, "DownloadTask# stop(): ");
        if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_START) {
            ///更新下载文件状态：下载停止
            mFileInfo.setStatus(FileInfo.FILE_STATUS_STOP);

            ///删除下载文件
            new File(mFileInfo.getSavePath() + mFileInfo.getFileName()).delete();
        }
    }

}
