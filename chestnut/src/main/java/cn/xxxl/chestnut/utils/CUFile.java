package cn.xxxl.chestnut.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-17.
 */
public class CUFile {

    public static List<File> mkdirs(String... paths) {
        List<File> files = new ArrayList<>(paths.length);
        for (String each : paths)
            files.add(mkdirs(each));
        return files;
    }

    public static File mkdirs(String path) {
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static void deleteFiles(File... files) {
        for (File each : files)
            if (each.exists())
                each.delete();
    }
}
