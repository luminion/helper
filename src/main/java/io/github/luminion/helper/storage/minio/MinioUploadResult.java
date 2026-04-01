package io.github.luminion.helper.storage.minio;

/**
 * MinIO 上传结果。
 *
 * @author luminion
 */
public class MinioUploadResult {
    private final String bucketName;
    private final String objectName;
    private final String url;
    private final String etag;
    private final String versionId;

    public MinioUploadResult(String bucketName, String objectName, String url, String etag, String versionId) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.url = url;
        this.etag = etag;
        this.versionId = versionId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getUrl() {
        return url;
    }

    public String getEtag() {
        return etag;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getFilename() {
        return objectName;
    }
}
