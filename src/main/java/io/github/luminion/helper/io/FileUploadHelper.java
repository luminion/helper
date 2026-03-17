package io.github.luminion.helper.io;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文件上传帮助程序
 *
 * @author luminion
 * @date 2026-03-17
 */
@Slf4j
@RequiredArgsConstructor
public class FileUploadHelper {

    /**
     * temp dir路径
     */
    private final String chunkUploadPath;

    /**
     * 验证文件是否存在【用于秒传】
     *
     * @param fileMD5 文件md5
     * @param fileExt 文件扩展名
     * @return boolean
     */
    public boolean verifyFileExists(String fileMD5, String fileExt) {
        // 获取合并后的文件
        File uploadFile = new File(chunkUploadPath.concat(File.separator).concat(fileMD5).concat(".").concat(fileExt));

        // 验证文件是否已经上传，如果已经上传，返回true，通知客户端秒传成功
        return uploadFile.exists();
    }

    /**
     * 获取已上传的分片数量【用于初始化上传进度】
     *
     * @param fileMD5 文件md5
     * @return int
     */
    public int getChunkUploadCount(String fileMD5) {
        // 获取分片存储目录
        File uploadChunkDir = new File(chunkUploadPath.concat(File.separator).concat(fileMD5));

        // 验证是否上传过分片，如果目录都不存在，则返回0
        if (!uploadChunkDir.exists()) {
            return 0;
        }

        // 获取该目录下的所有分片
        File[] files = uploadChunkDir.listFiles();
        if (files == null) {
            return 0;
        }
        return files.length;
    }

    /**
     * 获取已上传的分片下标【用于分片上传前验证】
     *
     * @param fileMD5         文件md5
     * @param fileChunkLength 文件大小
     * @return 列表 <整数>
     */
    public List<Integer> getChunkUploadIndex(String fileMD5, int fileChunkLength) {
        // 获取分片存储目录
        File uploadChunkDir = new File(chunkUploadPath.concat(File.separator).concat(fileMD5));

        ArrayList<Integer> chunkIndexArray = new ArrayList<>();

        // 验证是否上传过分片，如果目录都不存在，则返回-1
        if (!uploadChunkDir.exists()) {
            chunkIndexArray.add(-1);
            return chunkIndexArray;
        }

        // 获取该目录下的所有分片
        File[] files = uploadChunkDir.listFiles();

        // 获取分片数量
        int fileLength = files.length;

        // 验证分片文件是否存在
        if (fileLength == 0) {
            chunkIndexArray.add(-1);
            return chunkIndexArray;
        }

        // 如果分片文件存在，则获取分片文件下标(chunk)
        for (File file : files) {
            // 如果分片大小不一致，则删除该分片，且不返回该分片索引
            if (file.length() != fileChunkLength) {
                file.delete();
            } else {
                chunkIndexArray.add(Integer.valueOf(file.getName().split("\\.")[0]));
            }
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
        // 获取上传的分片文件（f:\\{fileMD5}\\{chunk}.part）【part后缀为临时文件后缀】
        String uploadPath = chunkUploadPath.concat(File.separator).concat(fileMD5).concat(File.separator).concat(chunk).concat(".part");
        File file = new File(uploadPath);

        // 如果分片文件不存在，则返回false，客户端继续上传【正常来讲这里验证{fileMD5目录是否存在就可以了}】
        if (!file.exists()) {
            return false;
        }

        // 如果分片存在，验证存在的分片大小是否是要上传的分片的大小。
        // 如果不是，则证明该分片上次上传到一半就失败了，那么删除该分片，返回false，由客户端重新上传该分片【如果是最后一个分片也可能大小不一样，这个不用考虑】
        if (file.length() != Integer.valueOf(chunkSize)) {
            file.delete();// 删除该分片【delete只能删除文件或者空的文件夹】
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
        Path chunkDir = Paths.get(chunkUploadPath, fileMD5);

        if (!Files.exists(chunkDir)) {
            Files.createDirectories(chunkDir);
        }
        // 使用分片的下标chunk作为临时文件名称【.part后缀为临时文件】
        Path dist = chunkDir.resolve(chunk + ".part");
        Files.copy(inputStream, dist);
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
        // 获取目标文件输出流
        File outputFile = new File(chunkUploadPath.concat(File.separator).concat(fileMD5).concat(".").concat(fileExt));

        try (FileOutputStream outputStream = new FileOutputStream(outputFile, true)) {

            // 获取分片文件存储目录
            String uploadPath = chunkUploadPath.concat(File.separator).concat(fileMD5);
            File chunkDir = new File(uploadPath);
            if (!chunkDir.isDirectory()) {
                return false;
            }

            // 获取该目录下的所有分片文件
            File[] files = chunkDir.listFiles();
            if (Objects.isNull(files) || files.length == 0) {
                return false;
            }

            // 验证分片是否已经全部上传完毕
            if (files.length != Integer.valueOf(chunkCount)) {
                return false;
            }

            // 将分片文件合并到目标文件
            File chunkFile;
            for (int i = 0; i < files.length; i++) {
                chunkFile = new File(uploadPath.concat(File.separator).concat(String.valueOf(i)).concat(".part"));
                FileUtils.copyFile(chunkFile, outputStream);
            }

            // 清除分片文件【分片文件的清除不要放在合并的循环操作的copyFile后，当copyFile还没执行完可能就执行删除操作了，
            // 导致某个分片还没合并完成就已经被删除，最终导致合并失败，文件损坏（有可能是因为copyFIle是nio操作）】
            for (File file : files) {
                file.delete();
            }

            // 删除分片目录
            chunkDir.delete();

            // 清空数组
            files = null;

            return true;

        } catch (Exception e) {
            log.info("分片合并异常:", e);
            //关闭文件输入流，并删除目标文件，防止合并失败的情况下，下次重传直接秒传成功了
            outputFile.delete();
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
        Path outputPath = Paths.get(chunkUploadPath, fileMD5 + "." + fileExt);
        return Files.newOutputStream(outputPath);
    }

    /**
     * 取消文件上传
     *
     * @param fileMD5 文件MD5值
     * @return boolean
     */
    public boolean cancel(String fileMD5) {
        // 获取分片存储目录
        String uploadPath = chunkUploadPath.concat(File.separator).concat(fileMD5);
        File chunkDir = new File(uploadPath);

        // 获取目录下所有分片文件
        File[] files = chunkDir.listFiles();
        for (File file : files) {
            file.delete();
        }

        // 删除分片目录
        chunkDir.delete();
        return true;
    }
}
