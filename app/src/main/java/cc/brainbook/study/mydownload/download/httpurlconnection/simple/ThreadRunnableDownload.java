package cc.brainbook.study.mydownload.download.httpurlconnection.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
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

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean.FileInfo;

public class ThreadRunnableDownload {
    private static final String TAG = "TAG";
    private static final int DOWNLOAD_COMPLETE = 1;
    private static final int DOWNLOAD_PROGRESS = 2;

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
    private static int fileId;

    public ThreadRunnableDownload(Context context) {
        mContext = context;
        mFileInfo = new FileInfo(fileId++);
    }


    /* ----------- [链式set方法设置] ----------- */
    public ThreadRunnableDownload setFileUrl(String fileUrl) {
        mFileInfo.setFileUrl(fileUrl);
        return this;
    }
    public ThreadRunnableDownload setFileName(String fileName) {
        mFileInfo.setFileName(fileName);
        return this;
    }
    public ThreadRunnableDownload setSavePath(String savePath) {
        mFileInfo.setSavePath(savePath);
        return this;
    }
    private int mConnectTimeout = 10000;    ///10秒
    public ThreadRunnableDownload setConnectTimeout(int connectTimeout) {
        mConnectTimeout = connectTimeout;
        return this;
    }
    ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
    ///https://blog.csdn.net/xisuo002/article/details/78742631
    private int mBufferSize = 1024; ///1k bytes
    public ThreadRunnableDownload setBufferSize(int bufferSize) {
        mBufferSize = bufferSize;
        return this;
    }
    private int mProgressInterval = 1000;   ///1秒
    public ThreadRunnableDownload setProgressInterval(int progressInterval) {
        mProgressInterval = progressInterval;
        return this;
    }
    private DownloadCallback mDownloadCallback;
    public ThreadRunnableDownload setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
        return this;
    }
    private OnProgressListener mOnProgressListener;
    public ThreadRunnableDownload setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
        return this;
    }


    /**
     * 开始下载
     */
    public void start() {
        Log.d(TAG, "ThreadRunnableDownload#start(): ");

        ///记录开始下载时间
        mFileInfo.setStartTimeMillis(System.currentTimeMillis());

        ///避免重复启动下载线程
        if (mFileInfo.getStatus() != FileInfo.FILE_STATUS_START) {
            mFileInfo.setStatus(FileInfo.FILE_STATUS_START);

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

            ///网络访问等耗时操作必须在子线程，否则阻塞主线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ///建议innerDownload()方法用参数传递的方式，而不建议直接引用类成员变量！符合解耦原则
                    innerDownload(mFileInfo, mConnectTimeout, mBufferSize, mProgressInterval);
                }
            }).start();
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        Log.d(TAG, "ThreadRunnableDownload#stop(): ");
        if (mFileInfo.getStatus() == FileInfo.FILE_STATUS_START) {
            mFileInfo.setStatus(FileInfo.FILE_STATUS_STOP);
        }
    }

    /**
     * 注意：This Handler class should be static or leaks might occur (anonymous android.os.Handler)
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    Log.d(TAG, "ThreadRunnableDownload#handleMessage(): msg.what = DOWNLOAD_COMPLETE");

                    ///设置下载完成时间
                    mFileInfo.setEndTimeMillis(System.currentTimeMillis());

                    ///下载完成回调接口DownloadCallback
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onComplete(mFileInfo);
                    }

                    break;
                case DOWNLOAD_PROGRESS:
                    Log.d(TAG, "ThreadRunnableDownload#handleMessage(): msg.what = DOWNLOAD_PROGRESS");

                    ///下载进度回调接口DownloadCallback
                    if (mOnProgressListener != null) {
                        ///获取已经下载完的字节数、下载文件的总字节数、下载进度的时间（毫秒）、下载进度的下载字节数
                        mOnProgressListener.onProgress(mFileInfo);
                    }

                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 内部下载过程
     *
     * @param fileInfo
     * @param connectTimeout
     * @param bufferSize
     * @param progressInterval
     */
    private void innerDownload(FileInfo fileInfo,
                               int connectTimeout,
                               int bufferSize,
                               int progressInterval) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;   ///文件输出流

        try {
            ///由下载文件的URL网址建立Http网络连接connection
            URL url = new URL(fileInfo.getFileUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.connect();

            ///如果网络连接connection的响应码为200，则开始下载过程
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                ///获得下载文件名
                if (fileInfo.getFileName().isEmpty()) {
                    fileInfo.setFileName(getUrlFileName(connection));
                    if (fileInfo.getFileName().isEmpty()) {
                        throw new DownloadException(DownloadException.EXCEPTION_FILE_NAME_NULL, "The file name cannot be null.");
                    }
                }

                ///获得网络连接connection的输入流对象
                inputStream = connection.getInputStream();
                ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
                ///https://blog.csdn.net/hfreeman2008/article/details/49174499
                bufferedInputStream = new BufferedInputStream(inputStream);

                ///创建文件输出流对象
                File saveFile = new File(fileInfo.getSavePath(), fileInfo.getFileName());
                fileOutputStream = new FileOutputStream(saveFile);
                FileChannel channel = fileOutputStream.getChannel();

                ///获得文件长度（建议用long类型，int类型最大为2GB）
                fileInfo.setFileSize(connection.getContentLength());

                ///控制更新下载进度的周期
                long currentTimeMillis = System.currentTimeMillis();
                long currentFinishedBytes = fileInfo.getFinishedBytes();

                ///输入流每次读取的内容（字节缓冲区）
                ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
                ///https://blog.csdn.net/xisuo002/article/details/78742631
                byte[] bytes = new byte[bufferSize];
                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int readLength;
                while ((readLength = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    ///Wrap a byte array into a buffer
                    ByteBuffer buf = ByteBuffer.wrap(bytes, 0, readLength);
                    channel.write(buf);

                    fileInfo.setFinishedBytes(fileInfo.getFinishedBytes() + readLength);
                    Log.d(TAG, "ThreadRunnableDownload#innerDownload(): thread name is: " + Thread.currentThread().getName());
                    Log.d(TAG, "ThreadRunnableDownload#innerDownload()#finishedBytes: " + fileInfo.getFinishedBytes() + ", fileSize: " + fileInfo.getFileSize());

                    if (mOnProgressListener != null) {
                        ///控制更新下载进度的周期
                        if (System.currentTimeMillis() - currentTimeMillis > progressInterval) {
                            fileInfo.setDiffTimeMillis(System.currentTimeMillis() - currentTimeMillis);   ///下载进度的时间（毫秒）
                            currentTimeMillis = System.currentTimeMillis();
                            fileInfo.setDiffFinishedBytes(fileInfo.getFinishedBytes() - currentFinishedBytes);  ///下载进度的下载字节数
                            currentFinishedBytes = fileInfo.getFinishedBytes();
                            ///发送消息：更新下载进度
                            mHandler.obtainMessage(DOWNLOAD_PROGRESS).sendToTarget();
                        }
                    }

                    ///停止下载线程
                    if (fileInfo.getStatus() == FileInfo.FILE_STATUS_STOP) {
                        Log.d(TAG, "ThreadRunnableDownload#innerDownload()#fileInfo.getStatus(): FILE_STATUS_STOP");
                        return;
                    }
                }

                ///下载完成时，设置进度为100、下载速度为0
                if (mOnProgressListener != null) {
                    fileInfo.setDiffTimeMillis(1);   ///避免除0异常
                    fileInfo.setDiffFinishedBytes(0);
                    ///发送消息：更新下载进度
                    mHandler.obtainMessage(DOWNLOAD_PROGRESS).sendToTarget();
                }

                fileInfo.setStatus(FileInfo.FILE_STATUS_COMPLETE);

                ///发送消息：下载完成
                Log.d(TAG, "ThreadRunnableDownload#innerDownload()#mHandler.obtainMessage(DOWNLOAD_COMPLETE, mFileInfo).sendToTarget();");
                ///因为要传递的不是单一数据类型，所以不能使用数组，只能用类FileInfo
                mHandler.obtainMessage(DOWNLOAD_COMPLETE).sendToTarget();

            } else {
                Log.d(TAG, "ThreadRunnableDownload#innerDownload()#connection的响应码: " + connection.getResponseCode());
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
            closeIO(fileOutputStream, bufferedInputStream, inputStream);
        }
    }

    private String getUrlFileName(HttpURLConnection connection) {
        String filename = "";
        String disposition = connection.getHeaderField("Content-Disposition");
        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                filename = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        }
        if (filename.length() == 0) {
            String path = connection.getURL().getPath();
            filename = new File(path).getName();
        }
        return filename;
    }

    ///关闭流Closeable
    private void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
