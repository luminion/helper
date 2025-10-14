package io.github.bootystar.helper.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * 文件操作工具
 *
 * @author bootystar
 */
@Slf4j
public abstract class FileHelper {


    /**
     * 删除目标文件夹内及其子文件夹所有指定文件
     *
     * @param source  目标文件夹
     * @param pattern 需要删除的文件, 匹配的正则表达式
     */
    public static void deleteFileByPattern(File source, String pattern) {
        if (source == null || pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("源文件或目标不合法");
        }
        if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFileByPattern(child, pattern);
                }
            }
        } else {
            String name = source.getName();
            if (name.matches(pattern)) {
                log.debug("删除文件：{}", source.getAbsolutePath());
                source.delete();
            }
        }
    }

    /**
     * 删除目标文件及文件夹内所有文件
     *
     * @param target 目标文件
     */
    public static void deleteFile(File target) {
        if (target == null) {
            return;
        }
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFile(child);
                }
            }
        } else {
            target.delete();
        }
    }

    /**
     * 复制文件
     *
     * @param source 源文件
     * @param dest   输出文件
     */
    @SneakyThrows
    private static void copyFile(File source, File dest) {
        if (source == null || source.isDirectory() || dest == null || dest.isDirectory()) {
            throw new IllegalArgumentException("源文件或目标不合法");
        }
        try (
                FileInputStream is = new FileInputStream(source);
                FileOutputStream os = new FileOutputStream(dest);
                FileChannel inputChannel = is.getChannel();
                FileChannel outputChannel = os.getChannel()
        ) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    /**
     * 复制文件夹
     *
     * @param sourceDir 源文件夹
     * @param destDir   输出文件夹
     */
    public static void copyDir(File sourceDir, File destDir) {
        if (sourceDir == null || !sourceDir.isDirectory() || destDir == null || !destDir.isDirectory()) {
            throw new IllegalArgumentException("源文件或目标不合法");
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File f : files) {
                File file = new File(destDir.getPath(), f.getName());
                if (f.isDirectory()) {
                    copyDir(f, file);
                } else if (f.isFile()) {
                    copyFile(f, file);
                }
            }
        }

    }

}