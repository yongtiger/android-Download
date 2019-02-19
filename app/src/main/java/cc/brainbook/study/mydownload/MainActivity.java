package cc.brainbook.study.mydownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cc.brainbook.study.mydownload.download.DownloadCallback;
import cc.brainbook.study.mydownload.download.HttpURLConnectionDownload;

public class MainActivity extends AppCompatActivity implements DownloadCallback {
    private static final String TAG = "TAG";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    public TextView mTextView;

    private HttpURLConnectionDownload httpURLConnectionDownload;

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

        ///实例化HttpURLConnectionDownload时传入Activity引用，方便操作view
        httpURLConnectionDownload = new HttpURLConnectionDownload(this);

    }


    public void startDownload(View view) {

//        httpURLConnectionDownload.download("http://23.237.10.182/ljdy_v1.0.1.apk",
//                "ljdy_v1.0.1.apk",
//                DOWNLOAD_PATH);
//        httpURLConnectionDownload.download("http://23.237.10.182/smqq.info.rar",
//                "smqq.info.rar",
//                DOWNLOAD_PATH);
        httpURLConnectionDownload.download("http://23.237.10.182/bbs.rar",
                "bbs.rar",
                DOWNLOAD_PATH, this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity#onDestroy(): ==============");
        super.onDestroy();

        if (httpURLConnectionDownload != null) {
            ///停止下载线程，避免内存泄漏
            httpURLConnectionDownload.isStarted = false;
        }
    }


    /* ----------- [下载回调接口DownloadCallback] ----------- */
    @Override
    public void onComplete() {
        mTextView.setText("DOWNLOAD_COMPLETE");
    }

    @Override
    public void onProgress(long finishedBytes, long totalBytes, long diffTimeMillis, long diffFinishedBytes) {
        int progress = (int) (finishedBytes * 100 / totalBytes);
        long speed = diffFinishedBytes / diffTimeMillis;
        mTextView.setText(progress + ", " + speed);
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(long bytes) {
        String[] quantifiers = new String[] { "KB", "MB", "GB", "TB" };
        double number = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "";
            }
            number /= 1024;
            if (number < 512) {
                return String.format("%.2f", number) + " " + quantifiers[i];
            }
        }
    }
}
