package cc.brainbook.study.mydownload.download;

import android.app.Activity;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cc.brainbook.study.mydownload.MainActivity;

public class HttpURLConnectionDownload {
    private static final String TAG = "TAG";
    private static final int DOWNLOAD_COMPLETE = 1;

    private Context mContext;

    public HttpURLConnectionDownload(Context context) {
        mContext = context;
        ///注意：外部类成员变量不能初始化为静态内部类实例！此时尚未执行外部类的构造方法，即mContext为null
        ///所以要在外部类的构造方法中赋值外部类成员变量
        mHandler = new MyHandler((MainActivity) mContext);
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

                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int length;
                ///输入流每次读取的内容（字节缓冲区）
                byte[] bytes = new byte[1024];
                while ((length = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    fileOutputStream.write(bytes, 0, length);
                }

                /* ------------ [子线程访问UI线程：发送下载结束消息] ------------ */

//                ///子线程访问UI线程方式之Activity.runOnUiThread
//                ((MainActivity) mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((MainActivity) mContext).mTextView.setText("DOWNLOAD_COMPLETE");
//                    }
//                });

//                ///子线程访问UI线程方式之Handler.post+Runnable
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((MainActivity) mContext).mTextView.setText("DOWNLOAD_COMPLETE");
//                    }
//                });

//                ///子线程访问UI线程方式之Handler.sendMessage
//                //Message msg = new Message();///不推荐！
//                Message msg = Message.obtain();///推荐！
//                msg.what = DOWNLOAD_COMPLETE;
//                Log.d(TAG, "HttpURLConnectionDownload#innerDownload(): mHandler.sendMessage(msg); msg.what = DOWNLOAD_COMPLETE");
//                mHandler.sendMessage(msg);

                ///子线程访问UI线程方式之Handler.sendEmptyMessage
                ///适合当消息中无msg.obj，而仅有msg.what的情况
//                Log.d(TAG, "HttpURLConnectionDownload#innerDownload(): mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);");
//                mHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);

                ///子线程访问UI线程方式之Handler.obtainMessage
                ///适合当消息中有msg.obj，而还有msg.what的情况
                Log.d(TAG, "HttpURLConnectionDownload#innerDownload(): mHandler.obtainMessage(DOWNLOAD_COMPLETE, null);");
                mHandler.obtainMessage(DOWNLOAD_COMPLETE, null).sendToTarget();

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

    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath) {

        download(fileUrl, fileName, savePath, 10000);
    }

    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath,
                         final int connectTimeout) {
        ///网络访问等耗时操作必须在子线程，否则阻塞主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                innerDownload(fileUrl, fileName, savePath, connectTimeout);
            }
        }).start();
    }

    ///注意：This Handler class should be static or leaks might occur (anonymous android.os.Handler)
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case DOWNLOAD_COMPLETE:
//                    Log.d(TAG, "HttpURLConnectionDownload#handleMessage(): msg.what = DOWNLOAD_COMPLETE");
//
//                    ///更新UI线程
//                    ((MainActivity) mContext).mTextView.setText("DOWNLOAD_COMPLETE");
//
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };
    static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case DOWNLOAD_COMPLETE:
                        Log.d(TAG, "HttpURLConnectionDownload#handleMessage(): msg.what = DOWNLOAD_COMPLETE");

                        ///更新UI线程
                        ((MainActivity) activity).mTextView.setText("DOWNLOAD_COMPLETE");

                        break;
                }
            }
        }
    }

    ///注意：外部类成员变量不能初始化为静态内部类实例！此时尚未执行外部类的构造方法，即mContext为null
    ///所以要在外部类的构造方法中赋值外部类成员变量
//    private Handler mHandler = new MyHandler((MainActivity) mContext);
    private Handler mHandler;

}
