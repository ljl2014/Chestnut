package cn.xxxl.chestnut.download.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import cn.xxxl.chestnut.download.entity.DownloadInfo;
import cn.xxxl.chestnut.download.entity.DownloadProgress;
import cn.xxxl.chestnut.download.entity.DownloadStatus;
import cn.xxxl.chestnut.download.entity.DownloadTempInfo;
import cn.xxxl.chestnut.utils.CUCheck;
import cn.xxxl.chestnut.utils.CUL;
import cn.xxxl.chestnut.utils.CUTime;
import io.reactivex.FlowableEmitter;
import okhttp3.ResponseBody;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static okhttp3.internal.Util.closeQuietly;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-22.
 */
public class DownloadFileHelper {

    private volatile static DownloadFileHelper INSTANCE;

    private static final int byteSize = 4 * 1024;

    public static DownloadFileHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (DownloadFileHelper.class) {
                if (INSTANCE == null)
                    INSTANCE = new DownloadFileHelper();
            }
        }
        return INSTANCE;
    }

    public void saveFile(DownloadTask task, FlowableEmitter<DownloadProgress> emitter,
                         ResponseBody responseBody) {

        DownloadProgress progress = task.getDownloadProgress();

        RandomAccessFile tempFile = null;
        FileChannel tempChannel = null;
        InputStream is = null;

        try {
            int len;
            byte[] bytes = new byte[byteSize];
            long current = 0;
            is = responseBody.byteStream();

            tempFile = new RandomAccessFile(task.getTempFile(), "rw");
            tempChannel = tempFile.getChannel();

            while (!emitter.isCancelled() && (len = is.read(bytes)) != -1) {
                MappedByteBuffer buffer = tempChannel.map(READ_WRITE, current, len);
                current += len;
                buffer.put(bytes, 0, len);
                progress.setStatus(DownloadStatus.STARTED);
                progress.setCurrentSize(current);
                emitter.onNext(progress);
            }
            if (task.getTempFile().renameTo(task.getSavaFile()))
                task.delTempFile();
            else
                emitter.onError(new IllegalStateException("文件转换失败！"));

            emitter.onComplete();
        } catch (IOException e) {
            emitter.onError(e);
        } finally {
            closeQuietly(tempFile);
            closeQuietly(tempChannel);
            closeQuietly(is);
        }
    }

    public void saveFile(DownloadTask task, int id, FlowableEmitter<DownloadProgress> emitter,
                         ResponseBody responseBody) {

        long total = task.getInfoTotalSize();
        DownloadProgress progress = task.getDownloadProgress();

        RandomAccessFile tempFile = null;
        FileChannel tempChannel = null;
        InputStream is = null;
        try {
            int len;
            byte[] bytes = new byte[byteSize];
            long current = task.getTempPosition(id);
            long CompletedSize = 0;

            is = responseBody.byteStream();

            tempFile = new RandomAccessFile(task.getTempPath(), "rw");
            tempFile.setLength(task.getInfoTotalSize());//important!
            tempChannel = tempFile.getChannel();

            while (!emitter.isCancelled() && (len = is.read(bytes)) != -1) {
                MappedByteBuffer buffer = tempChannel.map(READ_WRITE, current, len);
                current += len;
                buffer.put(bytes, 0, len);
                task.setTempPosition(id, current);

                CompletedSize = total - getRemainSize(task.getDownloadInfo());
                progress.setStatus(DownloadStatus.STARTED);
                progress.setCurrentSize(CompletedSize);
                emitter.onNext(progress);
            }
            if (CompletedSize == total)
                if (task.getTempFile().renameTo(task.getSavaFile()))
                    task.delTempFile();
                else
                    emitter.onError(new IllegalStateException("文件转换失败！"));

            emitter.onComplete();
        } catch (IOException e) {
            emitter.onError(e);
        } finally {
            closeQuietly(tempFile);
            closeQuietly(tempChannel);
            closeQuietly(is);
        }
    }

    @Deprecated
    private boolean transferFiles(FlowableEmitter<DownloadProgress> emitter,
                                  String savePath, String... tempPaths) {

        long start = CUTime.getCurrentTimeLong();
        if (!CUCheck.cString(savePath)) {
            return false;
        }
        if (tempPaths.length == 1) {
            return new File(tempPaths[0]).renameTo(new File(savePath));
        }

        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        try {
            File saveFile = new File(savePath);
            File[] tempFiles = new File[tempPaths.length];
            for (int i = 0; i < tempPaths.length; i++)
                tempFiles[i] = new File(tempPaths[i]);

            int len;
            byte[] bytes = new byte[byteSize];

            outputStream = new BufferedOutputStream(new FileOutputStream(saveFile));

            for (int i = 0; i < tempPaths.length; i++) {
                inputStream = new BufferedInputStream(new FileInputStream(tempFiles[i]));
                while (!emitter.isCancelled() && (len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(outputStream);
            closeQuietly(inputStream);
            long end = CUTime.getCurrentTimeLong();
            CUL.e(end - start);
        }
    }

    private long getRemainSize(DownloadInfo info) {
        long RemainSize = 0;
        for (DownloadTempInfo tempInfo : info.getInfos())
            RemainSize += tempInfo.getRemainSize();
        return RemainSize;
    }

}
