package cn.xxxl.chestnut.upload;

import java.io.File;

import okhttp3.MediaType;

/**
 * 上传文件建议使用该类
 *
 * @author Leon
 * @since 1.0.0
 */
public class UploadFile {

    private MediaType type;
    private File file;
    private String name;


    public UploadFile(MediaType type, File file) {
        this(type, file, file.getName());
    }

    public UploadFile(MediaType type, File file, String name) {
        this.type = type;
        this.file = file;
        this.name = name;
    }

    public MediaType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }
}
