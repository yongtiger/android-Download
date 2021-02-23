package cc.brainbook.android.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.config.Config;
import cc.brainbook.android.download.enumeration.DownloadState;
import cc.brainbook.android.download.exception.DownloadException;
import cc.brainbook.android.download.listener.DownloadListener;
import cc.brainbook.android.download.util.Util;

import static cc.brainbook.android.download.BuildConfig.DEBUG;

/**
 * 下载任务类DownloadTask（使用Android原生HttpURLConnection）
 *
 *
 * 特点：
 *
 * 1）链式set方法设置
 *
 * 2）丰富的下载监听器参数
 * 如获取下载进度progress和下载网速speed，获取实时的下载耗时，也可实现分段详细显示下载进度条
 *
 * 3）使用Handler状态机方式，方便修改状态变化逻辑，比如：
 *      成功/错误状态时，可以停止下载进行清空下载文件等
 *      成功后可以清空后再重新下载
 *      。。。
 *
 * 4）消除了内存泄漏
 *
 *
 * 使用：
 * 1）创建下载任务类DownloadTask实例，并链式set方法设置参数
 * mDownloadTask = new DownloadTask(getApplicationContext());
 * 1.1）实例化DownloadTask时传入Context引用，方便操作（但要留意引起内存泄漏！）
 * 1.2）配置下载文件的网址（必选）
 * 可通过DownloadTask#setFileUrl(String fileUrl)设置
 * 1.3）配置下载文件名（可选）
 * 可通过DownloadTask#setFileName(String fileName)设置
 * 如果用户不配置，则尝试从下载连接connection中获得下载文件的文件名HttpDownloadUtil.getUrlFileName(HttpURLConnection connection)
 * 注意：考虑到下载文件网址中不一定含有文件名，所有不考虑从网址中获取！
 * 1.4）下载文件保存目录（可选）
 * 默认为应用的外部下载目录context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)，如果系统无SD卡返回应用的文件目录getFilesDir()
 * 参考：Util.getDefaultFilesDir(Context context)
 * 可通过DownloadTask#setSavePath(String savePath)设置
 *
 * 2）设置进度监听（可选）
 * fileInfo用于获取下载进度progress和下载网速speed
 *      ///避免除0异常
 *      int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
 *      long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
 * 也可用fileInfo.getFinishedTimeMillis()获取实时的下载耗时
 *
 * 3）下载事件接口DownloadListener（可选）
 *      void onStateChanged(FileInfo fileInfo, DownloadState state);
 *      void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes);
 *      void onError(FileInfo fileInfo, Exception e);
 *
 */
public class DownloadTask {
    private static final String TAG = "TAG";

    /**
     * 持有Activity的引用
     *
     * 注意：可能带来的内存泄漏问题！
     * 当Activity关闭后，而子线程仍继续运行，此时如果GC，因为子线程仍持有Activity的引用mContext，导致Activity无法被回收，就会发生内存泄漏！
     * 通用解决方式：在子线程设置停止标志（并且声明为volatile），Activity关闭时置位该标志使得子线程终止运行。
     *
     * https://blog.csdn.net/changlei_shennan/article/details/44039905
     */
    private Context mContext;
    private FileInfo mFileInfo;
    private Config mConfig = new Config();
    private DownloadHandler mHandler;
    private DownloadListener mDownloadListener;

    public DownloadTask(Context context) {
        mContext = context;
        mFileInfo = new FileInfo();
    }


