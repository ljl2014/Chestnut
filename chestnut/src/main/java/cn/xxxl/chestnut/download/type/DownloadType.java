package cn.xxxl.chestnut.download.type;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import cn.xxxl.chestnut.download.core.DownloadTask;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * @author Leon
 * @since 1.0.0
 */
public abstract class DownloadType {
    protected DownloadTask task;

    protected DownloadType(DownloadTask task) {
        this.task = task;
    }

    public Observable<DownloadProgress> download() {
        return Flowable.just(true)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        task.start();
                    }
                })
                .flatMap(new Function<Boolean, Publisher<DownloadProgress>>() {

                    @Override
                    public Publisher<DownloadProgress> apply(@NonNull Boolean aBoolean) throws
                            Exception {
                        return realDownload();
                    }
                })
                .doOnNext(new Consumer<DownloadProgress>() {
                    @Override
                    public void accept(DownloadProgress progress) throws Exception {
                        task.update(progress);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        task.error();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        task.complete();
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        task.cancel();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        task.finish();
                    }
                })
                .toObservable();
    }

    protected abstract Publisher<DownloadProgress> realDownload();
}
