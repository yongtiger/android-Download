package cc.brainbook.study.mydownload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cc.brainbook.android.download.DownloadTask;
import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.enumeration.DownloadState;
import cc.brainbook.android.download.exception.DownloadException;
import cc.brainbook.android.download.listener.DownloadListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    public TextView mTextView;

    private DownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "============== MainActivity# onCreate()# ==============");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tvTextView);

        ///Android 6.0以上版本必须动态设置权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            init();
        }

    }

    ///https://developer.android.com/training/permissions/requesting?hl=zh-cn#handle-response
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    init();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity# onDestroy()# ==============");
        super.onDestroy();

        ///避免内存泄漏
        if (mDownloadTask != null) {
            mDownloadTask.setDownloadListener(null);
        }
    }

    public void init() {
        ///实例化下载监听器对象
        DownloadListener downloadListener = new MyDownloadListener();

        ///创建下载任务类DownloadTask实例，并链式配置参数
        ///实例化DownloadTask时传入Context引用，方便操作（但要留意引起内存泄漏！）
        mDownloadTask = new DownloadTask(getApplicationContext())
                .setFileUrl("http://ljdy.tv/test/ljdy_api_16.apk")
//                .setFileName("ljdy.apk")
                .setSavePath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())
                .setDownloadListener(downloadListener);
    }

    public void startDownload(View view) {
        startDownload(mDownloadTask);
    }

    public void stopDownload(View view) {
        stopDownload(mDownloadTask);
    }

    private void startDownload(DownloadTask downloadTask) {
        if (downloadTask != null) {
            downloadTask.start();
        }
    }

    private void stopDownload(DownloadTask downloadTask) {
        if (downloadTask != null) {
            downloadTask.stop();
        }
    }

    private class MyDownloadListener implements DownloadListener {
        @Override
        public void onStateChanged(FileInfo fileInfo, DownloadState state) {
            Log.d(TAG, "MainActivity# MyDownloadListener# onStateChanged()# ---------- " + state + " ----------");

            switch (state) {
                case STARTED:

                    break;
                case STOPPED:

                    break;
                case SUCCEED:
                    ///下载文件URL
                    String fileUrl = fileInfo.getFileUrl();
                    ///下载文件名
                    String fileName = fileInfo.getFileName();
                    ///下载文件大小
                    long fileSize = fileInfo.getFileSize();
                    ///下载文件保存路径
                    String savePath = fileInfo.getSavePath();
                    ///已经下载完的总耗时（毫秒）
                    long finishedTimeMillis = fileInfo.getFinishedTimeMillis();
                    ///已经下载完的总字节数
                    long finishedBytes = fileInfo.getFinishedBytes();

                    break;
                case FAILED:

                    break;
            }

            Log.d(TAG, "MainActivity# MyDownloadListener# onStateChanged()# fileInfo: " + fileInfo);
            Log.d(TAG, "MainActivity# MyDownloadListener# onStateChanged()# ------------------------------");
        }

        @Override
        public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
            Log.d(TAG, "MainActivity# onProgress()# fileInfo: " + fileInfo);
            Log.d(TAG, "MainActivity# onProgress()# diffTimeMillis: " + diffTimeMillis);
            Log.d(TAG, "MainActivity# onProgress()# diffFinishedBytes: " + diffFinishedBytes);

            ///避免除0异常
            int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
            long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
            Log.d(TAG, "MainActivity# onProgress()# progress, speed: " + progress + ", " + speed);

            mTextView.setText(progress + ", " + speed);
        }

        @Override
        public void onError(FileInfo fileInfo, Exception e) {
            e.printStackTrace();
            if (e.getCause() == null) {
                Log.d(TAG, "MainActivity# onError()# Message: " + e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "MainActivity# onError()# Message: " + e.getMessage() + "\n" + e.getCause().getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage() + "\n" + e.getCause().getMessage(), Toast.LENGTH_LONG).show();
            }

            if (e instanceof DownloadException) {
                DownloadException downloadException = (DownloadException) e;

                if (DownloadException.EXCEPTION_FILE_URL_NULL == downloadException.getCode()) {

                } else if (DownloadException.EXCEPTION_FILE_MKDIR_EXCEPTION == downloadException.getCode()) {

                } else if (DownloadException.EXCEPTION_NETWORK_MALFORMED_URL == downloadException.getCode()) {
                    ///当URL为null或无效网络连接协议时：java.net.MalformedURLException: Protocol not found

                } else if (DownloadException.EXCEPTION_NETWORK_UNKNOWN_HOST == downloadException.getCode()) {
                    ///URL虽然以http://或https://开头、但host为空或无效host
                    ///     java.net.UnknownHostException: http://
                    ///     java.net.UnknownHostException: Unable to resolve host "aaa": No address associated with hostname

                } else if (DownloadException.EXCEPTION_NETWORK_IO_EXCEPTION == downloadException.getCode()) {
                    ///如果没有网络连接

                    ///开启Wifi网络设置页面
                    startWifiSettingsActivity();
                } else if (DownloadException.EXCEPTION_NETWORK_FILE_IO_EXCEPTION == downloadException.getCode()) {
                    ///如果下载过程中断开网络连接，抛出异常DownloadException.EXCEPTION_NETWORK_FILE_IO_EXCEPTION
                    Log.d(TAG, "MainActivity# onError()# !!!!!! DownloadException.EXCEPTION_NETWORK_FILE_IO_EXCEPTION !!!!!! Message: " + e.getMessage());

//                    ///开启Wifi网络设置页面
//                    startWifiSettingsActivity();
                } else {

                }
            }
        }
    }

    /**
     * 开启Wifi网络设置页面
     */
    private void startWifiSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, 1);
    }

}
