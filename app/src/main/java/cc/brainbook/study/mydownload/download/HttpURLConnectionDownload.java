package cc.brainbook.study.mydownload.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
    public volatile boolean isStarted = false;

    public HttpURLConnectionDownload(Context context) {
        mContext = context;
    }

    private void innerDownload(@NotNull String fileUrl,
                               @NotNull String fileName,
                               @NotNull String savePath,
                               int connectTimeout) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;   ///文件输出流
        try {
            ///由下载文件的URL网址建立Http网络连接connection
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.connect();

            ///如果网络连接connection的响应码为200，则开始下载过程
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ///获得网络连接connection的输入流对象
                inputStream = connection.getInputStream();
                ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                ///创建文件输出流对象
                File saveFile = new File(savePath, fileName);
                fileOutputStream = new FileOutputStream(saveFile);

                ///获得文件长度（建议用long类型，int类型最大为2GB）
                long total = connection.getContentLength();
                long finished = 0;

                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int length;
                ///输入流每次读取的内容（字节缓冲区）
                byte[] bytes = new byte[1024];
                while ((length = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    fileOutputStream.write(bytes, 0, length);
                    finished += length;
                    Log.d(TAG, "HttpURLConnectionDownload#innerDownload(): thread name is: " + Thread.currentThread().getName());
                    Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#finished: " + finished + ", total: " + total);


                    /* ------------ [下载进度] ------------ */
                    mHandler.obtainMessage(DOWNLOAD_PROGRESS, new long[]{finished, total}).sendToTarget();////////////如何控制更新进度周期？


                    ///停止下载线程
                    if (!isStarted) {
                        Log.d(TAG, "HttpURLConnectionDownload#innerDownload()#isStarted: " + isStarted);
                        return;
                    }
                }

                isStarted = false;


                /* ------------ [子线程访问UI线程：发送下载结束消息] ------------ */
                updateUI();


            }
        } catch (MalformedURLException e) { ///URL
            e.printStackTrace();
        } catch (FileNotFoundException e) { ///FileOutputStream
            e.printStackTrace();
        } catch (IOException e) {   ///HttpURLConnection
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* ------------ [子线程访问UI线程：发送下载结束消息] ------------ */
    private void updateUI() {
//        ///子线程访问UI线程方式之Handler.sendMessage
//        //Message msg = new Message();///不推荐！
//        Message msg = Message.obtain();///推荐！
//        msg.what = DOWNLOAD_COMPLETE;
//        Log.d(TAG, "HttpURLConnectionDownload#updateUI(): mHandler.sendMessage(msg); msg.what = DOWNLOAD_COMPLETE");
//        mHandler.sendMessage(msg);

        ///子线程访问UI线程方式之Handler.sendEmptyMessage
        ///适合当消息中无msg.obj，而仅有msg.what的情况
//                Log.d(TAG, "HttpURLConnectionDownload#updateUI(): mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);");
//                mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);

        ///子线程访问UI线程方式之Handler.obtainMessage
        ///适合当消息中有msg.obj，而还有msg.what的情况
        Log.d(TAG, "HttpURLConnectionDownload#updateUI(): mHandler.obtainMessage(DOWNLOAD_COMPLETE, null);");
        mHandler.obtainMessage(DOWNLOAD_COMPLETE, null).sendToTarget();
    }

    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath,
                         @NotNull DownloadCallback downloadCallback) {

        download(fileUrl, fileName, savePath, 10000, downloadCallback);
    }

    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath,
                         final int connectTimeout,
                         @NotNull DownloadCallback downloadCallback) {

        mDownloadCallback = downloadCallback;

        ///避免重复启动下载线程
        if (!isStarted) {
            isStarted = true;

            ///网络访问等耗时操作必须在子线程，否则阻塞主线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    innerDownload(fileUrl, fileName, savePath, connectTimeout);
                }
            }).start();
        }
    }

    ///注意：This Handler class should be static or leaks might occur (anonymous android.os.Handler)
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    Log.d(TAG, "HttpURLConnectionDownload#handleMessage(): msg.what = DOWNLOAD_COMPLETE");


                    /* ----------- [下载回调接口DownloadCallback] ----------- */
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onComplete();
                    }


                    break;
                /* ------------ [下载进度] ------------ */
                case DOWNLOAD_PROGRESS:


                    /* ----------- [下载进度：回调接口DownloadCallback] ----------- */
                    if (mDownloadCallback != null) {
                        long[] l = (long[]) msg.obj;
                        mDownloadCallback.onProgress(l[0], l[1]);///获取下载进度和文件长度
                    }


                    break;
            }
            super.handleMessage(msg);
        }
    };


    /* ----------- [下载回调接口DownloadCallback] ----------- */
    private DownloadCallback mDownloadCallback;


}
