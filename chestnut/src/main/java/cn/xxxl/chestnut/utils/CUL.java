package cn.xxxl.chestnut.utils;

import android.util.Log;

/**
 * @author Leon
 * @since 1.0.0
 */
public class CUL {
    private static final String Tag = "Chestnut";

    public static void e(String msg) {
        Log.e(Tag, msg);
    }

    public static void e(Object o) {
        Log.e(Tag, o.toString());
    }

    public static void w(String msg) {
        Log.w(Tag, msg);
    }

    public static void w(Object o) {
        Log.w(Tag, o.toString());
    }

    public static void i(String msg) {
        Log.i(Tag, msg);
    }

    public static void i(Object o) {
        Log.i(Tag, o.toString());
    }

    public static void d(String msg) {
        Log.d(Tag, msg);
    }

    public static void d(Object o) {
        Log.d(Tag, o.toString());
    }

    public static void v(String msg) {
        Log.v(Tag, msg);
    }

    public static void v(Object o) {
        Log.v(Tag, o.toString());
    }
}
