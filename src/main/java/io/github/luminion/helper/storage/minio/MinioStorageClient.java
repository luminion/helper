package io.github.luminion.helper.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

@Slf4j
public class MinioStorageClient {

    private static final int PART_SIZE = 10 * 1024 * 1024;

    @Setter
    @Getter
    protected String defaultBucket = "bucket1";
    @Getter
    protected MinioClient minioClient;
    @Getter
    protected String endpoint;
    @Setter
    @Getter
    protected String publicEndpoint;

    public MinioStorageClient(MinioClient minioClient) {
        this.minioClient = minioClient;
        init();
    }

    public MinioStorageClient(String endpoint, String accessKey, String secretKey) {
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.endpoint = endpoint;
        init();
    }

    public MinioStorageClient(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        this.endpoint = endpoint;
        this.defaultBucket = defaultBucket;
        init();
    }

    private void init() {
        // 只有显式配置了默认桶时才做初始化，避免“只创建客户端”也产生强副作用。
        if (defaultBucket != null && !defaultBucket.isEmpty()) {
            createBucket(defaultBucket);
        }
    }

    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("createBucket => bucket error", e);
        }
        return false;
    }

    public boolean createBucket(String bucketName) {
        try {
            boolean b = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!b) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                return true;
            }
        } catch (Exception e) {
            log.error("createBucket => bucket error", e);
        }
        return false;
    }

    public boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            return true;
        } catch (Exception e) {
            log.error("createBucket => bucket error", e);
        }
        return false;
    }

    public ObjectWriteResponse upload(String bucketName, String filename, InputStream is) {
        String actualBucketName = requireBucketName(bucketName);
        String actualObjectName = normalizeObjectName(filename);
        try {
            // 上传前补一次桶创建，降低调用方忘记手动建桶的使用成本。
            createBucket(actualBucketName);
            return minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(actualBucketName)
                            .object(actualObjectName)
                            .stream(is, -1, PART_SIZE)
                            .build()
            );
        } catch (Exception e) {
            log.error("upload => file error", e);
        } finally {
            closeQuietly(is, "upload => close inputStream failed");
        }
        return null;
    }

    public ObjectWriteResponse upload(String filename, InputStream is) {
        return upload(requireDefaultBucket(), filename, is);
    }

    public MinioUploadResult uploadAndGetResult(String bucketName, String filename, InputStream is) {
        ObjectWriteResponse response = upload(bucketName, filename, is);
        if (response == null) {
            return null;
        }
        String actualBucketName = requireBucketName(bucketName);
        String actualObjectName = normalizeObjectName(filename);
        return new MinioUploadResult(actualBucketName, actualObjectName, getObjectUrl(actualBucketName, actualObjectName),
                response.etag(), response.versionId());
    }

    public MinioUploadResult uploadAndGetResult(String filename, InputStream is) {
        return uploadAndGetResult(requireDefaultBucket(), filename, is);
    }

    public MinioUploadResult uploadAndGetResult(String bucketName, String filename, InputStream is, String fileExt) {
        return uploadAndGetResult(bucketName, buildObjectName(filename, fileExt), is);
    }

    public MinioUploadResult uploadAndGetResult(String filename, InputStream is, String fileExt) {
        return uploadAndGetResult(requireDefaultBucket(), filename, is, fileExt);
    }

    public String uploadAndGetObjectName(String bucketName, String filename, InputStream is) {
        MinioUploadResult result = uploadAndGetResult(bucketName, filename, is);
        return result == null ? null : result.getObjectName();
    }

    public String uploadAndGetObjectName(String filename, InputStream is) {
        return uploadAndGetObjectName(requireDefaultBucket(), filename, is);
    }

    public String uploadAndGetObjectName(String bucketName, String filename, InputStream is, String fileExt) {
        MinioUploadResult result = uploadAndGetResult(bucketName, filename, is, fileExt);
        return result == null ? null : result.getObjectName();
    }

    public String uploadAndGetObjectName(String filename, InputStream is, String fileExt) {
        return uploadAndGetObjectName(requireDefaultBucket(), filename, is, fileExt);
    }

    public String uploadAndGetUrl(String bucketName, String filename, InputStream is) {
        MinioUploadResult result = uploadAndGetResult(bucketName, filename, is);
        return result == null ? null : result.getUrl();
    }

    public String uploadAndGetUrl(String filename, InputStream is) {
        return uploadAndGetUrl(requireDefaultBucket(), filename, is);
    }

    public String uploadAndGetUrl(String bucketName, String filename, InputStream is, String fileExt) {
        MinioUploadResult result = uploadAndGetResult(bucketName, filename, is, fileExt);
        return result == null ? null : result.getUrl();
    }

    public String uploadAndGetUrl(String filename, InputStream is, String fileExt) {
        return uploadAndGetUrl(requireDefaultBucket(), filename, is, fileExt);
    }

    public MinioUploadResult uploadByMd5(String bucketName, InputStream is, String fileExt) {
        Path tempFile = null;
        try (InputStream actualInputStream = is) {
            // 先把流写入临时文件，同时计算 MD5，避免要求调用方必须传入可重复读取的流。
            tempFile = Files.createTempFile("minio-helper-", ".upload");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[16 * 1024];
                int len;
                while ((len = actualInputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, len);
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            }

            String objectName = toHex(messageDigest.digest()) + buildExtensionSuffix(fileExt);
            try (InputStream uploadInputStream = Files.newInputStream(tempFile)) {
                return uploadAndGetResult(bucketName, objectName, uploadInputStream);
            }
        } catch (Exception e) {
            log.error("uploadByMd5 => file error", e);
        } finally {
            deleteTempFile(tempFile);
        }
        return null;
    }

    public MinioUploadResult uploadByMd5(InputStream is, String fileExt) {
        return uploadByMd5(requireDefaultBucket(), is, fileExt);
    }

    public String uploadByMd5AndGetUrl(String bucketName, InputStream is, String fileExt) {
        MinioUploadResult result = uploadByMd5(bucketName, is, fileExt);
        return result == null ? null : result.getUrl();
    }

    public String uploadByMd5AndGetUrl(InputStream is, String fileExt) {
        return uploadByMd5AndGetUrl(requireDefaultBucket(), is, fileExt);
    }

    public String uploadByMd5AndGetObjectName(String bucketName, InputStream is, String fileExt) {
        MinioUploadResult result = uploadByMd5(bucketName, is, fileExt);
        return result == null ? null : result.getObjectName();
    }

    public String uploadByMd5AndGetObjectName(InputStream is, String fileExt) {
        return uploadByMd5AndGetObjectName(requireDefaultBucket(), is, fileExt);
    }

    public String getObjectUrl(String bucketName, String filename) {
        // URL 编码统一收口在这里，避免对象名包含中文、空格或斜杠时产生不可访问链接。
        String baseUrl = normalizeBaseUrl(publicEndpoint != null && !publicEndpoint.isEmpty() ? publicEndpoint : endpoint);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl + "/" + encodePathSegment(bucketName) + "/" + encodeObjectPath(filename);
    }

    public String getObjectUrl(String filename) {
        return getObjectUrl(requireDefaultBucket(), filename);
    }

    public boolean fileExists(String bucketName, String filename) {
        StatObjectResponse statObjectResponse = null;
        try {
            statObjectResponse = minioClient
                    .statObject(StatObjectArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.debug("statObjectResponse: {}", statObjectResponse);
        return statObjectResponse.size() > 0;
    }

    public boolean fileExists(String filename) {
        return fileExists(requireDefaultBucket(), filename);
    }

    public boolean download(String bucketName, String filename, OutputStream os) {
        try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .build())) {
            byte[] buf = new byte[16384];
            int bytesRead;
            while ((bytesRead = is.read(buf, 0, buf.length)) >= 0) {
                os.write(buf, 0, bytesRead);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            log.error("download => close inputStream failed", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("download => close outputStream failed", e);
                }
            }
        }
        return false;
    }

    public boolean download(String filename, OutputStream os) {
        return download(requireDefaultBucket(), filename, os);
    }

    private String requireDefaultBucket() {
        if (defaultBucket == null || defaultBucket.trim().isEmpty()) {
            throw new IllegalStateException("defaultBucket is null, please set defaultBucket");
        }
        return defaultBucket;
    }

    private String requireBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("bucketName is blank");
        }
        return bucketName.trim();
    }

    private String buildObjectName(String filename, String fileExt) {
        String actualObjectName = normalizeObjectName(filename);
        if (hasExtension(actualObjectName) || normalizeExtension(fileExt) == null) {
            return actualObjectName;
        }
        return actualObjectName + buildExtensionSuffix(fileExt);
    }

    private String normalizeObjectName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("filename is blank");
        }
        return filename.trim().replace('\\', '/');
    }

    private boolean hasExtension(String filename) {
        int slashIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > slashIndex && dotIndex < filename.length() - 1;
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

    private String buildExtensionSuffix(String fileExt) {
        String extension = normalizeExtension(fileExt);
        return extension == null ? "" : "." + extension;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return null;
        }
        String actualBaseUrl = baseUrl.trim();
        while (actualBaseUrl.endsWith("/")) {
            actualBaseUrl = actualBaseUrl.substring(0, actualBaseUrl.length() - 1);
        }
        return actualBaseUrl;
    }

    private String encodeObjectPath(String objectName) {
        String actualObjectName = normalizeObjectName(objectName);
        String[] segments = actualObjectName.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(encodePathSegment(segments[i]));
        }
        return builder.toString();
    }

    private String encodePathSegment(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            throw new IllegalStateException("encode url segment failed", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(Character.forDigit((b >> 4) & 0x0F, 16));
            builder.append(Character.forDigit(b & 0x0F, 16));
        }
        return builder.toString();
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("uploadByMd5 => delete temp file failed, file={}", tempFile, e);
        }
    }

    public static MinioStorageClient of(MinioClient minioClient) {
        return new MinioStorageClient(minioClient);
    }

    public static MinioStorageClient of(String endpoint, String accessKey, String secretKey) {
        return new MinioStorageClient(endpoint, accessKey, secretKey);
    }

    public static MinioStorageClient of(String endpoint, String accessKey, String secretKey, String defaultBucket) {
        return new MinioStorageClient(endpoint, accessKey, secretKey, defaultBucket);
    }

    private void closeQuietly(InputStream is, String errorMessage) {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (IOException e) {
            log.error(errorMessage, e);
        }
    }

}
