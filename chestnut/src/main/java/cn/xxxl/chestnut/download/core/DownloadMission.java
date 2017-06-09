package cn.xxxl.chestnut.download.core;

import java.util.Map;
import java.util.concurrent.Semaphore;

import cn.xxxl.chestnut.download.entity.DownloadInfo;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.download.entity.DownloadStatus;
import cn.xxxl.chestnut.utils.CUFormat;
import cn.xxxl.chestnut.utils.CURx;
import cn.xxxl.chestnut.utils.DataStorage;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Leon
 * @since 1.0.0
 */
public class DownloadMission {

    private final DownloadTask task;

    private Disposable disposable;
    private DownloadProgress progress;
    private FlowableProcessor<DownloadProgress> processor;
    private boolean isCanceled = false;
    private boolean isCompleted = false;

    public DownloadMission(DownloadTask downloadTask) {
        this.task = downloadTask;
        this.progress = task.getDownloadProgress();
    }

    public void init(Map<String, DownloadMission> missionMap, Map<String,
            FlowableProcessor<DownloadProgress>> processorMap) {
        DownloadMission mission = missionMap.get(getUrl());
        if (mission == null)
            missionMap.put(getUrl(), this);
        else {
            if (mission.isCanceled())
                missionMap.put(getUrl(), this);
            else
                throw new IllegalStateException(CUFormat.urlExistsOnService(getUrl()));
        }
        this.processor = CURx.createProcessor(getUrl(), processorMap);
    }

    public void prepare() {
        DataStorage.storeOrUpdate(task.getDownloadInfo(), getUrl());
        progress.setStatus(DownloadStatus.WAITING);
        processor.onNext(progress);
    }

    public void start(final Semaphore semaphore) throws InterruptedException {
        if (isCanceled())
            return;

        semaphore.acquire();

        if (isCanceled()) {
            semaphore.release();
            return;
        }

        disposable = task.prepareDownload()
                .subscribeOn(Schedulers.io())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        setCanceled(true);
                        semaphore.release();
                    }
                })
                .subscribe(new Consumer<DownloadProgress>() {
                    @Override
                    public void accept(@NonNull DownloadProgress p) throws Exception {
                        progress = p;
                        processor.onNext(progress);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        progress.setStatus(DownloadStatus.FAILED);
                        processor.onNext(progress);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        progress.setStatus(DownloadStatus.COMPLETED);
                        processor.onNext(progress);
                    }
                });
    }

    public void pause() {
        CURx.dispose(disposable);
        setCanceled(true);
        if (processor != null && !isCompleted()) {
            progress.setStatus(DownloadStatus.PAUSED);
            processor.onNext(progress);
        }
    }


    public void delete(boolean deleteFile) {
        pause();
        if (processor != null) {
            progress = null;
            processor.onError(new IllegalStateException(CUFormat.urlDel(getUrl())));
        }
        if (deleteFile) {
            task.delAllFiles();
        }
        DataStorage.delete(DownloadInfo.class, getUrl());
    }

    public String getUrl() {
        return task.getUrl();
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
