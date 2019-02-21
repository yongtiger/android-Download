package cc.brainbook.study.mydownload.threadhandler.simple.config;

public class Config {

    public int connectTimeout = 10000;    ///10秒
    ///BufferedInputStream的默认缓冲区大小是8192字节。当每次读取数据量接近或远超这个值时，两者效率就没有明显差别了
    ///https://blog.csdn.net/xisuo002/article/details/78742631
    public int bufferSize = 1024; ///1k bytes
    public int progressInterval = 1000;   ///1秒
}
