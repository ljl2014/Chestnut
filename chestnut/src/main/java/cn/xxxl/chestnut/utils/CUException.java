package cn.xxxl.chestnut.utils;

/**
 * Chestnut 常用异常工具类
 *
 * @author Leon
 * @since 1.0.0
 */
public class CUException {

    /**
     * 判断对象是否为空
     *
     * @param t   对象
     * @param msg 异常描述
     * @param <T> 泛型
     * @return t
     */
    public static <T> T cNull(T t, String msg) {
        if (t == null)
            throw new NullPointerException(msg);
        return t;
    }

    /**
     * 判断对象是否为空
     * msg = t.getClass().getSimpleName() + " == null";
     *
     * @param t   对象
     * @param <T> 泛型
     * @return t
     */
    public static <T> T cNull(T t) {
        return cNull(t, t.getClass().getSimpleName() + " == null");
    }


    public static void cNum(long small, long big, String msg) {
        if (big <= small)
            throw new IllegalStateException(msg);
    }

    public static void cNum(long small, long big) {
        cNum(small, big, "Num is wrong");
    }
}
