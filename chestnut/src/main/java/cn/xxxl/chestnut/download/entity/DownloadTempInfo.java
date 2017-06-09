package cn.xxxl.chestnut.download.entity;

import android.support.annotation.NonNull;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-23.
 */
public class DownloadTempInfo implements Comparable<DownloadTempInfo> {

    private String url;
    private int id;

    private long start;
    private long end;
    private long size;
    private long position;

    public DownloadTempInfo(String url, int id) {
        this.url = url;
        this.id = id;
    }

    //################################################
    public String getUrl() {
        return url;
    }

    public int getId() {
        return id;
    }

    public long getRemainSize() {
        return end - position + 1;
    }

    @Override
    public int compareTo(@NonNull DownloadTempInfo anotherInfo) {
        int x = getId();
        int y = anotherInfo.getId();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    //################################################
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
        this.position = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Temp{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
