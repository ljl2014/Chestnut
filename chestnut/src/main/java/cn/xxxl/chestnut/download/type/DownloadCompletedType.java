package cn.xxxl.chestnut.download.type;

import org.reactivestreams.Publisher;

import cn.xxxl.chestnut.download.core.DownloadTask;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import io.reactivex.Flowable;

/**
 * @author Leon
 * @since 1.0.0
 */
public class DownloadCompletedType extends DownloadType {

    public DownloadCompletedType(DownloadTask task) {
        super(task);
    }

    @Override
    protected Publisher<DownloadProgress> realDownload() {
        return Flowable.just(new DownloadProgress(task.getInfoTotalSize()));
    }
}
