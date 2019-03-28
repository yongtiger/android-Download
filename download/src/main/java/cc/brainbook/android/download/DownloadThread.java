package cc.brainbook.android.download;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;

import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.config.Config;
import cc.brainbook.android.download.exception.DownloadException;
import cc.brainbook.android.download.handler.DownloadHandler;
import cc.brainbook.android.download.util.HttpDownloadUtil;
import cc.brainbook.android.download.util.Util;

import static cc.brainbook.android.download.BuildConfig.DEBUG;

public class DownloadThread extends Thread {
    private static final String TAG = "TAG";

    private Config mConfig;
    private FileInfo mFileInfo;
    private DownloadHandler mHandler;
    private boolean mHasOnProgressListener;

    public DownloadThread(Config config,
                          FileInfo fileInfo,
                          DownloadHandler handler,
                          boolean hasOnProgressListener) {
        this.mConfig = config;
        this.mFileInfo = fileInfo;
        this.mHandler = handler;
        this.mHasOnProgressListener = hasOnProgressListener;
    }

    @Override
    public void run() {
        super.run();

        HttpURLConnection connection = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel channel = null;

        try {
            ///由下载文件的URL网址建立网络连接
            connection = HttpDownloadUtil.openConnection(mFileInfo.getFileUrl(), mConfig.connectTimeout);

            ///发起网络连接
            HttpDownloadUtil.connect(connection);

            ///处理网络连接的响应码，如果网络连接connection的响应码为200，则开始下载过程，否则抛出异常
            HttpDownloadUtil.handleResponseCode(connection, HttpURLConnection.HTTP_OK);

            ///获得网络连接的缓冲输入流对象BufferedInputStream
            bufferedInputStream = HttpDownloadUtil.getBufferedInputStream(connection);

            ///由网络连接获得文件名
            if (TextUtils.isEmpty(mFileInfo.getFileName())) {
                mFileInfo.setFileName(HttpDownloadUtil.getUrlFileName(connection));
            }
            ///由网络连接获得文件长度（建议用long类型，int类型最大为2GB）
            mFileInfo.setFileSize(connection.getContentLength());

            ///获得保存文件
            File saveFile = new File(mFileInfo.getSavePath(), mFileInfo.getFileName());

            ///如果保存文件存在则删除
            if (saveFile.exists()) {
                if (!saveFile.delete()) {
                    throw new DownloadException(DownloadException.EXCEPTION_FILE_DELETE_EXCEPTION, "The file cannot be deleted: " + saveFile);
                }
            }

            ///获得保存文件的输出流对象
            fileOutputStream = HttpDownloadUtil.getFileOutputStream(saveFile);
            ///由文件的输出流对象获得FileChannel对象
            channel = fileOutputStream.getChannel();

            ///发送消息：下载开始
            if (DEBUG) Log.d(TAG, "DownloadThread# run()# ------- 发送消息：下载开始 -------");
            mHandler.obtainMessage(DownloadHandler.MSG_START).sendToTarget();

            ///设置下载开始时间
            long startTimeMillis = System.currentTimeMillis();
            ///控制更新下载进度的周期
            long currentTimeMillis = startTimeMillis;
            long currentFinishedBytes = 0;

            ///输入流每次读取的内容（字节缓冲区）
            ///BufferedInputStream的默认缓冲区大小是8192字节。
            ///当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
            ///https://blog.csdn.net/xisuo002/article/details/78742631
            byte[] bytes = new byte[mConfig.bufferSize];
            ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
            int readLength;
            while ((readLength = HttpDownloadUtil.bufferedInputStreamRead(bufferedInputStream, bytes)) != -1) {
                ///写入字节缓冲区内容到文件输出流
                HttpDownloadUtil.channelWrite(channel, bytes, readLength);

                ///更新已经下载完的总耗时（毫秒）
                mFileInfo.setFinishedTimeMillis(System.currentTimeMillis() - startTimeMillis);
                ///更新已经下载完的总字节数
                mFileInfo.setFinishedBytes(mFileInfo.getFinishedBytes() + readLength);
                if (DEBUG)
                    Log.d(TAG, "DownloadThread# run()# thread name: " + Thread.currentThread().getName() +
                            ", finishedBytes: " + mFileInfo.getFinishedBytes() +
                            ", fileSize: " + mFileInfo.getFileSize() +
                            ", finishedTimeMillis: " + mFileInfo.getFinishedTimeMillis());

                if (mHasOnProgressListener) {
                    ///控制更新下载进度的周期
                    if (System.currentTimeMillis() - currentTimeMillis > mConfig.progressInterval) {
                        ///发送消息：更新下载进度
                        if (DEBUG) Log.d(TAG, "DownloadThread# run()# 发送消息：更新下载进度");
                        long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;   ///下载进度的耗时（毫秒）
                        currentTimeMillis = System.currentTimeMillis();
                        long diffFinishedBytes = mFileInfo.getFinishedBytes() - currentFinishedBytes;  ///下载进度的下载字节数
                        currentFinishedBytes = mFileInfo.getFinishedBytes();
                        mHandler.obtainMessage(DownloadHandler.MSG_PROGRESS, new long[]{diffTimeMillis, diffFinishedBytes}).sendToTarget();
                    }
                }

                ///停止下载线程
                if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_STOP) {
                    ///发送消息：下载停止
                    if (DEBUG) Log.d(TAG, "DownloadThread# run()# ------- 发送消息：下载停止 -------");
                    mHandler.obtainMessage(DownloadHandler.MSG_STOP).sendToTarget();

                    return;
                }
            }

            ///更新下载文件状态：下载完成
            mFileInfo.setStatus(FileInfo.FILE_STATUS_COMPLETE);

            ///发送消息：下载完成
            if (DEBUG) Log.d(TAG, "DownloadThread# run()# ------- 发送消息：下载完成 -------");
            mHandler.obtainMessage(DownloadHandler.MSG_COMPLETE).sendToTarget();

        } catch (DownloadException downloadException) {
            ///更新下载文件状态：下载错误
            mFileInfo.setStatus(FileInfo.FILE_STATUS_ERROR);
            mHandler.obtainMessage(DownloadHandler.MSG_ERROR, downloadException).sendToTarget();
        } finally {
            ///关闭连接
            if (connection != null) {
                connection.disconnect();
            }

            ///关闭流Closeable
            Util.closeIO(bufferedInputStream, bufferedInputStream, fileOutputStream, channel);
        }

    }

}
