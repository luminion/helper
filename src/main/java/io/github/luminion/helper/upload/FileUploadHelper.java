package io.github.luminion.helper.upload;

/**
 * 文件上传静态入口。
 * <p>
 * 只负责创建上传执行器，没有独立职责。
 * 直接使用 {@link ChunkFileUploader#of(String)} 即可。
 *
 * @author luminion
 * @deprecated 请直接使用 {@link ChunkFileUploader}
 */
@Deprecated
public abstract class FileUploadHelper {
    private FileUploadHelper() {}

    /**
     * 创建分片上传执行器。
     */
    public static ChunkFileUploader of(String chunkUploadPath) {
        return ChunkFileUploader.of(chunkUploadPath);
    }
}
