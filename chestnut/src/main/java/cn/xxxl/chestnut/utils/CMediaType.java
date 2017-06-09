package cn.xxxl.chestnut.utils;

import okhttp3.MediaType;

/**
 * Chestnut 常用MediaType
 *
 * @author Leon
 * @since 1.0.0
 */
public class CMediaType {

    public static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");

    public static final MediaType JPEG = MediaType.parse("image/jpeg;");
    public static final MediaType PNG = MediaType.parse("image/png;");

    public static final MediaType AUDIO = MediaType.parse("audio/*;");
    public static final MediaType IMAGE = MediaType.parse("image/*;");
    public static final MediaType TEXT = MediaType.parse("text/*; charset=UTF-8");
    public static final MediaType VIDEO = MediaType.parse("video/*;");
}
