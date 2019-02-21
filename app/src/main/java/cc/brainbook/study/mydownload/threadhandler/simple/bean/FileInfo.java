package cc.brainbook.study.mydownload.threadhandler.simple.bean;

public class FileInfo {
    public static final int FILE_STATUS_INIT = 0;
    public static final int FILE_STATUS_START = 1;
    public static final int FILE_STATUS_STOP = 2;
    public static final int FILE_STATUS_COMPLETE = 3;

    private volatile int status;    ///设置停止标志（并且声明为volatile），https://blog.csdn.net/changlei_shennan/article/details/44039905
    private String fileUrl;
    private String fileName;
    private String savePath;
    private long fileSize;
    private long finishedBytes;
    private long startTimeMillis;
    private long endTimeMillis;
    private long diffTimeMillis;
    private long diffFinishedBytes;

    public FileInfo() {
        this.status = FILE_STATUS_INIT;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public long getFinishedBytes() {
        return finishedBytes;
    }

    public void setFinishedBytes(long finishedBytes) {
        this.finishedBytes = finishedBytes;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public long getDiffTimeMillis() {
        return diffTimeMillis;
    }

    public void setDiffTimeMillis(long diffTimeMillis) {
        this.diffTimeMillis = diffTimeMillis;
    }

    public long getDiffFinishedBytes() {
        return diffFinishedBytes;
    }

    public void setDiffFinishedBytes(long diffFinishedBytes) {
        this.diffFinishedBytes = diffFinishedBytes;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                ", status=" + status +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", savePath='" + savePath + '\'' +
                ", fileSize=" + fileSize +
                ", finishedBytes=" + finishedBytes +
                ", startTimeMillis=" + startTimeMillis +
                ", endTimeMillis=" + endTimeMillis +
                ", diffTimeMillis=" + diffTimeMillis +
                ", diffFinishedBytes=" + diffFinishedBytes +
                '}';
    }
}
