package io.github.luminion.helper.upload;

import io.github.luminion.helper.io.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分片上传执行器
 *
 * @author luminion
 */
@Slf4j
public class ChunkFileUploader {

    /**
     * temp dir路径
     */
    private final String chunkUploadPath;

    public ChunkFileUploader(String chunkUploadPath) {
        this.chunkUploadPath = chunkUploadPath;
    }

    public static ChunkFileUploader of(String chunkUploadPath) {
        return new ChunkFileUploader(chunkUploadPath);
    }

    /**
     * 验证文件是否存在【用于秒传】
     *
     * @param fileMD5 文件md5
     * @param fileExt 文件扩展名
     * @return boolean
     */
    public boolean verifyFileExists(String fileMD5, String fileExt) {
        return Files.exists(resolveMergePath(fileMD5, fileExt));
    }

    /**
     * 获取已上传的分片数量【用于初始化上传进度】
     *
     * @param fileMD5 文件md5
     * @return int
     */
    public int getChunkUploadCount(String fileMD5) {
        File uploadChunkDir = resolveChunkDir(fileMD5).toFile();
        if (!uploadChunkDir.exists()) {
            return 0;
        }

        File[] files = uploadChunkDir.listFiles();
        return files == null ? 0 : files.length;
    }

    /**
     * 获取已上传的分片下标【用于分片上传前验证】
     *
     * @param fileMD5         文件md5
     * @param fileChunkLength 文件大小
     * @return 列表 <整数>
     */
    public List<Integer> getChunkUploadIndex(String fileMD5, int fileChunkLength) {
        File uploadChunkDir = resolveChunkDir(fileMD5).toFile();

        ArrayList<Integer> chunkIndexArray = new ArrayList<>();
        if (!uploadChunkDir.exists()) {
            chunkIndexArray.add(-1);
            return chunkIndexArray;
        }

        File[] files = uploadChunkDir.listFiles();
        if (files == null || files.length == 0) {
            chunkIndexArray.add(-1);
            return chunkIndexArray;
        }

        for (File file : files) {
            if (file.length() != fileChunkLength) {
                file.delete();
                continue;
            }
            String[] split = file.getName().split("\\.");
            if (split.length == 0) {
                continue;
            }
            try {
                chunkIndexArray.add(Integer.valueOf(split[0]));
            } catch (NumberFormatException ignore) {
                log.warn("非法分片文件名: {}", file.getAbsolutePath());
            }
        }
        if (chunkIndexArray.isEmpty()) {
            chunkIndexArray.add(-1);
        }
        return chunkIndexArray;
    }

    /**
     * 实时验证分片是否已经上传【不推荐】
     *
     * @param fileMD5   文件MD5值
     * @param chunk     分片下标
     * @param chunkSize 分片大小
     * @return boolean
     */
    public boolean verifyChunk(String fileMD5, String chunk, int chunkSize) {
        File file = resolveChunkDir(fileMD5).resolve(chunk + ".part").toFile();
        if (!file.exists()) {
            return false;
        }

        if (file.length() != chunkSize) {
            file.delete();
            return false;
        }
        return true;
    }

    /**
     * 分片上传
     * 【分片上传的目录必须带上MD5的路径，用于检测分片是否已经上传】
     *
     * @param inputStream 上传的分片
     * @param fileMD5     上传文件的MD5值
     * @param chunk       分片下标
     * @return boolean
     */
    @SneakyThrows
    public boolean uploadChunk(InputStream inputStream, String fileMD5, String chunk) {
        Path chunkDir = resolveChunkDir(fileMD5);
        if (!Files.exists(chunkDir)) {
            Files.createDirectories(chunkDir);
        }
        Path dist = chunkDir.resolve(chunk + ".part");
        try (InputStream actualInputStream = inputStream) {
            Files.copy(actualInputStream, dist, StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    /**
     * 合并分片
     *
     * @param fileMD5    文件MD5值
     * @param fileExt    文件名称
     * @param chunkCount 分片总数量
     * @return boolean
     */
    @SneakyThrows
    public boolean mergeFile(String fileMD5, String fileExt, int chunkCount) {
        Path chunkDir = resolveChunkDir(fileMD5);
        Path outputPath = resolveMergePath(fileMD5, fileExt);

        if (!Files.isDirectory(chunkDir)) {
            return false;
        }

        File[] files = chunkDir.toFile().listFiles();
        if (files == null || files.length == 0 || files.length != chunkCount) {
            return false;
        }

        for (int i = 0; i < chunkCount; i++) {
            if (!Files.isRegularFile(chunkDir.resolve(i + ".part"))) {
                return false;
            }
        }

        try (OutputStream outputStream = Files.newOutputStream(outputPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (int i = 0; i < chunkCount; i++) {
                try (InputStream chunkInputStream = Files.newInputStream(chunkDir.resolve(i + ".part"))) {
                    copy(chunkInputStream, outputStream);
                }
            }
            FileHelper.deleteFile(chunkDir.toFile());
            return true;
        } catch (Exception e) {
            log.info("分片合并异常:", e);
            FileHelper.deleteFile(outputPath.toFile());
            return false;
        }
    }

    /**
     * 获取合并文件的输出流
     *
     * @param fileMD5    文件MD5值
     * @param fileExt    文件名称
     * @param chunkCount 分片总数量
     * @return boolean
     */
    @SneakyThrows
    public OutputStream getMergeFileOutputStream(String fileMD5, String fileExt, int chunkCount) {
        Path outputPath = resolveMergePath(fileMD5, fileExt);
        Path parent = outputPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        return Files.newOutputStream(outputPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /**
     * 取消文件上传
     *
     * @param fileMD5 文件MD5值
     * @return boolean
     */
    @SneakyThrows
    public boolean cancel(String fileMD5) {
        Files.delete(resolveChunkDir(fileMD5));
        return true;
    }

    private Path resolveChunkDir(String fileMD5) {
        return Paths.get(chunkUploadPath, fileMD5);
    }

    private Path resolveMergePath(String fileMD5, String fileExt) {
        String extension = normalizeExtension(fileExt);
        String filename = extension == null ? fileMD5 : fileMD5 + "." + extension;
        return Paths.get(chunkUploadPath, filename);
    }

    private String normalizeExtension(String fileExt) {
        if (fileExt == null) {
            return null;
        }
        String extension = fileExt.trim();
        while (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension.isEmpty() ? null : extension;
    }

    private void copy(InputStream inputStream, OutputStream outputStream) throws Exception {
        byte[] buffer = new byte[16 * 1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
    }
}
