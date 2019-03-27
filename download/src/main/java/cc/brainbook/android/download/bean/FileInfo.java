package cc.brainbook.android.download.bean;

public class FileInfo {
    public static final int FILE_STATUS_ERROR = -1;
    public static final int FILE_STATUS_NEW = 0;
    public static final int FILE_STATUS_START = 1;
    public static final int FILE_STATUS_STOP = 2;
    public static final int FILE_STATUS_COMPLETE = 3;

    /**
     * 设置停止标志（并且声明为volatile），https://blog.csdn.net/changlei_shennan/article/details/44039905
     */
    private volatile int status;

    /**
     * 已经下载完的总耗时（毫秒）
     */
    private long finishedTimeMillis;

    /**
     * 已经下载完的总字节数
     */
    private long finishedBytes;

    private String fileUrl;
    private String fileName;
    private long fileSize;
    private String savePath;

    public FileInfo() {
        this.status = FILE_STATUS_NEW;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getFinishedTimeMillis() {
        return finishedTimeMillis;
    }

    public void setFinishedTimeMillis(long finishedTimeMillis) {
        this.finishedTimeMillis = finishedTimeMillis;
    }

    public long getFinishedBytes() {
        return finishedBytes;
    }

    public void setFinishedBytes(long finishedBytes) {
        this.finishedBytes = finishedBytes;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "status=" + status +
                ", finishedTimeMillis=" + finishedTimeMillis +
                ", finishedBytes=" + finishedBytes +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", savePath='" + savePath + '\'' +
                '}';
    }
}
