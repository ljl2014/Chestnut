package cn.xxxl.chestnut.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-17.
 */
public class CUFormat {

    public final static long KB = 1024;
    public final static long MB = KB * 1024;
    public final static long GB = MB * 1024;
    public final static long TB = GB * 1024;

    public static String formatStr(String str, Object... args) {
        return String.format(Locale.getDefault(), str, args);
    }

    public static String formatSize(long size) {
        String cSize;
        double b = size;
        double k = b / 1024.0;
        double m = k / 1024.0;
        double g = m / 1024.0;
        double t = g / 1024.0;
        DecimalFormat dec = new DecimalFormat("0.00");
        if (t > 1)
            cSize = dec.format(t).concat(" TB");
        else if (g > 1)
            cSize = dec.format(g).concat(" GB");
        else if (m > 1)
            cSize = dec.format(m).concat(" MB");
        else if (k > 1)
            cSize = dec.format(k).concat(" KB");
        else
            cSize = dec.format(b).concat(" B");
        return cSize;
    }

    public static String concat(CharSequence... text) {
        return TextUtils.concat(text).toString();
    }

    public static String urlExists(String url) {
        return CUFormat.formatStr("ChestnutDownload: Task [%s] is exists.", url);
    }

    public static String urlExistsOnService(String url) {
        return CUFormat.formatStr("ChestnutDownload: Task [%s] is exists on service.", url);
    }

    public static String urlDel(String url) {
        return CUFormat.formatStr("ChestnutDownload: Task [%s] is deleted.", url);
    }

    public static String urlIllegal(String url) {
        return CUFormat.formatStr("ChestnutDownload: Task [%s] is illegal.", url);
    }

    public static String urlInitFailed(String url) {
        return CUFormat.formatStr("ChestnutDownload: Task [%s] init failed.", url);
    }
}
