package cc.brainbook.study.mydownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cc.brainbook.android.download.DownloadTask;
import cc.brainbook.android.download.bean.FileInfo;
import cc.brainbook.android.download.interfaces.DownloadEvent;
import cc.brainbook.android.download.interfaces.OnProgressListener;

public class MainActivity extends AppCompatActivity implements DownloadEvent {
    private static final String TAG = "TAG";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public TextView mTextView;

    private DownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "============== MainActivity#onCreate(): ==============");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tvTextView);

        ///Android 6.0以上版本必须动态设置权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            init();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity#onDestroy(): ==============");
        stopDownload(mDownloadTask);

        super.onDestroy();
    }

    ///https://developer.android.com/training/permissions/requesting?hl=zh-cn#handle-response
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    private void init() {
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
                });
//        mDownloadTask = new DownloadTask(getApplicationContext())
//                .setFileUrl("http://23.237.10.182/smqq.info.rar")
//                .setFileName("smqq.info.rar")
//                .setSavePath(DOWNLOAD_PATH)
//                .setOnProgressListener(new OnProgressListener() {
//                    @Override
//                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
//                        ///避免除0异常
//                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
//                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
//
//                        mTextView.setText(progress + ", " + speed);
//                    }
//                });
//        mDownloadTask = new DownloadTask(getApplicationContext())
//                .setFileUrl("http://23.237.10.182/bbs.rar")
//                .setFileName("bbs.rar")
//                .setSavePath(DOWNLOAD_PATH)
//                .setOnProgressListener(new OnProgressListener() {
//                    @Override
//                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
//                        ///避免除0异常
//                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
//                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
//
//                        mTextView.setText(progress + ", " + speed);
//                    }
//                });

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

}
