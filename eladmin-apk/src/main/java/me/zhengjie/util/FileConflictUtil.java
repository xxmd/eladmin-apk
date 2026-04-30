package me.zhengjie.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FileConflictUtil {

    /**
     * 删除 destDir 中与 srcDir “同名(不含后缀)”冲突的文件
     */
    public static void deleteSameNameFiles(File srcDir, File destDir) throws IOException {

        if (srcDir == null || destDir == null) return;
        if (!srcDir.exists() || !destDir.exists()) return;

        // 1. 收集 srcDir 所有“文件名（去后缀）”
        Set<String> srcBaseNames = new HashSet<>();
        collectBaseNames(srcDir, srcBaseNames);

        // 2. 遍历 destDir 删除冲突文件
        deleteConflictFiles(destDir, srcBaseNames);
    }

    /**
     * 收集 src 中所有文件的 baseName（不含扩展名）
     */
    private static void collectBaseNames(File dir, Set<String> set) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                collectBaseNames(f, set);
            } else {
                String name = f.getName();
                int dot = name.lastIndexOf('.');
                String base = (dot > 0) ? name.substring(0, dot) : name;
                set.add(base);
            }
        }
    }

    /**
     * 删除 destDir 中冲突文件（同 baseName）
     */
    private static void deleteConflictFiles(File dir, Set<String> srcBaseNames) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {

            if (f.isDirectory()) {
                deleteConflictFiles(f, srcBaseNames);
            } else {
                String name = f.getName();
                int dot = name.lastIndexOf('.');
                String base = (dot > 0) ? name.substring(0, dot) : name;

                if (srcBaseNames.contains(base)) {
                    f.delete();
                }
            }
        }
    }
}