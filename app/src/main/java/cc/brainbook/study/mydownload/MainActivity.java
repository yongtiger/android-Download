package cc.brainbook.study.mydownload;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private static final int ACTION_APPLICATION_DETAILS_SETTINGS_REQUEST_CODE = 1;
    private static final int ACTION_WIFI_SETTINGS_REQUEST_CODE = 2;

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public TextView mTextView;

    private DownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "============== MainActivity# onCreate()# ==============");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tvTextView);

        ///[Android M 6.0 (API level 23)及以上版本必须动态设置权限]
        ///申请权限（强制获得权限，否则退出APP应用）
        if (requestPermission()) {
            ///初始化
            init();
        }
    }

    ///[Android M 6.0 (API level 23)及以上版本必须动态设置权限]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "MainActivity# onRequestPermissionsResult()# requestCode: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ///初始化
                    init();
                } else {
                    Toast.makeText(this,"您拒绝了SD卡写入权限，请开通", Toast.LENGTH_SHORT).show();

                    ///用户未选择任何（同意、拒绝、不再询问）时为false，第一次用户拒绝后为true，直到用户点击不再询问时为false
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ///开启本APP应用的设置页面
                        startApplicationDetailsSettingsActivity();
                    }

                    ///退出APP
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity# onDestroy()# ==============");
        super.onDestroy();

        stopDownload(mDownloadTask);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "MainActivity# onActivityResult()# requestCode: " + requestCode + ", resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTION_APPLICATION_DETAILS_SETTINGS_REQUEST_CODE:
                ///刷新本页面
                recreate();// 直接调用Activity的recreate()方法重启Activity
                break;
            case ACTION_WIFI_SETTINGS_REQUEST_CODE:
                ///刷新本页面
                recreate();// 直接调用Activity的recreate()方法重启Activity
                break;

        }
    }

    ///[Android M 6.0 (API level 23)及以上版本必须动态设置权限]
    /**
     * 申请权限（强制获得权限，否则退出APP应用）
     *
     * @return
     */
    private boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            ///用户未选择任何（同意、拒绝、不再询问）时为false，第一次用户拒绝后为true，直到用户点击不再询问时为false
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
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 开启本APP应用的设置页面
     */
    private void startApplicationDetailsSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, ACTION_APPLICATION_DETAILS_SETTINGS_REQUEST_CODE);
    }

    /**
     * 开启Wifi网络设置页面
     */
    private void startWifiSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, ACTION_WIFI_SETTINGS_REQUEST_CODE);
    }

    /**
     * 初始化过程
     */
    private void init() {
        Log.d(TAG, "MainActivity# init()# ");

        ///创建下载任务类DownloadTask实例，并链式配置参数
        ///实例化DownloadTask时传入Context引用，方便操作（但要留意引起内存泄漏！）
        mDownloadTask = new DownloadTask(getApplicationContext())
                .setFileUrl("http://ljdy.tv/app/ljdy.apk")
                .setFileName("ljdy.apk")
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ///如果未获得SD卡写入权限，则使用APP本应用的外部文件目录（无需获得SD卡写入权限）
            mDownloadTask.setSavePath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        } else {
            ///如果获得SD卡写入权限，则使用SD卡外部存储目录
            mDownloadTask.setSavePath(DOWNLOAD_PATH);
        }
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
        Log.d(TAG, "MainActivity# onError()# downloadException.getCode(): " + downloadException.getCode() + ", " + downloadException.getMessage());

        if (DownloadException.EXCEPTION_FILE_URL_NULL == downloadException.getCode()) {
            Toast.makeText(this, downloadException.getMessage(), Toast.LENGTH_LONG).show();
        } else
        if (DownloadException.EXCEPTION_SAVE_PATH_MKDIR == downloadException.getCode()) {
            Toast.makeText(this, downloadException.getMessage(), Toast.LENGTH_LONG).show();
        }
        ///当URL为null或无效网络连接协议时：java.net.MalformedURLException: Protocol not found
        if (DownloadException.EXCEPTION_NETWORK_MALFORMED_URL == downloadException.getCode()) {
            Toast.makeText(this, downloadException.getMessage() + "\n" + downloadException.getCause().getMessage(), Toast.LENGTH_LONG).show();
        } else
        ///URL虽然以http://或https://开头、但host为空或无效host
        ///     java.net.UnknownHostException: http://
        ///     java.net.UnknownHostException: Unable to resolve host "aaa": No address associated with hostname
        if (DownloadException.EXCEPTION_NETWORK_UNKNOWN_HOST == downloadException.getCode()) {
            Toast.makeText(this, downloadException.getMessage() + "\n" + downloadException.getCause().getMessage(), Toast.LENGTH_LONG).show();
        } else
        ///如果没有网络连接
        if (DownloadException.EXCEPTION_NETWORK_IO_EXCEPTION == downloadException.getCode()) {
            ///开启Wifi网络设置页面
            startWifiSettingsActivity();
        } else {
            Toast.makeText(this, downloadException.getMessage() + "\n" + downloadException.getCause().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
