package cn.xxxl.chestnut.download.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import cn.xxxl.chestnut.download.ChestnutDownloadServer;
import cn.xxxl.chestnut.download.entity.DownloadInfo;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.CUFormat;
import cn.xxxl.chestnut.utils.CURx;
import cn.xxxl.chestnut.utils.DataStorage;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;

import static cn.xxxl.chestnut.utils.CURx.createProcessor;
import static cn.xxxl.chestnut.utils.DataStorage.load;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public class DownloadService extends Service {

    public static final String RETRY_COUNT = "CHESTNUT_MAX_RETRY_COUNT";
    public static final String CONNECT_COUNT = "CHESTNUT_MAX_CONNECT_COUNT";
    public static final String TASK_COUNT = "CHESTNUT_MAX_TASK_COUNT";
    public static final String SAVE_PATH = "CHESTNUT_DEFAULT_SAVE_PATH";

    private int maxRetryCount;
    private int maxConnectCount;
    private int maxTaskCount;
    private String defaultSavePath;
    private String defaultTempPath;

    private DownloadBinder binder;
    private Disposable disposable;
    private Semaphore semaphore;
    private BlockingQueue<DownloadMission> queue;
    private Map<String, DownloadMission> missionMap;
    private Map<String, FlowableProcessor<DownloadProgress>> processorMap;


    @Override
    public void onCreate() {
        super.onCreate();
        binder = new DownloadBinder();
        queue = new LinkedBlockingDeque<>();
        missionMap = new ConcurrentHashMap<>();
        processorMap = new ConcurrentHashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            maxTaskCount = intent.getIntExtra(TASK_COUNT, 3);
            maxRetryCount = intent.getIntExtra(RETRY_COUNT, 3);
            maxConnectCount = intent.getIntExtra(CONNECT_COUNT, 5);
            defaultSavePath = intent.getStringExtra(SAVE_PATH);
            if (CUCheck.cString(defaultSavePath))
                defaultTempPath = CUFormat.concat(defaultSavePath, File.separator, ".temp");
            semaphore = new Semaphore(maxTaskCount);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        CURx.dispose(disposable);
        for (DownloadMission mission : missionMap.values()) {
            mission.pause();
        }
        queue.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initService();
        return binder;
    }


    public FlowableProcessor<DownloadProgress> getDownloadProgress(String url) {
        FlowableProcessor<DownloadProgress> processor = createProcessor(url, processorMap);
        DownloadMission mission = missionMap.get(url);
        if (mission == null && DataStorage.contains(DownloadInfo.class, url)) {
            DownloadInfo info = load(DownloadInfo.class, url);
            if (info == null)
                processor.onNext(new DownloadProgress(0));
            else
                processor.onNext(new DownloadProgress(info.getTotalSize(), info.getCurrentSize(),
                        info.getStatus()));
        }
        return processor;
    }

    public void startDownload(String url, @Nullable String saveName,
                              ChestnutDownloadServer server) throws InterruptedException {
        DownloadMission mission;
        DownloadInfo info;
        if (DataStorage.contains(DownloadInfo.class, url)) {
            info = DataStorage.load(DownloadInfo.class, url);
            if (!info.isMission()) {
                info.setMission(true);
                DataStorage.storeOrUpdate(info);
            }
        } else
            info = new DownloadInfo(url, saveName, true);
        mission = new DownloadMission(new DownloadTask(info, maxRetryCount, maxConnectCount,
                defaultSavePath, defaultTempPath, server));
        mission.init(missionMap, processorMap);
        mission.prepare();
        queue.put(mission);
    }

    // TODO: 17-6-8
    //    public void startAll() throws InterruptedException {
    //        for (DownloadMission each : missionMap.values()) {
    //            if (each.isCompleted()) {
    //                continue;
    //            }
    //            addDownloadMission(each);
    //        }
    //    }

    public void pauseDownload(String url) {
        DownloadMission mission = missionMap.get(url);
        if (mission != null)
            mission.pause();
    }

    // TODO: 17-6-8
    //    public void pauseAll() {
    //        for (DownloadMission each : missionMap.values()) {
    //            each.pause();
    //        }
    //    }

    public void deleteDownload(String url, boolean deleteFile) {
        DownloadMission mission = missionMap.get(url);
        if (mission != null) {
            mission.delete(deleteFile);
            missionMap.remove(url);
        } else {
            createProcessor(url, processorMap).onError(new IllegalStateException(CUFormat.urlDel
                    (url)));
            DataStorage.delete(DownloadInfo.class, url);
        }
    }

    // TODO: 17-6-8
    //    public void deleteAll(boolean deleteFile) {
    //
    //    }

    private void initService() {
        disposable = Observable.create(new ObservableOnSubscribe<DownloadMission>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<DownloadMission> emitter) throws
                    Exception {
                DownloadMission mission;
                while (!emitter.isDisposed()) {
                    try {
                        mission = queue.take();
                    } catch (InterruptedException e) {
                        Thread.sleep(100);
                        continue;
                    }
                    emitter.onNext(mission);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<DownloadMission>() {
                    @Override
                    public void accept(@NonNull DownloadMission downloadMission)
                            throws Exception {
                        downloadMission.start(semaphore);
                    }
                });
    }

    private void addDownloadMission(DownloadMission mission) throws InterruptedException {
        mission.init(missionMap, processorMap);
        mission.prepare();
        queue.put(mission);
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
