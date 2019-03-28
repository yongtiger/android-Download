package cc.brainbook.study.mydownload;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
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

        ///初始化过程
        init();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity# onStart()# ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "MainActivity# onStart()# ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "MainActivity# onStart()# ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity# onStart()# ");
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
                ///刷新当前页面
                refresh();
                break;
            case ACTION_WIFI_SETTINGS_REQUEST_CODE:
                ///刷新当前页面
                refresh();
                break;

        }
    }

    /**
     * 刷新当前页面
     *
     * 注意：尽量不用recreate()！因为存在闪屏现象
     * https://stackoverflow.com/questions/2486934/programmatically-relaunch-recreate-an-activity/23989089
     */
    private void refresh() {
        startActivity(getIntent());
        finish();
        overridePendingTransition(0, 0);
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

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ///如果未获得SD卡写入权限，则使用APP本应用的外部文件目录（无需获得SD卡写入权限）
//            mDownloadTask.setSavePath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
//        } else {
            ///如果获得SD卡写入权限，则使用SD卡外部存储目录
            mDownloadTask.setSavePath(DOWNLOAD_PATH);
//        }
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
        Log.d(TAG, "MainActivity# onComplete()# fileInfo: " + fileInfo.getStatus());

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
        if (downloadException.getCause() == null) {
            Log.d(TAG, "MainActivity# onError()# downloadException.getCode(): " + downloadException.getCode() + ", " + downloadException.getMessage());
            Toast.makeText(this, downloadException.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "MainActivity# onError()# downloadException.getCode(): " + downloadException.getCode() + ", " + downloadException.getMessage() + "\n" + downloadException.getCause().getMessage());
            Toast.makeText(this, downloadException.getMessage() + "\n" + downloadException.getCause().getMessage(), Toast.LENGTH_LONG).show();
        }

        if (DownloadException.EXCEPTION_FILE_URL_NULL == downloadException.getCode()) {

        } else if (DownloadException.EXCEPTION_SAVE_PATH_MKDIR == downloadException.getCode()) {

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
        } else {

        }
    }

}
