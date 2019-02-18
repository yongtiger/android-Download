package cc.brainbook.study.mydownload.download;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpURLConnectionDownload {
    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath) {

        download(fileUrl, fileName, savePath, 10000);
    }

    public void download(@NotNull final String fileUrl,
                         @NotNull final String fileName,
                         @NotNull final String savePath,
                         final int connectTimeout) {
        ///网络访问等耗时操作必须在子线程，否则阻塞主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                innerDownload(fileUrl, fileName, savePath, connectTimeout);
            }
        }).start();
    }

    private void innerDownload(@NotNull String fileUrl,
                         @NotNull String fileName,
                         @NotNull String savePath,
                         int connectTimeout) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;   ///文件输出流
        try {
            ///由下载文件的URL网址建立Http网络连接connection
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.connect();

            ///如果网络连接connection的响应码为200，则开始下载过程
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ///获得网络连接connection的输入流对象
                inputStream = connection.getInputStream();
                ///由输入流对象创建缓冲输入流对象（比inputStream效率要高）
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                ///创建文件输出流对象
                File saveFile = new File(savePath, fileName);
                fileOutputStream = new FileOutputStream(saveFile);

                ///每次循环读取的内容长度，如为-1表示输入流已经读取结束
                int length;
                ///输入流每次读取的内容（字节缓冲区）
                byte[] bytes = new byte[1024];
                while ((length = bufferedInputStream.read(bytes)) != -1) {
                    ///写入字节缓冲区内容到文件输出流
                    fileOutputStream.write(bytes, 0, length);
                }
            }
        } catch (MalformedURLException e) { ///URL
            e.printStackTrace();
        } catch (FileNotFoundException e) { ///FileOutputStream
            e.printStackTrace();
        } catch (IOException e) {   ///HttpURLConnection
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
