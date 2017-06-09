package cn.xxxl.chestnut.download.entity;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public enum DownloadStatus {
    /**
     * 正常（未下载）
     */
    NORMAL,
    /**
     * 等待下载
     */
    WAITING,
    /**
     * 开始下载
     */
    STARTED,
    /**
     * 暂停下载
     */
    PAUSED,
    /**
     * 取消下载
     */
    CANCELED,
    /**
     * 下载完成
     */
    COMPLETED,
    /**
     * 下载失败
     */
    FAILED,
    /**
     * 删除文件
     */
    DELETED
}
