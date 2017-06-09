package cn.xxxl.chestnut.download.type;

import org.reactivestreams.Publisher;

import cn.xxxl.chestnut.download.core.DownloadTask;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author Leon
 * @since 1.0.0
 */
public class DownloadNormalType extends DownloadType {

    public DownloadNormalType(DownloadTask task) {
        super(task);
    }

    @Override
    protected Publisher<DownloadProgress> realDownload() {
        return task.download()
                .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadProgress>>() {
                    @Override
                    public Publisher<DownloadProgress> apply(@NonNull Response<ResponseBody>
                                                                   responseBodyResponse) throws
                            Exception {
                        return save(responseBodyResponse.body());
                    }
                })
                .compose(task.<DownloadProgress>getRetryFlowable());
    }

    private Publisher<DownloadProgress> save(final ResponseBody responseBody) {
        return Flowable.create(new FlowableOnSubscribe<DownloadProgress>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<DownloadProgress> e) throws Exception {
                task.save(e, responseBody);
            }
        }, BackpressureStrategy.LATEST);
    }
}
