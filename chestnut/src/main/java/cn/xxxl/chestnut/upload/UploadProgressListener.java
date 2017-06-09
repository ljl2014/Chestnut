package cn.xxxl.chestnut.upload;

/**
 * @author Leon
 * @since 1.0.0
 */
public interface UploadProgressListener {

    void onProgress(long current, long total);
}
