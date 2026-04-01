package io.github.luminion.helper.io;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.upload.FileUploadHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.upload.FileUploadHelper}
 */
@Deprecated
public class FileUploadHelper extends io.github.luminion.helper.upload.ChunkFileUploader {

    public FileUploadHelper(String chunkUploadPath) {
        super(chunkUploadPath);
    }
}
