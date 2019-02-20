package cc.brainbook.study.mydownload.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

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

public class HttpURLConnectionDownload {
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
    private volatile boolean isStarted = false;

    public HttpURLConnectionDownload(Context context) {
        mContext = context;
    }


    /* ----------- [链式set方法设置] ----------- */
    private String mFileUrl;
    public HttpURLConnectionDownload setFileUrl(String fileUrl) {
        mFileUrl = fileUrl;
        return this;
    }
    private String mFileName;
    public HttpURLConnectionDownload setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }
    private String mSavePath;
//    private File mSavePath = Environment.getDownloadCacheDirectory();
    public HttpURLConnectionDownload setSavePath(String savePath) {
        mSavePath = savePath;
        return this;
    }
    private int mConnectTimeout = 10000;    ///10秒
    public HttpURLConnectionDownload setConnectTimeout(int connectTimeout) {
        mConnectTimeout = connectTimeout;
        return this;
    }
    ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
    ///https://blog.csdn.net/xisuo002/article/details/78742631
    private int mBufferSize = 1024; ///1k bytes
    public HttpURLConnectionDownload setBufferSize(int bufferSize) {
        mBufferSize = bufferSize;
        return this;
    }
    private int mProgressInterval = 1000;   ///1秒
    public HttpURLConnectionDownload setProgressInterval(int progressInterval) {
        mProgressInterval = progressInterval;
        return this;
    }
    private DownloadCallback mDownloadCallback;
    public HttpURLConnectionDownload setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
        return this;
    }

    /**
     * 开始下载
     */
    public void start() {
        Log.d(TAG, "HttpURLConnectionDownload#start(): ");
        ///避免重复启动下载线程
        if (!isStarted) {
            isStarted = true;

            ///检验参数
            if (TextUtils.isEmpty(mFileUrl)) {
                throw new DownloadException(DownloadException.EXCEPTION_FILE_URL_NULL, "The file url cannot be null.");
            }
            if (TextUtils.isEmpty(mSavePath)) {
                ///https://juejin.im/entry/5951d0096fb9a06bb8745f75
                File downloadDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir != null) {
                    mSavePath = downloadDir.getAbsolutePath();
                } else {
                    mSavePath = mContext.getFilesDir().getAbsolutePath();
                }
            } else {
                ///创建本地下载目录
                File dir = new File(mSavePath);
                if (!dir.exists()) {
                    ///mkdir()和mkdirs()的区别：
                    ///mkdir()  创建此抽象路径名指定的目录。如果父目录不存在则创建不成功。
                    ///mkdirs() 创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
                    if (!dir.mkdirs()) {
                        throw new DownloadException(DownloadException.EXCEPTION_SAVE_PATH_MKDIR, "The save path cannot be made: " + mSavePath);
                    }
                }
            }

            ///网络访问等耗时操作必须在子线程，否则阻塞主线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ///建议innerDownload()方法用参数传递的方式，而不建议直接引用类成员变量！符合解耦原则
                    innerDownload(mFileUrl, mFileName, mSavePath, mConnectTimeout, mBufferSize, mProgressInterval);
                }
            }).start();
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        Log.d(TAG, "HttpURLConnectionDownload#stop(): ");
        if (isStarted) {
            isStarted = false;
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
                    Log.d(TAG, "HttpURLConnectionDownload#handleMessage(): msg.what = DOWNLOAD_COMPLETE");

                    ///下载完成回调接口DownloadCallback
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onComplete();
                    }

                    break;
                case DOWNLOAD_PROGRESS:
                    Log.d(TAG, "HttpURLConnectionDownload#handleMessage(): msg.what = DOWNLOAD_PROGRESS");

                    ///下载进度回调接口DownloadCallback
                    if (mDownloadCallback != null) {
                        ///获取已经下载完的字节数、下载文件的总字节数、下载进度的时间（毫秒）、下载进度的下载字节数
                        long[] l = (long[]) msg.obj;
                        mDownloadCallback.onProgress(l[0], l[1], l[2], l[3]);
                    }

                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 内部下载过程
     *
     * @param fileUrl
     * @param fileName
     * @param savePath
     * @param connectTimeout
     * @param bufferSize
     * @param progressInterval
     */
    private void innerDownload(@NotNull String fileUrl,
                               String fileName,
                               String savePath,
                               int connectTimeout,
                               int bufferSize,
                               int progressInterval) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;   ///文件输出流

        try {
            ///由下载文件的URL网址建立Http网络连接connection
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.connect();

            ///如果网络连接connection的响应码为200，则开始下载过程
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                ///获得下载文件名
                if (fileName == null || fileName.length() == 0) {
                    fileName = getUrlFileName(connection);
                    if (fileName.length() == 0) {
                        throw new DownloadException(DownloadException.EXCEPTION_FILE_NAME_NULL, "The file name cannot be null.");
                    }
                }

                ///获得网络连接connection的输入流对象
                inputStream = connection.getInputStream();
                ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
                ///https://blog.csdn.net/hfreeman2008/article/details/49174499
                bufferedInputStream = new BufferedInputStream(inputStream);

                ///创建文件输出流对象
                File saveFile = new File(savePath, fileName);
                fileOutputStream = new FileOutputStream(saveFile);
                FileChannel channel = fileOutputStream.getChannel();

                ///获得文件长度（建议用long类型，int类型最大为2GB）
                long totalBytes = connection.getContentLength();
                ///已经下载完的字节数
                long finishedBytes = 0;
                ///控制更新下载进度的周期
                long currentTimeMillis = System.currentTimeMillis();
                long currentFinishedBytes = 0;

                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int length;
                ///输入流每次读取的内容（字节缓冲区）
                ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
                ///https://blog.csdn.net/xisuo002/article/details/78742631
                byte[] bytes = new byte[bufferSize];
                while ((length = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    ///Wrap a byte array into a buffer
                    ByteBuffer buf = ByteBuffer.wrap(bytes, 0, length);
                    channel.write(buf);
                    finishedBytes += length;
                    Log.d(TAG, "HttpURLConnectionDownload#innerDownload(): thread name is: " + Thread.currentThread().getName());
                    Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#finishedBytes: " + finishedBytes + ", totalBytes: " + totalBytes);

                    ///控制更新下载进度的周期
                    if (System.currentTimeMillis() - currentTimeMillis > progressInterval) {
                        long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;   ///下载进度的时间（毫秒）
                        currentTimeMillis = System.currentTimeMillis();
                        long diffFinishedBytes = finishedBytes - currentFinishedBytes;  ///下载进度的下载字节数
                        currentFinishedBytes = finishedBytes;
                        ///发送消息：更新下载进度
                        mHandler.obtainMessage(DOWNLOAD_PROGRESS, new long[]{finishedBytes, totalBytes, diffTimeMillis, diffFinishedBytes}).sendToTarget();
                    }

                    ///停止下载线程
                    if (!isStarted) {
                        Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#isStarted: " + isStarted);
                        return;
                    }
                }

                isStarted = false;

                ///发送消息：下载完成
                Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#mHandler.obtainMessage(DOWNLOAD_COMPLETE, null).sendToTarget();");
                mHandler.obtainMessage(DOWNLOAD_COMPLETE, null).sendToTarget();

            } else {
                Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#connection的响应码: " + connection.getResponseCode());
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
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
}
