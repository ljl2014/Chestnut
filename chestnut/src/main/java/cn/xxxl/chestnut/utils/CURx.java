package cn.xxxl.chestnut.utils;

import org.reactivestreams.Publisher;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import retrofit2.HttpException;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-18.
 */
public class CURx {

    public static void dispose(Disposable... disposables) {
        if (disposables != null && disposables.length > 0)
            for (Disposable disposable : disposables)
                if (!disposable.isDisposed())
                    disposable.dispose();
    }

    public static <T> FlowableProcessor<T> createProcessor(
            String url, Map<String, FlowableProcessor<T>> processorMap) {

        if (processorMap.get(url) == null) {
            FlowableProcessor<T> processor =
                    BehaviorProcessor.<T>create().toSerialized();
            processorMap.put(url, processor);
        }
        return processorMap.get(url);
    }

    public static <T> ObservableTransformer<T, T> retryObservable(final int retryCount) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(@NonNull Integer integer, @NonNull Throwable throwable)
                            throws Exception {
                        return retry(retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    public static <T> FlowableTransformer<T, T> retryFlowable(final int retryCount) {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(@NonNull Integer integer, @NonNull Throwable throwable)
                            throws Exception {
                        return retry(retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    private static Boolean retry(int maxRetryCount, Integer integer, Throwable throwable) {
        if (integer > maxRetryCount)
            return false;
        else
            return throwable instanceof ProtocolException
                    || throwable instanceof UnknownHostException
                    || throwable instanceof HttpException
                    || throwable instanceof SocketTimeoutException
                    || throwable instanceof ConnectException
                    || throwable instanceof SocketException;
    }
}
