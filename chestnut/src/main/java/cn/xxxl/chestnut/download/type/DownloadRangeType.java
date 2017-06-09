package cn.xxxl.chestnut.download.type;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import cn.xxxl.chestnut.download.core.DownloadTask;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author Leon
 * @since 1.0.0
 */
public class DownloadRangeType extends DownloadType {

    public DownloadRangeType(DownloadTask task) {
        super(task);
    }

    @Override
    protected Publisher<DownloadProgress> realDownload() {
        List<Publisher<DownloadProgress>> tasks = new ArrayList<>();
        for (int i = 0; i < task.getDownloadTempInfos().size(); i++) {
            tasks.add(rangeDownload(i));
        }
        return Flowable.mergeDelayError(tasks);
    }

    private Publisher<DownloadProgress> rangeDownload(final int id) {
        return task.download(id)
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadProgress>>() {
                    @Override
                    public Publisher<DownloadProgress> apply(Response<ResponseBody> response)
                            throws Exception {
                        return save(id, response.body());
                    }
                })
                .compose(task.<DownloadProgress>getRetryFlowable());
    }

    private Publisher<DownloadProgress> save(final int id, final ResponseBody response) {

        return Flowable.create(new FlowableOnSubscribe<DownloadProgress>() {
            @Override
            public void subscribe(FlowableEmitter<DownloadProgress> emitter) throws Exception {
                task.save(id, emitter, response);
            }
        }, BackpressureStrategy.LATEST);
    }
}
