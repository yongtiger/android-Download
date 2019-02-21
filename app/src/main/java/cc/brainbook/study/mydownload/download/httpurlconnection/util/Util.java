package cc.brainbook.study.mydownload.download.httpurlconnection.util;

import android.content.Context;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import cc.brainbook.study.mydownload.download.httpurlconnection.simple.exception.DownloadException;

public class Util {

    /**
     * 由HttpURLConnection获得文件名
     *
     * @param connection
     * @return
     */
    public static String getUrlFileName(HttpURLConnection connection) {
        String filename = "";
        String disposition = connection.getHeaderField("Content-Disposition");
        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                filename = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        }
        if (filename.length() == 0) {
            String path = connection.getURL().getPath();
            filename = new File(path).getName();
        }
        return filename;
    }

    /**
     * 关闭流Closeable
     *
     * @param closeables
     */
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得缺省的下载目录
     *
     * @param context
     * @return
     */
    public static File getDefaultFilesDir(Context context) {
        ///https://juejin.im/entry/5951d0096fb9a06bb8745f75
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null) {
            return downloadDir;
        } else {
            return context.getFilesDir();
        }
    }
    public static String getDefaultFilesDirPath(Context context) {
        return getDefaultFilesDir(context).getAbsolutePath();
    }

    /**
     * 创建本地目录
     *
     * mkdir()和mkdirs()的区别：
     *     mkdir()  创建此抽象路径名指定的目录。如果父目录不存在则创建不成功。
     *     mkdirs() 创建此抽象路径名指定的目录，包括所有必需但不存在的父目录。
     *
     * @param dir
     */
    public static boolean mkdirs(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }
        return true;
    }
    public static boolean mkdirs(String path) {
        return mkdirs(new File(path));
    }
    public static boolean mkdir(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                return false;
            }
        }
        return true;
    }
    public static boolean mkdir(String path) {
        return mkdir(new File(path));
    }
}
