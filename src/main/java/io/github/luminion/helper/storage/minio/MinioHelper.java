package io.github.luminion.helper.storage.minio;

import io.minio.MinioClient;

/**
 * MinIO 静态入口。
 * <p>
 * 只负责创建客户端实例，没有独立职责。
 * 直接使用 {@link MinioStorageClient#of(MinioClient)} 或
 * {@link MinioStorageClient#of(String, String, String)} 即可。
 *
 * @author luminion
 * @deprecated 请直接使用 {@link MinioStorageClient}
 */
@Deprecated
public abstract class MinioHelper {
    private MinioHelper() {}

    /**
     * 基于已有的 MinIO SDK 客户端创建包装对象。
     */
    public static MinioStorageClient of(MinioClient minioClient) {
        return MinioStorageClient.of(minioClient);
    }

    /**
     * 创建带默认桶自动初始化能力的 MinIO 客户端。
     */
    public static MinioStorageClient of(String endpoint, String accessKey, String secretKey) {
        return MinioStorageClient.of(endpoint, accessKey, secretKey);
    }

    /**
     * 创建带指定默认桶的 MinIO 客户端。
     */
    public static MinioStorageClient of(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        return MinioStorageClient.of(endpoint, accessKey, secretKey, defaultBucket);
    }

    /**
     * 兼容旧入口，建议改用 {@link #of(MinioClient)}。
     *
     * @deprecated 请使用 {@link #of(MinioClient)}
     */
    @Deprecated
    public static MinioStorageClient from(MinioClient minioClient) {
        return of(minioClient);
    }
}
