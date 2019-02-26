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

import cc.brainbook.study.mydownload.httpdownload.DownloadTask;
import cc.brainbook.study.mydownload.httpdownload.bean.FileInfo;
import cc.brainbook.study.mydownload.httpdownload.interfaces.DownloadEvent;
import cc.brainbook.study.mydownload.httpdownload.interfaces.OnProgressListener;

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
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        ///实例化DownloadTask时传入Activity引用，方便操作view
        mDownloadTask = new DownloadTask(this);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity#onDestroy(): ==============");
        stopDownload(mDownloadTask);

        super.onDestroy();
    }

    public void startDownload(View view) {
        startDownload(mDownloadTask);
    }

    public void stopDownload(View view) {
        stopDownload(mDownloadTask);
    }

    private void startDownload(DownloadTask downloadTask) {
//        downloadTask.setFileUrl("http://23.237.10.182/ljdy_v1.0.1.apk")
//                .setFileName("ljdy_v1.0.1.apk")
//                .setSavePath(DOWNLOAD_PATH)
//                .setDownloadCallback(this)
//                .setOnProgressListener(new OnProgressListener() {
//                    @Override
//                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
//                        ///避免除0异常
//                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
//                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
//
//                        mTextView.setText(progress + ", " + speed);
//                    }
//                })
//                .start();
        downloadTask.setFileUrl("http://23.237.10.182/smqq.info.rar")
                .setFileName("smqq.info.rar")
                .setSavePath(DOWNLOAD_PATH)
                .setDownloadEvent(this)
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
                        ///避免除0异常
                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;

                        mTextView.setText(progress + ", " + speed);
                    }
                })
                .start();
//        downloadTask.setFileUrl("http://23.237.10.182/bbs.rar")
//                .setFileName("bbs.rar")
//                .setSavePath(DOWNLOAD_PATH)
//                .setDownloadEvent(this)
//                .setOnProgressListener(new OnProgressListener() {
//                    @Override
//                    public void onProgress(FileInfo fileInfo, long diffTimeMillis, long diffFinishedBytes) {
//                        ///避免除0异常
//                        int progress = fileInfo.getFinishedBytes() == 0 ? 0 : (int) (fileInfo.getFinishedBytes() * 100 / fileInfo.getFileSize());
//                        long speed = diffFinishedBytes == 0 ? 0 : diffFinishedBytes / diffTimeMillis;
//
//                        mTextView.setText(progress + ", " + speed);
//                    }
//                })
//                .start();
    }

    private void stopDownload(DownloadTask downloadTask) {
        if (downloadTask != null) {
            ///停止下载线程，避免内存泄漏
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

        Log.d(TAG, "MainActivity# onComplete()# fileInfo: " + fileInfo);
    }

}
