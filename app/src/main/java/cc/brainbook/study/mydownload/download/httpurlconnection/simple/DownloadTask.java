package cc.brainbook.study.mydownload.download.httpurlconnection.simple;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.config.Config;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.exception.DownloadException;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.handler.DownloadHandler;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.DownloadCallback;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.interfaces.OnProgressListener;

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

    private Handler mHandler;

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
    private DownloadCallback mDownloadCallback;
    public DownloadTask setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
        return this;
    }
    private OnProgressListener mOnProgressListener;
    public DownloadTask setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
        return this;
    }

    /**
     * 开始下载
     */
    public void start() {
        Log.d(TAG, "DownloadTask#start(): ");

        ///避免重复启动下载线程
        if (mFileInfo.getStatus() != FileInfo.FILE_STATUS_START) {
            mFileInfo.setStatus(FileInfo.FILE_STATUS_START);

            ///重置已经下载字节数
            mFileInfo.setFinishedBytes(0);

            ///检验参数
            if (TextUtils.isEmpty(mFileInfo.getFileUrl())) {
                throw new DownloadException(DownloadException.EXCEPTION_FILE_URL_NULL, "The file url cannot be null.");
            }
            if (TextUtils.isEmpty(mFileInfo.getSavePath())) {
                ///https://juejin.im/entry/5951d0096fb9a06bb8745f75
                File downloadDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir != null) {
                    mFileInfo.setSavePath(downloadDir.getAbsolutePath());
                } else {
                    mFileInfo.setSavePath(mContext.getFilesDir().getAbsolutePath());
                }
            } else {
                ///创建本地下载目录
                File dir = new File(mFileInfo.getSavePath());
                if (!dir.exists()) {
                    ///mkdir()和mkdirs()的区别：
                    ///mkdir()  创建此抽象路径名指定的目录。如果父目录不存在则创建不成功。
                    ///mkdirs() 创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
                    if (!dir.mkdirs()) {
                        throw new DownloadException(DownloadException.EXCEPTION_SAVE_PATH_MKDIR, "The save path cannot be made: " + mFileInfo.getSavePath());
                    }
                }
            }

            mHandler = new DownloadHandler(mFileInfo, mDownloadCallback, mOnProgressListener);

            DownloadThread downloadThread = new DownloadThread(mFileInfo, mConfig, mHandler, mOnProgressListener != null);
            downloadThread.start();
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        Log.d(TAG, "DownloadTask#stop(): ");
        if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_START) {
            mFileInfo.setStatus(FileInfo.FILE_STATUS_STOP);
        }
    }

}