    /* ------------ 链式配置 ----------- */
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
    public DownloadTask setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
        return this;
    }
    /* ------------ 链式配置 ----------- */


    /**
     * 开始下载
     */
    public void start() {
        if (DEBUG) Log.d(TAG, "DownloadTask# start()# mFileInfo.getState(): " + mFileInfo.getState());

        switch (mFileInfo.getState()) {
            case NEW:           ///创建下载任务对象（NEW）后开始下载start()
                ///执行下载过程
                innerStart();

                break;
            case STARTED:       ///下载开始（STARTED）后开始下载start()
                ///忽略
                break;
            case STOPPED:       ///下载停止（STOPPED）后开始下载start()
                ///执行下载过程
                innerStart();

                break;
            case SUCCEED:       ///下载成功（SUCCEED）后开始下载start()
                ///重置下载
                reset();

                ///执行下载过程
                innerStart();

                break;

            case FAILED:        ///下载失败（FAILED）后开始下载start()
                ///重置下载
                reset();

                ///执行下载过程
                innerStart();

                break;
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (DEBUG) Log.d(TAG, "DownloadTask# stop()# mFileInfo.getState(): " + mFileInfo.getState());

        switch (mFileInfo.getState()) {
            case NEW:           ///创建下载任务对象（NEW）后停止下载stop()
                ///忽略
                break;
            case STARTED:       ///下载开始（STARTED）后停止下载stop()
                ///更新文件信息的状态：下载停止
                ///注意：start/pause/stop尽量提早设置状态（所以不放在Handler中），避免短时间内连续点击造成的重复操作！
                mFileInfo.setState(DownloadState.STOPPED);

                break;
            case STOPPED:       ///下载停止（STOPPED）后停止下载stop()
                ///忽略
                break;
            case SUCCEED:       ///下载成功（SUCCEED）后停止下载stop()
                ///发送消息：下载停止
                mHandler.obtainMessage(DownloadHandler.MSG_STOPPED).sendToTarget();

                break;
            case FAILED:   ///下载失败（FAILED）后停止下载stop()
                ///发送消息：下载停止
                mHandler.obtainMessage(DownloadHandler.MSG_STOPPED).sendToTarget();

                break;
        }
    }

    private void innerStart() {
        ///更新文件信息的状态：下载开始
        ///注意：start/pause/stop尽量提早设置状态（所以不放在Handler中），避免短时间内连续点击造成的重复操作！
        mFileInfo.setState(DownloadState.STARTED);

        ///创建Handler对象
        ///注意：Handler对象不能在构造中创建！因为mDownloadEvent、mOnProgressListener参数尚未准备好
        if (mHandler == null) {
            mHandler = new DownloadHandler (
                    mConfig,
                    this,
                    mFileInfo,
                    mDownloadListener);

        }

        ///检查文件网址URL
        ///如果为null或空字符串则报错后退出
        if (TextUtils.isEmpty(mFileInfo.getFileUrl())) {
            ///发送消息：下载错误
            mHandler.obtainMessage(DownloadHandler.MSG_FAILED,
                    new DownloadException(DownloadException.EXCEPTION_FILE_URL_NULL, "The file url cannot be null."))
                    .sendToTarget();
            return;
        }

        ///检查下载文件保存目录
        ///如果为null或空字符串则获得缺省的下载目录，否则创建文件下载目录
        if (TextUtils.isEmpty(mFileInfo.getSavePath())) {
            mFileInfo.setSavePath(Util.getDefaultFilesDirPath(mContext));
        } else {
            if (!Util.mkdirs(mFileInfo.getSavePath())) {
                ///发送消息：下载失败
                mHandler.obtainMessage(DownloadHandler.MSG_FAILED,
                        new DownloadException(DownloadException.EXCEPTION_FILE_MKDIR_EXCEPTION,
                                mContext.getString(R.string.msg_the_file_cannot_be_deleted,  mFileInfo.getSavePath())))
                        .sendToTarget();
                return;
            } else if (!Util.isCanWrite(mFileInfo.getSavePath())) {
                ///发送消息：下载失败
                mHandler.obtainMessage(DownloadHandler.MSG_FAILED,
                        new DownloadException(DownloadException.EXCEPTION_FILE_WRITE_EXCEPTION,
                                mContext.getString(R.string.msg_the_file_save_path_is_not_writable,  mFileInfo.getSavePath())))
                        .sendToTarget();
                return;
            }
        }

        ///创建下载线程并启动
        new DownloadThread(mContext, mConfig, mFileInfo, mHandler).start();

        ///发送消息：下载开始
        mHandler.obtainMessage(DownloadHandler.MSG_STARTED).sendToTarget();
    }

    /**
     * 重置下载
     *
     * 删除数据库中下载文件的所有线程信息、删除下载文件、重置文件信息的已经完成的总耗时（毫秒）、总字节数、清空线程信息集合
     * 重置后可以重新设置各参数、初始化等操作
     */
    public void reset() {
        if (DEBUG) Log.d(TAG, "DownloadTask# reset()# ");

        ///删除下载文件
        new File(mFileInfo.getSavePath() + mFileInfo.getFileName()).delete();

        ///[修正下载完成（成功/失败/停止）后重新开始下载]
        ///重置文件信息的已经完成的总字节数、总耗时（毫秒）
        mFileInfo.setFinishedBytes(0);
        mFileInfo.setFinishedTimeMillis(0);

        ///更新文件信息的状态
        mFileInfo.setState(DownloadState.NEW);
    }

}
