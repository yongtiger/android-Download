package cc.brainbook.study.mydownload.httpdownload.base;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import cc.brainbook.study.mydownload.httpdownload.config.Config;
import cc.brainbook.study.mydownload.httpdownload.exception.DownloadException;
import cc.brainbook.study.mydownload.httpdownload.util.Util;

public class BaseDownloadThread extends Thread {
    private static final String TAG = "TAG";

    protected Config mConfig;

    public BaseDownloadThread(Config config) {
        this.mConfig = config;
    }

    /**
     * 由下载文件的URL网址建立网络连接
     *
     * @param fileUrl
     * @throws MalformedURLException
     * @throws IOException
     */
    protected HttpURLConnection openConnection(String fileUrl) {
        URL url;
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            ///当URL为null或无效网络连接协议时：java.net.MalformedURLException: Protocol not found
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_MALFORMED_URL, "The protocol is not found.", e);
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (UnknownHostException e) {
            ///URL虽然以http://或https://开头、但host为空或无效host
            ///     java.net.UnknownHostException: http://
            ///     java.net.UnknownHostException: Unable to resolve host "aaa": No address associated with hostname
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_UNKNOWN_HOST, "The host is unknown.", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        }

        connection.setConnectTimeout(mConfig.connectTimeout);

//        try {
//            ///Operations that depend on being connected, like getInputStream, getOutputStream, etc, will implicitly perform the connection, if necessary.
//            ///https://stackoverflow.com/questions/16122999/java-urlconnection-when-do-i-need-to-use-the-connect-method
//            connection.connect();
//        } catch (IOException e) {
//            ///当没有网络链接
//            e.printStackTrace();
//            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
//        }

        return connection;
    }

    /**
     * 处理响应码
     *
     * 如果状态为正常（200）则继续运行，否则抛出异常
     *
     * @param connection
     */
    protected void handleResponseCode(HttpURLConnection connection) {
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "DownloadThread#run(): connection的响应码: " + responseCode);
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "The connection response code is " + responseCode);
        }
    }

    /**
     * 由网络连接获得文件名
     *
     * @param connection
     * @return
     */
    protected String getUrlFileName(HttpURLConnection connection) {
        String filename = Util.getUrlFileName(connection);
        if (filename.isEmpty()) {
            throw new DownloadException(DownloadException.EXCEPTION_FILE_NAME_NULL, "The file name cannot be null.");
        }
        return filename;
    }

    /**
     * 获得网络连接的缓冲输入流对象BufferedInputStream
     *
     * 注意：缓冲输入流对象比inputStream效率要高
     * https://blog.csdn.net/hfreeman2008/article/details/49174499
     *
     * @param connection
     * @return
     */
    protected BufferedInputStream getBufferedInputStream(HttpURLConnection connection) {
        ///获得网络连接connection的输入流对象
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        }
        ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
        return new BufferedInputStream(inputStream);
    }

    /**
     * BufferedInputStream的读操作
     *
     * 注意：缓冲输入流对象比inputStream效率要高
     * https://blog.csdn.net/hfreeman2008/article/details/49174499
     *
     * @param bufferedInputStream
     * @param bytes
     * @return
     */
    protected int inputStreamRead(BufferedInputStream bufferedInputStream, byte[] bytes) {
        int result;
        try {
            result = bufferedInputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        }
        return result;
    }

    /**
     * 获得保存文件的FileOutputStream
     *
     * @param saveFile
     * @return
     */
    protected FileOutputStream getFileOutputStream(File saveFile) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(saveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_FILE_NOT_FOUND, "The file is not found.", e);
        }
        return fileOutputStream;
    }

    /**
     * FileChannel的写操作
     *
     * @param channel
     * @param bytes
     * @param readLength
     */
    protected void channelWrite(FileChannel channel, byte[] bytes, int readLength) {
        ///Wrap a byte array into a buffer
        ByteBuffer buf = ByteBuffer.wrap(bytes, 0, readLength);
        try {
            channel.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadException.EXCEPTION_IO_EXCEPTION, "IOException expected.", e);
        }
    }
}
