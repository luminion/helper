package io.github.luminion.helper.io;

import io.github.luminion.helper.storage.minio.MinioStorageClient;
import io.github.luminion.helper.storage.minio.MinioUploadResult;
import io.minio.MinioClient;

import java.io.InputStream;

/**
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.storage.minio.MinioHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.storage.minio.MinioHelper}
 */
@Deprecated
public class MinioHelper extends MinioStorageClient {

    public MinioHelper(MinioClient minioClient) {
        super(minioClient);
    }

    public MinioHelper(String endpoint, String accessKey, String secretKey) {
        super(endpoint, accessKey, secretKey);
    }

    public MinioHelper(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        super(endpoint, accessKey, secretKey, defaultBucket);
    }

    public static MinioHelper of(MinioClient minioClient) {
        return new MinioHelper(minioClient);
    }

    public static MinioHelper of(String endpoint, String accessKey, String secretKey) {
        return new MinioHelper(endpoint, accessKey, secretKey);
    }

    public static MinioHelper of(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        return new MinioHelper(endpoint, accessKey, secretKey, defaultBucket);
    }

    /**
     * @deprecated 请使用 {@link #of(MinioClient)}
     */
    @Deprecated
    public static MinioHelper from(MinioClient minioClient) {
        return of(minioClient);
    }

    /**
     * @deprecated 请使用 {@link #of(String, String, String)}
     */
    @Deprecated
    public static MinioHelper create(String endpoint, String accessKey, String secretKey) {
        return of(endpoint, accessKey, secretKey);
    }

    /**
     * @deprecated 请使用 {@link #of(String, String, String, String)}
     */
    @Deprecated
    public static MinioHelper create(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        return of(endpoint, accessKey, secretKey, defaultBucket);
    }

    @Override
    public UploadResult uploadAndGetResult(String bucketName, String filename, InputStream is) {
        return convert(super.uploadAndGetResult(bucketName, filename, is));
    }

    @Override
    public UploadResult uploadAndGetResult(String filename, InputStream is) {
        return convert(super.uploadAndGetResult(filename, is));
    }

    @Override
    public UploadResult uploadAndGetResult(String bucketName, String filename, InputStream is, String fileExt) {
        return convert(super.uploadAndGetResult(bucketName, filename, is, fileExt));
    }

    @Override
    public UploadResult uploadAndGetResult(String filename, InputStream is, String fileExt) {
        return convert(super.uploadAndGetResult(filename, is, fileExt));
    }

    @Override
    public UploadResult uploadByMd5(String bucketName, InputStream is, String fileExt) {
        return convert(super.uploadByMd5(bucketName, is, fileExt));
    }

    @Override
    public UploadResult uploadByMd5(InputStream is, String fileExt) {
        return convert(super.uploadByMd5(is, fileExt));
    }

    private static UploadResult convert(MinioUploadResult result) {
        if (result == null) {
            return null;
        }
        return new UploadResult(result.getBucketName(), result.getObjectName(), result.getUrl(), result.getEtag(), result.getVersionId());
    }

    public static class UploadResult extends MinioUploadResult {
        public UploadResult(String bucketName, String objectName, String url, String etag, String versionId) {
            super(bucketName, objectName, url, etag, versionId);
        }
    }
}
