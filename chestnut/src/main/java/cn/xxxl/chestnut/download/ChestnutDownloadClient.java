package cn.xxxl.chestnut.download;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.Semaphore;

import cn.xxxl.chestnut.download.core.DownloadManager;
import cn.xxxl.chestnut.download.core.DownloadService;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public class ChestnutDownloadClient {

    public static final String TAG = "ChestnutDownloadClient";

    private Context app;
    private int maxConnectCount;
    private int maxRetryCount;
    private int maxTaskCount;
    private String defaultSavePath;

    private DownloadManager manager;
    private DownloadService service;

    private Retrofit retrofit;
    private ChestnutDownloadServer server;
    private Semaphore semaphore;
    private volatile boolean isServiceAlive = false;

    public ChestnutDownloadClient(Application app) {
        this(new Builder(app));
    }

    private ChestnutDownloadClient(Builder builder) {
        this.app = builder.app;
        this.maxConnectCount = builder.maxConnectCount;
        this.maxRetryCount = builder.maxRetryCount;
        this.maxTaskCount = builder.maxTaskCount;
        this.defaultSavePath = builder.defaultSavePath;
        this.manager = new DownloadManager(this.maxRetryCount,
                this.maxConnectCount, this.defaultSavePath);
    }


    public synchronized void init(Retrofit retrofit) {
        this.retrofit = retrofit;
        server = this.retrofit.create(ChestnutDownloadServer.class);
        manager.setDownloadServer(server);
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public static final class Builder {

        private final int DEFAULT_CONNECTIONCOUNT = 5;
        private final int DEFAULT_RETRYCOUNT = 3;
        private final int DEFAULT_TASKCOUNT = 3;

        private Context app;
        private int maxConnectCount = DEFAULT_CONNECTIONCOUNT;
        private int maxRetryCount = DEFAULT_RETRYCOUNT;
        private int maxTaskCount = DEFAULT_TASKCOUNT;
        private String defaultSavePath;

        public Builder(Application app) {
            this.app = app;
            defaultSavePath = this.app.getExternalFilesDir(Context.DOWNLOAD_SERVICE).getPath();
        }

        /**
         * 设置最大连接数
         *
         * @param connectCount 最大连接数（默认：5）
         */
        public Builder setMaxConnectCount(int connectCount) {
            this.maxConnectCount = connectCount;
            return this;
        }

        /**
         * 设置最大重试次数
         *
         * @param retryCount 最大重试次数（默认：3）
         */
        public Builder setMaxRetryCount(int retryCount) {
            this.maxRetryCount = retryCount;
            return this;
        }

        /**
         * 设置最大任务数
         *
         * @param taskCount 最大任务数（默认：3）
         * @return Builder
         */
        public Builder setMaxTaskCount(int taskCount) {
            this.maxTaskCount = taskCount;
            return this;
        }

        /**
         * 文件保存路径
         *
         * @param savePath default = ExternalFilesDir_DOWNLOAD_SERVICE
         * @return Builder
         */
        public Builder setDefaultSavePath(String savePath) {
            this.defaultSavePath = savePath;
            return this;
        }

        public ChestnutDownloadClient build() {
            return new ChestnutDownloadClient(this);
        }

    }

    //#################### Normal Download ####################
    public Observable<DownloadProgress> download(@NonNull String url) {
        return manager.download(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<DownloadProgress> download(@NonNull String url, @Nullable String saveName) {
        return manager.download(url, saveName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //#################### Service Download ####################
    public Completable serviceDownload(@NonNull final String url) {
        return serviceDownload(url, null);
    }

    public Completable serviceDownload(@NonNull final String url, @Nullable final String saveName) {
        return getCompletable(new ServiceDownloadCallback() {
            @Override
            public void call() throws Exception {
                service.startDownload(url, saveName, server);
            }
        })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io());
    }

    public Observable<DownloadProgress> getDownloadProgress(@NonNull String url) {
        return service.getDownloadProgress(url).toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable pauseServiceDownload(@NonNull final String url) {
        return getCompletable(new ServiceDownloadCallback() {
            @Override
            public void call() throws Exception {
                service.pauseDownload(url);
            }
        })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io());
    }

    public Completable deleteServiceDownload(@NonNull final String url, final boolean deleteFile) {
        return getCompletable(new ServiceDownloadCallback() {
            @Override
            public void call() throws Exception {
                service.deleteDownload(url, deleteFile);
            }
        })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io());
    }

    private Completable getCompletable(final ServiceDownloadCallback callback) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter e) throws Exception {
                if (!isServiceAlive) {
                    getSemaphore().acquire();
                    if (!isServiceAlive) {
                        startService(new Runnable() {
                            @Override
                            public void run() {
                                doCall(callback, e);
                                getSemaphore().release();
                            }
                        });
                    } else {
                        doCall(callback, e);
                        getSemaphore().release();
                    }
                } else {
                    doCall(callback, e);
                }
            }
        });
    }

    private void startService(final Runnable runnable) {
        Intent intent = new Intent(app, DownloadService.class);
        intent.putExtra(DownloadService.CONNECT_COUNT, maxConnectCount)
                .putExtra(DownloadService.RETRY_COUNT, maxRetryCount)
                .putExtra(DownloadService.TASK_COUNT, maxTaskCount)
                .putExtra(DownloadService.SAVE_PATH, defaultSavePath);
        app.startService(intent);
        app.bindService(intent, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadService.DownloadBinder downloadBinder =
                        (DownloadService.DownloadBinder) binder;
                service = downloadBinder.getService();
                app.unbindService(this);
                isServiceAlive = true;
                runnable.run();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isServiceAlive = false;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void doCall(ServiceDownloadCallback callback, CompletableEmitter emitter) {
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }
        emitter.onComplete();
    }

    private Semaphore getSemaphore() {
        if (semaphore == null)
            semaphore = new Semaphore(1);
        return semaphore;
    }

    private interface ServiceDownloadCallback {
        void call() throws Exception;
    }
}
