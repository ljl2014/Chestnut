package cn.xxxl.chestnut;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import cn.xxxl.chestnut.cache.CacheInfo;
import cn.xxxl.chestnut.client.ChestnutClient;
import cn.xxxl.chestnut.download.ChestnutDownloadClient;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.utils.CUL;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Retrofit;

/**
 * @author Leon
 * @since 1.0.0
 */
public class Chestnut {

    public static final String TAG = "Chestnut";

    private static ChestnutClient client;
    private static ChestnutDownloadClient downloadClient;

    public static void init(ChestnutClient chestnutClient) {
        init(chestnutClient, new ChestnutDownloadClient((Application) chestnutClient.getContext()));
    }

    public static void init(ChestnutClient chestnutClient,
                            ChestnutDownloadClient chestnutDownloadClient) {
        client = chestnutClient;
        downloadClient = chestnutDownloadClient;
        downloadClient.init(client.isLog() ?
                client.newBuilder().isLog(false).build().getRetrofit() :
                client.getRetrofit());
    }

    static {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                CUL.e(throwable.getMessage());
            }
        });
    }

    public static Context getContext() {
        assertInitialized();
        return client.getContext();
    }

    public static Retrofit getRetrofit() {
        assertInitialized();
        return client.getRetrofit();
    }

    private static void assertInitialized() {
        if (client == null)
            throw new IllegalStateException("Please initialize Chestnut First!");
    }

    private static void assertDownloadInitialized() {
        if (client == null || downloadClient == null)
            throw new IllegalStateException("Please initialize Chestnut (with Download) First!");
    }

    //#################### Normal Reqeust ####################
    public static ChestnutClient.HeadProcess head(@NonNull String url) {
        assertInitialized();
        return client.head(url);
    }

    public static <T> ChestnutClient.GetProcess<T> get(@NonNull String url,
                                                       final Class<T> tClass) {
        assertInitialized();
        return client.get(url, tClass);
    }

    public static <T> ChestnutClient.GetCacheProcess<T> getWithCache(@NonNull String url,
                                                                     final Class<T> tClass) {
        assertInitialized();
        return client.getWithCache(url, tClass);
    }

    public static <T> ChestnutClient.PostProcess<T> post(@NonNull String url,
                                                         final Class<T> tClass) {
        assertInitialized();
        return client.post(url, tClass);
    }

    public static <T> ChestnutClient.PostCacheProcess<T> postWithCache(@NonNull String url,
                                                                       final Class<T> tClass) {
        assertInitialized();
        return client.postWithCache(url, tClass);
    }

    public static <T> ChestnutClient.BodyProcess<T> body(@NonNull String url,
                                                         final Class<T> tClass) {
        assertInitialized();
        return client.body(url, tClass);
    }

    public static <T> ChestnutClient.BodyCacheProcess<T> bodyWithCache(@NonNull String url,
                                                                       final Class<T> tClass) {
        assertInitialized();
        return client.bodyWithCache(url, tClass);
    }

    public static <T> ChestnutClient.UploadProcess<T> upload(@NonNull String url,
                                                             final Class<T> tClass) {
        assertInitialized();
        return client.upload(url, tClass);
    }


    //#################### CACHE ####################
    public static <T> Observable<T> cache(Observable<T> originObservable, CacheInfo cacheInfo,
                                          final Class<T> tClass) {
        assertInitialized();
        return client.cache(originObservable, cacheInfo, tClass);
    }

    public static ChestnutClient.CacheInfoBuilder getCacheInfoBuilder(String url) {
        assertInitialized();
        return client.getCacheInfoBuilder(url);
    }

    //#################### Download ####################
    public static Observable<DownloadProgress> download(@NonNull String url) {
        assertDownloadInitialized();
        return downloadClient.download(url);
    }

    public static Observable<DownloadProgress> download(@NonNull String url,
                                                        @Nullable String saveName) {
        assertDownloadInitialized();
        return downloadClient.download(url, saveName);
    }

    public static Completable serviceDownload(@NonNull String url) {
        assertDownloadInitialized();
        return downloadClient.serviceDownload(url);
    }

    public static Completable serviceDownload(@NonNull String url,
                                              @Nullable String saveName) {
        assertDownloadInitialized();
        return downloadClient.serviceDownload(url, saveName);
    }

    public static Observable<DownloadProgress> getDownloadProgress(@NonNull String url) {
        assertDownloadInitialized();
        return downloadClient.getDownloadProgress(url);
    }

    public static Completable pauseServiceDownload(@NonNull String url) {
        assertDownloadInitialized();
        return downloadClient.pauseServiceDownload(url);
    }

    public static Completable deleteServiceDownload(@NonNull String url, boolean deleteFile) {
        assertDownloadInitialized();
        return downloadClient.deleteServiceDownload(url, deleteFile);
    }
}
