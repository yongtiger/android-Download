package cc.brainbook.study.mydownload.download.httpurlconnection.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class Util {

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

    ///关闭流Closeable
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
}
