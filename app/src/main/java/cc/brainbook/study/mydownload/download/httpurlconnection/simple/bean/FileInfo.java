package cc.brainbook.study.mydownload.download.httpurlconnection.simple.bean;

public class FileInfo {
    public static final int FILE_STATUS_INIT = 0;
    public static final int FILE_STATUS_START = 1;
    public static final int FILE_STATUS_STOP = 2;
    public static final int FILE_STATUS_COMPLETE = 3;

    private int id;
    private volatile int status;
    private String fileUrl;
    private String fileName;
    private String savePath;
    private long totalBytes;
    private long finishedBytes;
    private long startTimeMillis;
    private long endTimeMillis;
    private long diffTimeMillis;
    private long diffFinishedBytes;

    public FileInfo(int id) {
        this.id = id;
        this.status = FILE_STATUS_INIT;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
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
                "id=" + id +
                ", status=" + status +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", savePath='" + savePath + '\'' +
                ", totalBytes=" + totalBytes +
                ", finishedBytes=" + finishedBytes +
                ", startTimeMillis=" + startTimeMillis +
                ", endTimeMillis=" + endTimeMillis +
                ", diffTimeMillis=" + diffTimeMillis +
                ", diffFinishedBytes=" + diffFinishedBytes +
                '}';
    }
}
