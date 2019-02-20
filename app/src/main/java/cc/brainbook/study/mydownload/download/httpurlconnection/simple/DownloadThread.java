package cc.brainbook.study.mydownload.download.httpurlconnection.simple;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import android.os.Handler;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.config.Config;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.handler.DownloadHandler;
import cc.brainbook.study.mydownload.download.httpurlconnection.simple.util.Util;

public class DownloadThread extends Thread {
    private static final String TAG = "TAG";

    private FileInfo mFileInfo;
    private Config mConfig;
    private OnProgressListener mOnProgressListener;
    private Handler mHandler;

    public DownloadThread(FileInfo fileInfo, Config config, OnProgressListener onProgressListener, Handler handler) {
        this.mFileInfo = fileInfo;
        this.mConfig = config;
        this.mOnProgressListener = onProgressListener;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        super.run();

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;   ///文件输出流

        try {
            ///由下载文件的URL网址建立Http网络连接connection
            URL url = new URL(mFileInfo.getFileUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(mConfig.connectTimeout);
            connection.connect();

            ///如果网络连接connection的响应码为200，则开始下载过程
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                ///获得下载文件名
                if (mFileInfo.getFileName().isEmpty()) {
                    mFileInfo.setFileName(Util.getUrlFileName(connection));
                    if (mFileInfo.getFileName().isEmpty()) {
                        throw new DownloadException(DownloadException.EXCEPTION_FILE_NAME_NULL, "The file name cannot be null.");
                    }
                }

                ///获得网络连接connection的输入流对象
                inputStream = connection.getInputStream();
                ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
                ///https://blog.csdn.net/hfreeman2008/article/details/49174499
                bufferedInputStream = new BufferedInputStream(inputStream);

                ///创建文件输出流对象
                File saveFile = new File(mFileInfo.getSavePath(), mFileInfo.getFileName());
                fileOutputStream = new FileOutputStream(saveFile);
                FileChannel channel = fileOutputStream.getChannel();

                ///获得文件长度（建议用long类型，int类型最大为2GB）
                mFileInfo.setFileSize(connection.getContentLength());

                ///控制更新下载进度的周期
                long currentTimeMillis = System.currentTimeMillis();
                long currentFinishedBytes = mFileInfo.getFinishedBytes();

                ///输入流每次读取的内容（字节缓冲区）
                ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
                ///https://blog.csdn.net/xisuo002/article/details/78742631
                byte[] bytes = new byte[mConfig.bufferSize];
                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int readLength;
                while ((readLength = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    ///Wrap a byte array into a buffer
                    ByteBuffer buf = ByteBuffer.wrap(bytes, 0, readLength);
                    channel.write(buf);

                    mFileInfo.setFinishedBytes(mFileInfo.getFinishedBytes() + readLength);
                    Log.d(TAG, "DownloadTask#innerDownload(): thread name is: " + Thread.currentThread().getName());
                    Log.d(TAG, "DownloadTask#innerDownload()#finishedBytes: " + mFileInfo.getFinishedBytes() + ", fileSize: " + mFileInfo.getFileSize());

                    if (mOnProgressListener != null) {
                        ///控制更新下载进度的周期
                        if (System.currentTimeMillis() - currentTimeMillis > mConfig.progressInterval) {
                            mFileInfo.setDiffTimeMillis(System.currentTimeMillis() - currentTimeMillis);   ///下载进度的时间（毫秒）
                            currentTimeMillis = System.currentTimeMillis();
                            mFileInfo.setDiffFinishedBytes(mFileInfo.getFinishedBytes() - currentFinishedBytes);  ///下载进度的下载字节数
                            currentFinishedBytes = mFileInfo.getFinishedBytes();
                            ///发送消息：更新下载进度
                            mHandler.obtainMessage(DownloadHandler.DOWNLOAD_PROGRESS).sendToTarget();
                        }
                    }

                    ///停止下载线程
                    if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_STOP) {
                        Log.d(TAG, "DownloadTask#innerDownload()#mFileInfo.getStatus(): FILE_STATUS_STOP");
                        return;
                    }
                }

                ///下载完成时，设置进度为100、下载速度为0
                if (mOnProgressListener != null) {
                    mFileInfo.setDiffTimeMillis(1);   ///避免除0异常
                    mFileInfo.setDiffFinishedBytes(0);
                    ///发送消息：更新下载进度
                    mHandler.obtainMessage(DownloadHandler.DOWNLOAD_PROGRESS).sendToTarget();
                }

                mFileInfo.setStatus(FileInfo.FILE_STATUS_COMPLETE);

                ///发送消息：下载完成
                Log.d(TAG, "DownloadTask#innerDownload()#mHandler.obtainMessage(DOWNLOAD_COMPLETE, mFileInfo).sendToTarget();");
                ///因为要传递的不是单一数据类型，所以不能使用数组，只能用类FileInfo
                mHandler.obtainMessage(DownloadHandler.DOWNLOAD_COMPLETE).sendToTarget();

            } else {
                Log.d(TAG, "DownloadTask#innerDownload()#connection的响应码: " + connection.getResponseCode());
                throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "The connection response code is " + connection.getResponseCode());
            }
        } catch (MalformedURLException e) {
            ///当URL为null或无效网络连接协议时：java.net.MalformedURLException: Protocol not found
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_MALFORMED_URL, "The protocol is not found.", e);
        } catch (UnknownHostException e) {
            ///URL虽然以http://或https://开头、但host为空或无效host
            ///     java.net.UnknownHostException: http://
            ///     java.net.UnknownHostException: Unable to resolve host "aaa": No address associated with hostname
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_UNKNOWN_HOST, "The host is unknown.", e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_FILE_NOT_FOUND, "The file is not found.", e);
        } catch (IOException e) {
            ///当没有网络链接
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            ///关闭流Closeable
            Util.closeIO(fileOutputStream, bufferedInputStream, inputStream);
        }
    }
}
