package cn.xxxl.chestnut.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Chestnut 常用判断工具类
 *
 * @author Leon
 * @since 1.0.0
 */
public class CUCheck {

    @Deprecated
    public static boolean cObject(Object object) {
        return object != null;
    }

    public static boolean cObjects(Object[] objects) {
        return objects != null && objects.length != 0;
    }

    public static boolean cCollection(Collection collection) {
        return collection != null && collection.size() != 0;
    }

    public static boolean cMap(Map map) {
        return map != null && map.size() != 0;
    }

    public static boolean cString(String string) {
        return string != null && string.length() != 0;
    }
}
