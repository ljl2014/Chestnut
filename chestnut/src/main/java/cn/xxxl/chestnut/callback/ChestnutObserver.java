package cn.xxxl.chestnut.callback;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author Leon
 * @since 1.0.0
 */
public class ChestnutObserver<T> implements Observer<T> {

    private final ChestnutCallback<T> callback;

    public ChestnutObserver(ChestnutCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        callback.succeed(t);
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
