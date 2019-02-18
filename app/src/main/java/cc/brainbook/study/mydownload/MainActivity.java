package cc.brainbook.study.mydownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;

import cc.brainbook.study.mydownload.download.HttpURLConnectionDownload;

public class MainActivity extends AppCompatActivity {
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Downloads/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///Android 6.0以上版本必须动态设置权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    public void startDownload(View view) {
        ///实例化HttpURLConnectionDownload，并开始下载
        HttpURLConnectionDownload httpURLConnectionDownload = new HttpURLConnectionDownload();
        httpURLConnectionDownload.download("http://23.237.10.182/ljdy_v1.0.1.apk",
                "ljdy_v1.0.1.apk",
                DOWNLOAD_PATH);
    }
}
