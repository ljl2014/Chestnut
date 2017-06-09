package cn.xxxl.chestnut.download.core;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.xxxl.chestnut.download.ChestnutDownloadServer;
import cn.xxxl.chestnut.download.entity.DownloadInfo;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.download.entity.DownloadStatus;
import cn.xxxl.chestnut.download.entity.DownloadTempInfo;
import cn.xxxl.chestnut.download.type.DownloadCompletedType;
import cn.xxxl.chestnut.download.type.DownloadContinueType;
import cn.xxxl.chestnut.download.type.DownloadNormalType;
import cn.xxxl.chestnut.download.type.DownloadRangeType;
import cn.xxxl.chestnut.download.type.DownloadType;
import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.CUFormat;
import cn.xxxl.chestnut.utils.CURx;
import cn.xxxl.chestnut.utils.CUTime;
import cn.xxxl.chestnut.utils.DataStorage;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import retrofit2.Response;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public class DownloadTask {

    private static final long defaultUpdateTime = 24 * 60 * 60 * 1000L;

    private final String url;
    private final int maxRetryCount;
    private final int maxConnectCount;
    private final String defaultSavePath;
    private final String defaultTempPath;
    private final ChestnutDownloadServer server;

    private final DownloadInfo info;

    public DownloadTask(DownloadInfo info, int maxRetryCount, int maxConnectCount, String
            savePath, String tempPath, ChestnutDownloadServer server) {
        this.info = info;
        this.url = info.getUrl();
        this.maxRetryCount = maxRetryCount;
        this.maxConnectCount = maxConnectCount;
        this.defaultSavePath = savePath;
        this.defaultTempPath = tempPath;
        this.server = server;
    }

    //####################　DOWNLOAD准备　####################
    public Observable<DownloadProgress> prepareDownload() {
        return Observable.just(info.isInit() && info.getCreateTime() > defaultUpdateTime)
                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(@NonNull Boolean aBoolean) throws
                            Exception {
                        return aBoolean ? Observable.just(true) : checkInfo(url, "bytes=0-");
                    }
                })
                .flatMap(new Function<Boolean, ObservableSource<? extends DownloadType>>() {
                    @Override
                    public ObservableSource<? extends DownloadType> apply(@NonNull Boolean
                                                                                  aBoolean)
                            throws Exception {
                        if (!aBoolean)      //Task初始化失败
                            throw new IllegalStateException(CUFormat.urlInitFailed(url));

                        return isFileExist() ? getFileExistsType() : getFileNoExistsType();
                    }
                })
                .flatMap(new Function<DownloadType, ObservableSource<DownloadProgress>>() {
                    @Override
                    public ObservableSource<DownloadProgress> apply(@NonNull DownloadType
                                                                            downloadType) throws
                            Exception {
                        return downloadType.download();
                    }
                })
                .sample(500, TimeUnit.MILLISECONDS, true);
    }

    private ObservableSource<Boolean> checkInfo(final String url, String range) {
        return server.checkRange(url, range)
                .map(new Function<Response<Void>, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Response<Void> voidResponse) throws Exception {
                        return saveDownloadInfo(url, voidResponse);
                    }
                })
                .compose(CURx.<Boolean>retryObservable(maxRetryCount));
    }

    private boolean saveDownloadInfo(String url, Response response) {
        if (response.isSuccessful()) {
            setInfoSaveName(url, response);
            setInfoContentLength(response);
            setInfoLastModify(response);
            setInfoRange(response);
            setTempInfoList();
            info.setInit(true);
            return true;
        } else {
            info.setInit(false);
            return false;
        }
    }

    private void setInfoSaveName(String url, Response response) {
        String contentName = info.getSaveName();
        if (!CUCheck.cString(contentName)) {
            contentName = getContentName(url, response);
            info.setSaveName(contentName);
        }
        info.setSavePath(CUFormat.concat(defaultSavePath, File.separator, contentName));
        info.setTempPath(CUFormat.concat(defaultTempPath, File.separator, info.getTempName()));
        info.setCreateTime(CUTime.getCurrentTimeLong());
    }

    private void setInfoContentLength(Response response) {
        info.setTotalSize(getContentLength(response));
    }

    private void setInfoLastModify(Response response) {
        info.setLastModify(getLastModify(response));
    }

    private void setInfoRange(Response response) {
        boolean isRange = CUCheck.cString(getContentRange(response))
                && getAcceptRanges(response).equals("bytes")
                && getContentLength(response) != -1
                && !isChunked(response)
                && (response.code() == 200 || response.code() == 206);
        info.setRange(isRange);
    }

    private void setTempInfoList() {
        int connectCount = 1;
        if (info.isRange() && info.getTotalSize() > 0)
            for (int i = 1; i <= maxConnectCount; i++)
                if (i == maxConnectCount ||
                        info.getTotalSize() < Math.pow(2, i) * 8 * CUFormat.MB) {
                    connectCount = i;
                    break;
                }

        long start;
        long end;
        long size = info.getTotalSize() / connectCount;

        for (int i = 0; i < connectCount; i++) {
            if (i == connectCount - 1) {
                start = i * size;
                end = info.getTotalSize() - 1;
            } else {
                start = i * size;
                end = (i + 1) * size - 1;
            }
            DownloadTempInfo tempInfo = new DownloadTempInfo(info.getUrl(), i);
            tempInfo.setStart(start);
            tempInfo.setEnd(end);
            info.addTempInfo(tempInfo);
        }
        info.setConnectCount(connectCount);
    }

    private Boolean isFileExist() {
        return info.getSaveFile().exists();
    }

    private ObservableSource<? extends DownloadType> getFileNoExistsType() {
        return Observable.just(getStartType());
    }

    private ObservableSource<? extends DownloadType> getFileExistsType() {
        return Observable.just(true)
                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(@NonNull Boolean aBoolean) throws
                            Exception {
                        return checkChange(url, info.getLastModify());
                    }
                })
                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(@NonNull Boolean aBoolean) throws
                            Exception {
                        return aBoolean ? checkChangeInfo(url, "bytes=0-") : Observable.just(false);
                    }
                })
                .flatMap(new Function<Boolean, ObservableSource<? extends DownloadType>>() {
                    @Override
                    public ObservableSource<? extends DownloadType> apply(@NonNull Boolean
                                                                                  aBoolean)
                            throws Exception {
                        return aBoolean ?
                                Observable.just(getRestartType()) :
                                Observable.just(getCompletedType());
                    }
                });

    }

    private ObservableSource<Boolean> checkChange(final String url, String lastModify) {
        return server.checkChange(url, lastModify)
                .map(new Function<Response<Void>, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Response<Void> voidResponse) throws Exception {
                        return saveIsChange(voidResponse);
                    }
                })
                .compose(CURx.<Boolean>retryObservable(maxRetryCount));
    }


    private boolean saveIsChange(Response response) {
        if (response.code() == 304 || getLastModify(response).equals(info.getLastModify())) {
            info.setChange(false);
            return false;
        } else {
            info.setChange(true);
            return true;
        }
    }

    private ObservableSource<Boolean> checkChangeInfo(String url, String range) {
        return server.checkRange(url, range)
                .map(new Function<Response<Void>, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull Response<Void> voidResponse) throws Exception {
                        return saveChangeInfo(voidResponse);
                    }
                })
                .compose(CURx.<Boolean>retryObservable(maxRetryCount));
    }

    private boolean saveChangeInfo(Response response) {
        if (response.isSuccessful()) {
            setInfoContentLength(response);
            setInfoLastModify(response);
            setInfoRange(response);
            updateTempInfoList();
            info.setCreateTime(CUTime.getCurrentTimeLong());
            return true;
        } else {
            return false;
        }
    }

    private void updateTempInfoList() {
        info.clearTempInfo();
        setTempInfoList();
    }

    //####################　DOWNLOADTYPE选择　####################
    private DownloadType getCompletedType() {
        return new DownloadCompletedType(this);
    }

    private DownloadType getNormalType() {
        return new DownloadNormalType(this);
    }

    private DownloadType getRangType() {
        return new DownloadRangeType(this);
    }

    private DownloadType getContinueType() {
        return new DownloadContinueType(this);
    }

    private DownloadType getStartType() {
        DownloadType type;
        if (info.isRange()) {
            if (info.isExistTempFiles())
                type = getContinueType();
            else
                type = getRangType();
        } else {
            delTempFile();
            type = getNormalType();
        }
        return type;
    }

    private DownloadType getRestartType() {
        info.delAllFiles();
        return getStartType();
    }

    //####################　DOWNLOAD执行　####################
    public Flowable<Response<ResponseBody>> download() {
        return server.download(url);
    }

    public void save(FlowableEmitter<DownloadProgress> e, ResponseBody responseBody) {
        DownloadFileHelper.getInstance().saveFile(this, e, responseBody);
    }

    public Flowable<Response<ResponseBody>> download(final int id) {
        DownloadTempInfo tempInfo = info.getInfos().get(id);
        String range = "bytes=" + tempInfo.getPosition() + "-" + tempInfo.getEnd();
        return server.download(url, range);
    }

    public void save(int id, FlowableEmitter<DownloadProgress> e, ResponseBody responseBody) {
        DownloadFileHelper.getInstance().saveFile(this, id, e, responseBody);
    }

    public <U> ObservableTransformer<U, U> getRetryObservable() {
        return CURx.retryObservable(maxRetryCount);
    }

    public <U> FlowableTransformer<U, U> getRetryFlowable() {
        return CURx.retryFlowable(maxRetryCount);
    }

    //####################　STATUS变更　####################
    public void start() {
        info.setStatus(DownloadStatus.STARTED);
    }

    public void update(DownloadProgress progress) {
        setDownloadProgress(progress);
    }

    public void error() {
        info.setStatus(DownloadStatus.FAILED);
    }

    public void complete() {
        info.setStatus(DownloadStatus.COMPLETED);
    }

    public void cancel() {
        info.setStatus(DownloadStatus.CANCELED);
    }

    public void finish() {
        DataStorage.storeOrUpdate(info);
    }


    //####################　HEADER判断　####################
    private String getAcceptRanges(Response response) {
        return response.headers().get("Accept-Ranges");
    }

    private String getContentDisposition(Response response) {
        String disposition = response.headers().get("Content-Disposition");
        if (!CUCheck.cString(disposition))
            return "";

        Matcher m = Pattern.compile(".*filename=(.*)").matcher(disposition.toLowerCase());
        return m.find() ? m.group(1) : "";
    }

    private String getContentName(String url, Response response) {
        String contentName = getContentDisposition(response);
        if (!CUCheck.cString(contentName))
            contentName = url.substring(url.lastIndexOf('/') + 1);

        if (contentName.startsWith("\""))
            contentName = contentName.substring(1);
        if (contentName.endsWith("\""))
            contentName = contentName.substring(0, contentName.length() - 1);
        return contentName;
    }

    private long getContentLength(Response response) {
        return HttpHeaders.contentLength(response.headers());
    }

    private String getContentRange(Response response) {
        return response.headers().get("Content-Range");
    }

    private String getLastModify(Response response) {
        return response.headers().get("Last-Modified");
    }

    private String getTransferEncoding(Response response) {
        return response.headers().get("Transfer-Encoding");
    }

    private boolean isChunked(Response response) {
        return CUCheck.cString(getTransferEncoding(response))
                && getTransferEncoding(response).equals("chunked");
    }

    //####################　INFO操作　####################
    public DownloadInfo getDownloadInfo() {
        return info;
    }

    public long getInfoTotalSize() {
        return info.getTotalSize();
    }

    public long getInfoCurrentSize() {
        return info.getCurrentSize();
    }

    public String getSavaPath() {
        return info.getSavePath();
    }

    public String getTempPath() {
        return info.getTempPath();
    }

    public File getSavaFile() {
        return new File(getSavaPath());
    }

    public File getTempFile() {
        return new File(getTempPath());
    }

    public boolean delSaveFile() {
        return info.delSaveFile();
    }

    public boolean delTempFile() {
        return info.delTempFile();
    }

    public boolean delAllFiles() {
        return info.delAllFiles();
    }

    public String getUrl() {
        return url;
    }

    public boolean isMission() {
        return info.isMission();
    }

    public DownloadProgress getDownloadProgress() {
        return info.getProgress();
    }

    public void setDownloadProgress(DownloadProgress progress) {
        this.info.setProgress(progress);
    }

    //####################　TEMPINFO操作　####################
    public ArrayList<DownloadTempInfo> getDownloadTempInfos() {
        info.flushTempInfo();
        return info.getInfos();
    }

    public DownloadTempInfo getDownloadTempInfo(int id) {
        info.flushTempInfo();
        int index = id < info.getInfos().size() ? id : 0;
        return info.getInfos().get(index);
    }

    public long getTempStart(int id) {
        return getDownloadTempInfo(id).getStart();
    }

    public long getTempPosition(int id) {
        return getDownloadTempInfo(id).getPosition();
    }

    public void setTempPosition(int id, long position) {
        getDownloadTempInfo(id).setPosition(position);
    }
}
