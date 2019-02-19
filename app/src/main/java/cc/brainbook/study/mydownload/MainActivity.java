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

import cc.brainbook.study.mydownload.download.HttpURLConnectionDownload;

public class MainActivity extends AppCompatActivity {
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
        ///设置下载完成的监听器
        httpURLConnectionDownload.setOnCompleteListener(new HttpURLConnectionDownload.OnCompleteListener() {
            @Override
            public void complete() {
                mTextView.setText("DOWNLOAD_COMPLETE");
            }
        });
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
                DOWNLOAD_PATH);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "============== MainActivity#onDestroy(): ==============");
        super.onDestroy();

        if (httpURLConnectionDownload != null) {
            ///停止下载线程，避免内存泄漏
            httpURLConnectionDownload.isStarted = false;
            ///注销下载监听器，避免内存泄漏
            httpURLConnectionDownload.setOnCompleteListener(null);
        }
    }
}
