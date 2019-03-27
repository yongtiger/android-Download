package cc.brainbook.study.mydownload;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cc.brainbook.android.download.DownloadTask;
import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.exception.DownloadException;
import cc.brainbook.android.download.interfaces.DownloadEvent;
import cc.brainbook.android.download.interfaces.OnProgressListener;

public class MainActivity extends AppCompatActivity implements DownloadEvent {
    private static final String TAG = "TAG";

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public TextView mTextView;

    private DownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "============== MainActivity# onCreate()# ==============");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tvTextView);

        if (requestPermission()) {
            init();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity# onDestroy()# ==============");
        super.onDestroy();

        stopDownload(mDownloadTask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "MainActivity# onRequestPermissionsResult()# requestCode: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this,"您拒绝了SD卡写入权限，请开通",Toast.LENGTH_SHORT).show();

                    ///开启本APP应用的设置页面
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);

                    ///退出APP
                    finish();
                }
                break;
        }
    }

    /**
     * 申请权限（强制获得权限）
     *
     * 注意：如果用户未授权权限则退出应用
     *
     * @return
     */
    private boolean requestPermission() {
        ///Android 6.0以上版本必须动态设置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("本应该需要权限：SD卡写入权限")
                        .setMessage("原因：保存APK下载的文件")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ///请求WRITE_EXTERNAL_STORAGE权限
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ///退出APP
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ///请求WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 初始化
     */
    private void init() {
        Log.d(TAG, "MainActivity# init()# ");

        ///创建下载任务类DownloadTask实例，并链式配置参数
        ///实例化DownloadTask时传入Context引用，方便操作（但要留意引起内存泄漏！）
        mDownloadTask = new DownloadTask(getApplicationContext())
                .setFileUrl("http://ljdy.tv/app/ljdy.apk")
                .setFileName("ljdy.apk")
                .setSavePath(DOWNLOAD_PATH)
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
                        ///避免除0异常
                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;

                        mTextView.setText(progress + ", " + speed);
                    }
                })
                .setDownloadEvent(this);
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


    /* ----------- [实现下载事件接口DownloadEvent] ----------- */
    @Override
    public void onStart(FileInfo fileInfo) {
        Log.d(TAG, "MainActivity# onStart()# fileInfo: " + fileInfo);
    }

    @Override
    public void onStop(FileInfo fileInfo) {
        Log.d(TAG, "MainActivity# onStop()# fileInfo: " + fileInfo);
    }

    @Override
    public void onComplete(FileInfo fileInfo) {
        Log.d(TAG, "MainActivity# onComplete()# fileInfo: " + fileInfo);

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
    }

    @Override
    public void onError(FileInfo fileInfo, DownloadException downloadException) {
        Log.d(TAG, "MainActivity# onError()# fileInfo: " + fileInfo);

        if (DownloadException.EXCEPTION_NETWORK_IO_EXCEPTION == downloadException.getCode()) {
            ///启动Wifi网络设置页面
            startWifiSettingsActivity();
        }
    }

    /**
     * 启动Wifi网络设置页面
     */
    private void startWifiSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }
}
