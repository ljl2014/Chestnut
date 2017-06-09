package cn.xxxl.chestnut.download.core;

import android.support.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.xxxl.chestnut.download.ChestnutDownloadServer;
import cn.xxxl.chestnut.download.entity.DownloadInfo;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.utils.CUFile;
import cn.xxxl.chestnut.utils.CUFormat;
import cn.xxxl.chestnut.utils.DataStorage;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-23.
 */
public class DownloadManager {

    private final int maxRetryCount;
    private final int maxConnectCount;
    private final String defaultSavePath;
    private final String defaultTempPath;

    private ChestnutDownloadServer server;
    private Map<String, DownloadTask> map;

    public DownloadManager(int maxRetryCount, int maxConnectCount, String defaultSavePath) {
        this.maxRetryCount = maxRetryCount;
        this.maxConnectCount = maxConnectCount;
        this.defaultSavePath = defaultSavePath;
        this.defaultTempPath = CUFormat.concat(defaultSavePath, File.separator, ".temp");
        CUFile.mkdirs(defaultSavePath, defaultTempPath);
        map = new HashMap<>();
    }

    public void setDownloadServer(ChestnutDownloadServer downloadServer) {
        server = downloadServer;
    }

    public Observable<DownloadProgress> download(final String url) {
        return download(url, null);
    }

    public Observable<DownloadProgress> download(final String url, @Nullable String saveName) {
        return addDownloadTask(url, saveName)
                .flatMap(new Function<Boolean, ObservableSource<DownloadProgress>>() {
                    @Override
                    public ObservableSource<DownloadProgress> apply(@NonNull Boolean aBoolean)
                            throws Exception {
                        return map.get(url).prepareDownload();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        if(map.containsKey(url))
                            map.remove(url);
                    }
                });
    }


    private Observable<Boolean> addDownloadTask(String url, @Nullable String saveName) {
        if (map.containsKey(url))
            return Observable.error(new IllegalStateException(CUFormat.urlExists(url)));

        DownloadInfo info = null;
        if (DataStorage.contains(DownloadInfo.class, url)) {
            info = DataStorage.load(DownloadInfo.class, url);
            if (info.isMission()) {
                return Observable.error(new IllegalStateException(
                        CUFormat.urlExistsOnService(url)));
            }
        }

        map.put(url, new DownloadTask(info == null ? new DownloadInfo(url, saveName) : info,
                maxRetryCount, maxConnectCount, defaultSavePath, defaultTempPath, server));

        return Observable.just(true);
    }
}
