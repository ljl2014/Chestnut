package cn.xxxl.chestnut.download.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.CUFormat;
import xiaofei.library.comparatorgenerator.Criterion;
import xiaofei.library.comparatorgenerator.Order;
import xiaofei.library.datastorage.annotation.ClassId;
import xiaofei.library.datastorage.annotation.ObjectId;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
@ClassId("$CD")
public class DownloadInfo {

    @ObjectId
    private final String url;
    private boolean isMission;

    private String saveName;
    private String savePath;
    private String tempName;
    private String tempPath;

    @Criterion(priority = 1, order = Order.DESCENDING)
    private long createTime;
    private long completeTime;

    private int connectCount;
    private String lastModify;

    private boolean isInit = false;
    private boolean isRange = false;
    private boolean isChange = false;

    private DownloadProgress progress;
    private ArrayList<DownloadTempInfo> infos;
    private Map<String, String> extraInfos;

    public DownloadInfo(String url) {
        this(url, null);
    }

    public DownloadInfo(String url, String saveName) {
        this(url, saveName, false);
    }

    public DownloadInfo(String url, String saveName, boolean isMission) {
        this.url = url;
        this.saveName = saveName;
        this.isMission = isMission;
        this.progress = new DownloadProgress();
    }

    //################################################
    public String getUrl() {
        return url;
    }

    public File getSaveFile() {
        return new File(savePath);
    }

    public File getTempFile() {
        return new File(tempPath);
    }

    public boolean delSaveFile() {
        return getSaveFile().delete();
    }

    public boolean delTempFile() {
        return !isExistTempFiles() || getTempFile().delete();
    }

    public boolean delAllFiles() {
        return delSaveFile() && delTempFile();
    }

    public boolean isExistTempFiles() {
        return getTempFile().exists();
    }

    public void addTempInfo(DownloadTempInfo tempInfo) {
        if (infos == null)
            infos = new ArrayList<>();
        infos.add(tempInfo);
    }

    public void flushTempInfo() {
        if (CUCheck.cCollection(infos))
            Collections.sort(infos, new Comparator<DownloadTempInfo>() {
                @Override
                public int compare(DownloadTempInfo o1, DownloadTempInfo o2) {
                    return o1.compareTo(o2);
                }
            });
    }

    public void delTempInfo(DownloadTempInfo tempInfo) {
        if (infos != null && infos.contains(tempInfo))
            infos.remove(tempInfo);
    }

    public void clearTempInfo() {
        if (infos != null) {
            infos.clear();
            infos = null;
        }
    }

    //################################################
    public boolean isMission() {
        return isMission;
    }

    public void setMission(boolean mission) {
        isMission = mission;
    }

    public String getSaveName() {
        return CUCheck.cString(saveName) ? saveName : "";
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
        this.tempName = CUFormat.concat(saveName.lastIndexOf('.') == -1 ? saveName :
                saveName.substring(0, saveName.lastIndexOf('.')), ".temp");
    }

    public String getTempName() {
        return CUCheck.cString(tempName) ? tempName : "";
    }

    public String getSavePath() {
        return CUCheck.cString(savePath) ? savePath : "";
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getTempPath() {
        return CUCheck.cString(tempPath) ? tempPath : "";
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    public long getTotalSize() {
        return progress.getTotalSize();
    }

    public void setTotalSize(long totalSize) {
        this.progress.setTotalSize(totalSize);
    }

    public long getCurrentSize() {
        return progress.getCurrentSize();
    }

    public void setCurrentSize(long currentSize) {
        this.progress.setCurrentSize(currentSize);
    }

    public String getLastModify() {
        return lastModify;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public boolean isRange() {
        return isRange;
    }

    public void setRange(boolean range) {
        isRange = range;
    }

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    public DownloadStatus getStatus() {
        return progress.getStatus();
    }

    public void setStatus(DownloadStatus status) {
        this.progress.setStatus(status);
    }

    public DownloadProgress getProgress() {
        return progress;
    }

    public void setProgress(DownloadProgress progress) {
        this.progress = progress;
    }

    public ArrayList<DownloadTempInfo> getInfos() {
        if (infos == null)
            infos = new ArrayList<>();
        return infos;
    }

    public void setInfos(ArrayList<DownloadTempInfo> infos) {
        this.infos = infos;
    }

    public Map<String, String> getExtraInfos() {
        return extraInfos;
    }

    public void setExtraInfos(Map<String, String> extraInfos) {
        this.extraInfos = extraInfos;
    }
}
