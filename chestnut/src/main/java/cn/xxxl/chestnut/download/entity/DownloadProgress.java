package cn.xxxl.chestnut.download.entity;

import java.text.NumberFormat;

import cn.xxxl.chestnut.utils.CUFormat;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public class DownloadProgress {

    private long totalSize;
    private long currentSize;
    private DownloadStatus status = DownloadStatus.NORMAL;

    public DownloadProgress() {
        this(0);
    }

    public DownloadProgress(long totalSize) {
        this(totalSize, totalSize);
    }

    public DownloadProgress(long totalSize, DownloadStatus status) {
        this(totalSize, totalSize, status);
    }

    public DownloadProgress(long totalSize, long currentSize) {
        this(totalSize, currentSize, DownloadStatus.NORMAL);
    }

    public DownloadProgress(long totalSize, long currentSize, DownloadStatus status) {
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.status = status;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    /**
     * 获得格式化的总Size
     *
     * @return e.g. 10.00MB
     */
    public String getFormatTotalSize() {
        return totalSize <= 0L ? "未知大小" : CUFormat.formatSize(totalSize);
    }

    /**
     * 获得格式化的当前Size
     *
     * @return e.g. 10.00MB
     */
    public String getFormatCurrentSize() {
        return CUFormat.formatSize(currentSize);
    }

    /**
     * 获得格式化的状态字符串
     *
     * @return e.g. 5.00MB/10.00MB 5.00MB/未知大小
     */
    public String getFormatSizeRatio() {
        return getFormatCurrentSize() + "/" + getFormatTotalSize();
    }

    /**
     * 获得下载的百分比, 保留两位小数
     *
     * @return e.g. 99.99%
     */
    public String getFormatSizePercent() {
        String percent;
        Double result;
        if (totalSize <= 0L) {
            result = 0.0;
        } else {
            result = currentSize * 1.0 / totalSize;
        }
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        percent = nf.format(result);
        return percent;
    }

    /**
     * 获得下载的百分比数值
     *
     * @return e.g. 20% -> 20
     */
    public long getPercentNumber() {
        double result;
        if (totalSize <= 0L) {
            result = 0.0;
        } else {
            result = currentSize * 1.0 / totalSize;
        }
        return (long) (result * 100);
    }
}
