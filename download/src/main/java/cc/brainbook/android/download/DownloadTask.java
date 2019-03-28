package cc.brainbook.android.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.config.Config;
import cc.brainbook.android.download.exception.DownloadException;
import cc.brainbook.android.download.handler.DownloadHandler;
import cc.brainbook.android.download.interfaces.DownloadEvent;
import cc.brainbook.android.download.interfaces.OnProgressListener;
import cc.brainbook.android.download.util.Util;

import static cc.brainbook.android.download.BuildConfig.DEBUG;

/**
 * 下载任务类DownloadTask（使用Android原生HttpURLConnection）
 *
 * 注意：建议在Service中使用！如在Activity/Fragment中使用，则DownloadTask的生命周期与Activity/Fragment绑定，将随Activity/Fragment销毁而终止（如屏幕反转将导致终止下载任务）
 */
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
        if (DEBUG) Log.d(TAG, "DownloadTask# start()# ");

        ///避免start时重复创建Handler对象
        if (mHandler == null) {
            mHandler = new DownloadHandler(mFileInfo, mDownloadEvent, mOnProgressListener);
        }

        ///避免重复启动下载线程
        if (mFileInfo.getStatus() != FileInfo.FILE_STATUS_START) {
            ///更新下载文件状态：下载开始
            mFileInfo.setStatus(FileInfo.FILE_STATUS_START);

            ///重置已经下载字节数
            mFileInfo.setFinishedBytes(0);

            ///检验参数
            if (TextUtils.isEmpty(mFileInfo.getFileUrl())) {
                ///发送消息：下载错误
                mHandler.obtainMessage(DownloadHandler.MSG_ERROR,
                        new DownloadException(DownloadException.EXCEPTION_FILE_URL_NULL, "The file url cannot be null."))
                        .sendToTarget();
                return;
            }
            if (TextUtils.isEmpty(mFileInfo.getSavePath())) {
                mFileInfo.setSavePath(Util.getDefaultFilesDirPath(mContext));
            } else {
                if (!Util.mkdirs(mFileInfo.getSavePath())) {
                    ///发送消息：下载错误
                    mHandler.obtainMessage(DownloadHandler.MSG_ERROR,
                            new DownloadException(DownloadException.EXCEPTION_SAVE_PATH_MKDIR, "The file save path cannot be made: " + mFileInfo.getSavePath()))
                            .sendToTarget();
                    return;
                }
            }

            new DownloadThread(mConfig, mFileInfo, mHandler, mOnProgressListener != null).start();
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (DEBUG) Log.d(TAG, "DownloadTask# stop()# ");

        if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_START) {
            ///更新下载文件状态：下载停止
            mFileInfo.setStatus(FileInfo.FILE_STATUS_STOP);
        }
    }

}
