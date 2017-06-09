package cn.xxxl.chestnut.utils;

import android.content.Context;

import java.util.Comparator;
import java.util.List;

import xiaofei.library.datastorage.DataStorageFactory;
import xiaofei.library.datastorage.IDataStorage;
import xiaofei.library.datastorage.util.Condition;


/**
 * Chestnut 数据存储工具类
 *
 * @author Leon
 * @since 1.0.0
 */
public class DataStorage {

    private static IDataStorage dataStorage;


    public static void init(Context context) {
        dataStorage = DataStorageFactory.getInstance(context, DataStorageFactory.TYPE_DATABASE);
    }

    private static void assertInitialized() {
        if (dataStorage == null)
            throw new IllegalStateException("Please initialize DataStorage!");
    }

    public static <T> boolean contains(Class<T> clazz, String id) {
        assertInitialized();
        return dataStorage.contains(clazz, id);
    }

    public static <T> boolean contains(T element) {
        assertInitialized();
        return dataStorage.contains(element);
    }

    public static <T> void storeOrUpdate(T element, String id) {
        assertInitialized();
        dataStorage.storeOrUpdate(element, id);
    }

    public static <T> void storeOrUpdate(T element) {
        assertInitialized();
        dataStorage.storeOrUpdate(element);
    }

    public static <T> void storeOrUpdate(List<T> list, List<String> ids) {
        assertInitialized();
        dataStorage.storeOrUpdate(list, ids);
    }

    public static <T> void storeOrUpdate(List<T> list) {
        assertInitialized();
        dataStorage.storeOrUpdate(list);
    }

    public static <T> T load(Class<T> clazz, String id) {
        assertInitialized();
        return dataStorage.load(clazz, id);
    }

    public static <T> List<T> load(Class<T> clazz, List<String> ids) {
        assertInitialized();
        return dataStorage.load(clazz, ids);
    }

    public static <T> List<T> load(Class<T> clazz, List<String> ids, Comparator<T> comparator) {
        assertInitialized();
        return dataStorage.load(clazz, ids, comparator);
    }

    public static <T> List<T> load(Class<T> clazz, Condition<T> condition) {
        assertInitialized();
        return dataStorage.load(clazz, condition);
    }

    public static <T> List<T> load(Class<T> clazz, Condition<T> condition, Comparator<T>
            comparator) {
        assertInitialized();
        return dataStorage.load(clazz, condition, comparator);
    }

    public static <T> List<T> loadAll(Class<T> clazz) {
        assertInitialized();
        return dataStorage.loadAll(clazz);
    }

    public static <T> List<T> loadAll(Class<T> clazz, Comparator<T> comparator) {
        assertInitialized();
        return dataStorage.loadAll(clazz, comparator);
    }

    public static <T> void delete(T element) {
        assertInitialized();
        dataStorage.delete(element);
    }

    public static <T> void delete(Class<T> clazz, String id) {
        assertInitialized();
        dataStorage.delete(clazz, id);
    }

    public static <T> void delete(Class<T> clazz, List<String> ids) {
        assertInitialized();
        dataStorage.delete(clazz, ids);
    }

    public static <T> void delete(List<T> list) {
        assertInitialized();
        dataStorage.delete(list);
    }

    public static <T> void delete(Class<T> clazz, Condition<T> condition) {
        assertInitialized();
        dataStorage.delete(clazz, condition);
    }

    public static <T> void deleteAll(Class<T> clazz) {
        assertInitialized();
        dataStorage.deleteAll(clazz);
    }

    public static void clear() {
        assertInitialized();
        dataStorage.clear();
    }
}
