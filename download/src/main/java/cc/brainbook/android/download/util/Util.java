package cc.brainbook.android.download.util;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class Util {
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
     * 先尝试获取应用的外部文件目录getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
     * 如果不存在（比如SD卡不存在）则返回应用的文件目录getFilesDir()
     *
     * @param context
     * @return
     */
    public static File getDefaultFilesDir(@NonNull Context context) {
        ///https://juejin.im/entry/5951d0096fb9a06bb8745f75
        final File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null) {
            return downloadDir;
        } else {
            return context.getFilesDir();
        }
    }
    @NonNull
    public static String getDefaultFilesDirPath(@NonNull Context context) {
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
    public static boolean mkdirs(@NonNull File dir) {
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
    public static boolean mkdir(@NonNull File dir) {
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

    /**
     * 检查文件或目录是否可写入
     *
     * @param path
     * @return
     */
    public static boolean isCanWrite(String path) {
        return new File(path).canWrite();
    }
}
