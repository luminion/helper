package io.github.luminion.helper.io;

import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
public class MinioHelper {

    private static final int PART_SIZE = 10 * 1024 * 1024;
    private static final String CHUNK_PREFIX = "__chunk_upload__/";

    private final MinioClient minioClient;
    private final String endpoint;
    private final String bucket;
    /**
     * 拼接对象访问 URL 时使用的前缀(已包含 bucket 语义)。
     * 最终 URL = objectUrlPrefix + "/" + 编码后的 objectName。
     */
    private final String objectUrlPrefix;

    private MinioHelper(Builder builder) {
        if (builder.minioClient == null) {
            throw new IllegalArgumentException("minioClient is null");
        }
        this.minioClient = builder.minioClient;
        this.endpoint = normalizeOptionalBaseUrl(builder.endpoint);
        this.bucket = requireBucketName(builder.bucket);
        this.objectUrlPrefix = resolveObjectUrlPrefix(this.endpoint, this.bucket, builder.publicUrl);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated 改用 {@link #builder()};该重载保留以兼容旧调用。
     */
    @Deprecated
    public static MinioHelper of(String endpoint, String accessKey, String secretKey, String bucket) {
        return of(endpoint, null, accessKey, secretKey, bucket);
    }

    /**
     * @deprecated 改用 {@link #builder()};{@code publicEndpoint} 现等价于
     * {@link Builder#publicUrl(String)},但需自行带上 bucket 段(如 {@code https://cdn.xxx.com/bucket})。
     */
    @Deprecated
    public static MinioHelper of(String endpoint, String publicEndpoint, String accessKey, String secretKey,
                                 String bucket) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(requireText(endpoint, "endpoint"))
                .credentials(requireText(accessKey, "accessKey"), requireText(secretKey, "secretKey"))
                .build();
        return builder()
                .client(minioClient)
                .endpoint(endpoint)
                .publicUrl(legacyPublicUrl(publicEndpoint, bucket))
                .bucket(bucket)
                .build();
    }

    /**
     * @deprecated 改用 {@link #builder()}。
     */
    @Deprecated
    public static MinioHelper of(MinioClient minioClient, String endpoint, String bucket) {
        return builder().client(minioClient).endpoint(endpoint).bucket(bucket).build();
    }

    /**
     * @deprecated 改用 {@link #builder()};{@code publicEndpoint} 需自行带上 bucket 段。
     */
    @Deprecated
    public static MinioHelper of(MinioClient minioClient, String endpoint, String publicEndpoint, String bucket) {
        return builder()
                .client(minioClient)
                .endpoint(endpoint)
                .publicUrl(legacyPublicUrl(publicEndpoint, bucket))
                .bucket(bucket)
                .build();
    }

    private static String legacyPublicUrl(String publicEndpoint, String bucket) {
        String base = normalizeOptionalBaseUrl(publicEndpoint);
        if (base == null) {
            return null;
        }
        return base + "/" + encodePathSegment(requireBucketName(bucket));
    }

    public boolean bucketExists() {
        return call("check bucket exists", () -> minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        ));
    }

    public void ensureBucketExists() {
        if (bucketExists()) {
            return;
        }
        run("create bucket", () -> minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build()));
    }

    public void removeBucket() {
        run("remove bucket", () -> minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build()));
    }

    public UploadResult upload(String objectName, InputStream inputStream) {
        return uploadInternal(normalizeObjectName(objectName), requireInputStream(inputStream));
    }

    public UploadResult upload(String objectName, InputStream inputStream, String fileExt) {
        return uploadInternal(buildObjectName(objectName, fileExt), requireInputStream(inputStream));
    }

    /**
     * 指定 Content-Type 上传(浏览器内联预览需要,如 image/png、application/pdf)。
     * {@code contentType} 为空时按对象名后缀自动推断。
     */
    public UploadResult upload(String objectName, InputStream inputStream, long size, String contentType) {
        String actualObjectName = normalizeObjectName(objectName);
        return uploadInternal(actualObjectName, requireInputStream(inputStream), size,
                resolveContentType(contentType, actualObjectName));
    }

    public String uploadAndGetUrl(String objectName, InputStream inputStream) {
        return upload(objectName, inputStream).getUrl();
    }

    public String uploadAndGetUrl(String objectName, InputStream inputStream, String fileExt) {
        return upload(objectName, inputStream, fileExt).getUrl();
    }

    public UploadResult uploadByMd5(InputStream inputStream, String fileExt) {
        Path tempFile = null;
        try (InputStream actualInputStream = requireInputStream(inputStream)) {
            tempFile = Files.createTempFile("minio-helper-", ".upload");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[16 * 1024];
                int length;
                while ((length = actualInputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, length);
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }

            String objectName = toHex(messageDigest.digest()) + buildExtensionSuffix(fileExt);
            try (InputStream uploadInputStream = Files.newInputStream(tempFile)) {
                return upload(objectName, uploadInputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("uploadByMd5 failed", e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    public String uploadByMd5AndGetUrl(InputStream inputStream, String fileExt) {
        return uploadByMd5(inputStream, fileExt).getUrl();
    }

    public String getObjectUrl(String objectName) {
        return objectUrlPrefix + "/" + encodeObjectPath(objectName);
    }

    /**
     * 生成带签名的临时访问 URL(私有桶取链的正确方式,OSS/S3 默认私有桶请用此方法)。
     *
     * @param objectName 对象名
     * @param expiry     有效期,最长 7 天
     * @return 预签名 URL
     */
    public String presignedUrl(String objectName, Duration expiry) {
        return presignedUrl(objectName, expiry, Method.GET);
    }

    /**
     * 生成预签名上传 URL,前端可直接 PUT 文件到对象存储,不经过应用服务器。
     *
     * @param objectName 对象名
     * @param expiry     有效期,最长 7 天
     * @return 预签名 PUT URL
     */
    public String presignedUploadUrl(String objectName, Duration expiry) {
        return presignedUrl(objectName, expiry, Method.PUT);
    }

    private String presignedUrl(String objectName, Duration expiry, Method method) {
        int seconds = toExpirySeconds(expiry);
        return call("get presigned url", () -> minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(method)
                        .bucket(bucket)
                        .object(normalizeObjectName(objectName))
                        .expiry(seconds)
                        .build()
        ));
    }

    private static int toExpirySeconds(Duration expiry) {
        if (expiry == null || expiry.isZero() || expiry.isNegative()) {
            throw new IllegalArgumentException("expiry must be positive");
        }
        long seconds = expiry.getSeconds();
        if (seconds > 7 * 24 * 60 * 60L) {
            throw new IllegalArgumentException("expiry must be <= 7 days");
        }
        return (int) seconds;
    }

    public boolean fileExists(String objectName) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(normalizeObjectName(objectName)).build()
            );
            return response.size() >= 0;
        } catch (ErrorResponseException e) {
            String code = e.errorResponse() == null ? null : e.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "NoSuchBucket".equals(code)) {
                return false;
            }
            throw new IllegalStateException("check object exists failed", e);
        } catch (Exception e) {
            throw new IllegalStateException("check object exists failed", e);
        }
    }

    public void download(String objectName, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null");
        }
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(normalizeObjectName(objectName)).build()
        )) {
            byte[] buffer = new byte[16 * 1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new IllegalStateException("download failed", e);
        }
    }

    /**
     * 删除单个对象。对象不存在时不报错(幂等)。
     */
    public void delete(String objectName) {
        run("delete object", () -> minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket).object(normalizeObjectName(objectName)).build()
        ));
    }

    /**
     * 批量删除对象。
     *
     * @param objectNames 待删除对象名
     * @return 删除失败的对象名(全部成功则为空列表)
     */
    public List<String> delete(List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<DeleteObject> targets = new ArrayList<>(objectNames.size());
        for (String objectName : objectNames) {
            targets.add(new DeleteObject(normalizeObjectName(objectName)));
        }
        List<String> failed = new ArrayList<>();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder().bucket(bucket).objects(targets).build()
        );
        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                failed.add(error.objectName());
                log.warn("delete object failed, objectName={}, code={}, message={}",
                        error.objectName(), error.code(), error.message());
            } catch (Exception e) {
                throw new IllegalStateException("batch delete failed", e);
            }
        }
        return failed;
    }

    /**
     * 获取对象元信息(size / contentType / lastModified 等)。对象不存在抛 {@link IllegalStateException}。
     */
    public ObjectStat stat(String objectName) {
        StatObjectResponse response = call("stat object", () -> minioClient.statObject(
                StatObjectArgs.builder().bucket(bucket).object(normalizeObjectName(objectName)).build()
        ));
        return new ObjectStat(response.object(), response.size(), response.etag(),
                response.contentType(), response.lastModified(), response.userMetadata());
    }

    /**
     * 列出指定前缀下的所有对象名(递归)。{@code prefix} 为空则列出整个桶。
     */
    public List<String> listObjects(String prefix) {
        ListObjectsArgs.Builder argsBuilder = ListObjectsArgs.builder().bucket(bucket).recursive(true);
        if (prefix != null && !prefix.trim().isEmpty()) {
            argsBuilder.prefix(prefix.trim());
        }
        List<String> names = new LinkedList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(argsBuilder.build());
        for (Result<Item> result : results) {
            try {
                names.add(result.get().objectName());
            } catch (Exception e) {
                throw new IllegalStateException("list objects failed", e);
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * 在同一桶内复制对象。
     */
    public UploadResult copy(String sourceObjectName, String targetObjectName) {
        String target = normalizeObjectName(targetObjectName);
        ObjectWriteResponse response = call("copy object", () -> minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(target)
                        .source(CopySource.builder()
                                .bucket(bucket)
                                .object(normalizeObjectName(sourceObjectName))
                                .build())
                        .build()
        ));
        return toUploadResult(target, response);
    }

    public ChunkUploadSession initChunkUpload(String objectName) {
        return initChunkUpload(objectName, null);
    }

    public ChunkUploadSession initChunkUpload(String objectName, String fileExt) {
        String actualObjectName = buildObjectName(objectName, fileExt);
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        return new ChunkUploadSession(uploadId, actualObjectName, buildChunkPrefix(uploadId));
    }

    public void uploadChunk(ChunkUploadSession session, int chunkIndex, InputStream inputStream) {
        validateChunkIndex(chunkIndex);
        ChunkUploadSession actualSession = requireSession(session);
        uploadInternal(buildChunkObjectName(actualSession, chunkIndex), requireInputStream(inputStream));
    }

    public List<Integer> getUploadedChunkIndexes(ChunkUploadSession session) {
        ChunkUploadSession actualSession = requireSession(session);
        List<ChunkPart> parts = listChunkParts(actualSession);
        List<Integer> indexes = new ArrayList<>(parts.size());
        for (ChunkPart part : parts) {
            indexes.add(part.chunkIndex);
        }
        return indexes;
    }

    public UploadResult completeChunkUpload(ChunkUploadSession session) {
        ChunkUploadSession actualSession = requireSession(session);
        List<ChunkPart> parts = listChunkParts(actualSession);
        if (parts.isEmpty()) {
            throw new IllegalStateException("no uploaded chunks found");
        }
        return composeChunks(actualSession, parts);
    }

    public UploadResult completeChunkUpload(ChunkUploadSession session, int expectedChunkCount) {
        if (expectedChunkCount <= 0) {
            throw new IllegalArgumentException("expectedChunkCount must be > 0");
        }
        ChunkUploadSession actualSession = requireSession(session);
        List<ChunkPart> parts = listChunkParts(actualSession);
        if (parts.size() != expectedChunkCount) {
            throw new IllegalStateException("chunk count mismatch, expected=" + expectedChunkCount
                    + ", actual=" + parts.size());
        }
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).chunkIndex != i) {
                throw new IllegalStateException("missing chunk index: " + i);
            }
        }
        return composeChunks(actualSession, parts);
    }

    public void abortChunkUpload(ChunkUploadSession session) {
        cleanupChunks(requireSession(session), false);
    }

    private UploadResult uploadInternal(String objectName, InputStream inputStream) {
        return uploadInternal(objectName, inputStream, -1, resolveContentType(null, objectName));
    }

    private UploadResult uploadInternal(String objectName, InputStream inputStream, long size, String contentType) {
        try (InputStream actualInputStream = inputStream) {
            PutObjectArgs.Builder argsBuilder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(actualInputStream, size, size > 0 ? -1 : PART_SIZE);
            if (contentType != null) {
                argsBuilder.contentType(contentType);
            }
            PutObjectArgs args = argsBuilder.build();
            ObjectWriteResponse response = call("upload object", () -> minioClient.putObject(args));
            return toUploadResult(objectName, response);
        } catch (IOException e) {
            throw new IllegalStateException("close inputStream failed", e);
        }
    }

    private UploadResult composeChunks(ChunkUploadSession session, List<ChunkPart> parts) {
        List<ComposeSource> sources = new ArrayList<>(parts.size());
        for (ChunkPart part : parts) {
            sources.add(ComposeSource.builder().bucket(bucket).object(part.objectName).build());
        }
        ObjectWriteResponse response = call("compose chunk upload", () -> minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(bucket)
                        .object(session.objectName)
                        .sources(sources)
                        .build()
        ));
        cleanupChunks(session, true);
        return toUploadResult(session.objectName, response);
    }

    private List<ChunkPart> listChunkParts(ChunkUploadSession session) {
        List<ChunkPart> parts = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket).prefix(session.chunkPrefix).recursive(true).build()
        );
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                String objectName = item.objectName();
                Integer chunkIndex = parseChunkIndex(session.chunkPrefix, objectName);
                if (chunkIndex != null) {
                    parts.add(new ChunkPart(chunkIndex, objectName));
                }
            } catch (Exception e) {
                throw new IllegalStateException("list chunk upload failed", e);
            }
        }
        Collections.sort(parts, Comparator.comparingInt(part -> part.chunkIndex));
        return parts;
    }

    private void cleanupChunks(ChunkUploadSession session, boolean ignoreFailure) {
        List<ChunkPart> parts;
        try {
            parts = listChunkParts(session);
        } catch (RuntimeException e) {
            if (ignoreFailure) {
                log.warn("cleanup chunk upload failed, uploadId={}", session.uploadId, e);
                return;
            }
            throw e;
        }

        for (ChunkPart part : parts) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(part.objectName).build());
            } catch (Exception e) {
                if (ignoreFailure) {
                    log.warn("cleanup chunk object failed, uploadId={}, objectName={}",
                            session.uploadId, part.objectName, e);
                } else {
                    throw new IllegalStateException("abort chunk upload failed", e);
                }
            }
        }
    }

    private UploadResult toUploadResult(String objectName, ObjectWriteResponse response) {
        return new UploadResult(bucket, objectName, getObjectUrl(objectName), response.etag(), response.versionId());
    }

    private static InputStream requireInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null");
        }
        return inputStream;
    }

    private static ChunkUploadSession requireSession(ChunkUploadSession session) {
        if (session == null) {
            throw new IllegalArgumentException("chunkUploadSession is null");
        }
        return session;
    }

    private static void validateChunkIndex(int chunkIndex) {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be >= 0");
        }
    }

    private String buildChunkPrefix(String uploadId) {
        return CHUNK_PREFIX + uploadId + "/";
    }

    private String buildChunkObjectName(ChunkUploadSession session, int chunkIndex) {
        return session.chunkPrefix + String.format(Locale.ROOT, "%05d.part", chunkIndex);
    }

    private Integer parseChunkIndex(String chunkPrefix, String objectName) {
        if (objectName == null || !objectName.startsWith(chunkPrefix) || !objectName.endsWith(".part")) {
            return null;
        }
        String partName = objectName.substring(chunkPrefix.length(), objectName.length() - 5);
        if (partName.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(partName);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private String buildObjectName(String objectName, String fileExt) {
        String actualObjectName = normalizeObjectName(objectName);
        if (hasExtension(actualObjectName) || normalizeExtension(fileExt) == null) {
            return actualObjectName;
        }
        return actualObjectName + buildExtensionSuffix(fileExt);
    }

    private String normalizeObjectName(String objectName) {
        String normalized = requireText(objectName, "objectName").replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("objectName is blank");
        }
        return normalized;
    }

    private boolean hasExtension(String objectName) {
        int slashIndex = objectName.lastIndexOf('/');
        int dotIndex = objectName.lastIndexOf('.');
        return dotIndex > slashIndex && dotIndex < objectName.length() - 1;
    }

    private static String normalizeExtension(String fileExt) {
        if (fileExt == null) {
            return null;
        }
        String extension = fileExt.trim();
        while (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension.isEmpty() ? null : extension;
    }

    private static String buildExtensionSuffix(String fileExt) {
        String extension = normalizeExtension(fileExt);
        return extension == null ? "" : "." + extension;
    }

    private static String resolveContentType(String contentType, String objectName) {
        if (contentType != null && !contentType.trim().isEmpty()) {
            return contentType.trim();
        }
        return URLConnection.guessContentTypeFromName(objectName);
    }

    private static String resolveObjectUrlPrefix(String endpoint, String bucket, String publicUrl) {
        String normalizedPublicUrl = normalizeOptionalBaseUrl(publicUrl);
        if (normalizedPublicUrl != null) {
            return normalizedPublicUrl;
        }
        if (endpoint == null) {
            throw new IllegalArgumentException("either endpoint or publicUrl must be provided");
        }
        return endpoint + "/" + encodePathSegment(bucket);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String actualBaseUrl = requireText(baseUrl, "baseUrl");
        while (actualBaseUrl.endsWith("/")) {
            actualBaseUrl = actualBaseUrl.substring(0, actualBaseUrl.length() - 1);
        }
        return actualBaseUrl;
    }

    private static String normalizeOptionalBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return null;
        }
        return normalizeBaseUrl(baseUrl);
    }

    private String encodeObjectPath(String objectName) {
        String[] segments = normalizeObjectName(objectName).split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(encodePathSegment(segments[i]));
        }
        return builder.toString();
    }

    private static String encodePathSegment(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            throw new IllegalStateException("encode url segment failed", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0x0F, 16));
            builder.append(Character.forDigit(value & 0x0F, 16));
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
            log.warn("delete temp file failed, file={}", tempFile, e);
        }
    }

    private static String requireBucketName(String bucketName) {
        return requireText(bucketName, "bucketName");
    }

    private static String requireText(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is blank");
        }
        return text.trim();
    }

    private void run(String action, ThrowingRunnable runnable) {
        call(action, () -> {
            runnable.run();
            return null;
        });
    }

    private <T> T call(String action, ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new IllegalStateException(action + " failed", e);
        }
    }

    @Getter
    public static class UploadResult {
        private final String bucketName;
        private final String objectName;
        private final String url;
        private final String etag;
        private final String versionId;

        public UploadResult(String bucketName, String objectName, String url, String etag, String versionId) {
            this.bucketName = bucketName;
            this.objectName = objectName;
            this.url = url;
            this.etag = etag;
            this.versionId = versionId;
        }

        public String getFilename() {
            return objectName;
        }
    }

    @Getter
    public static class ObjectStat {
        private final String objectName;
        private final long size;
        private final String etag;
        private final String contentType;
        private final ZonedDateTime lastModified;
        private final Map<String, String> userMetadata;

        public ObjectStat(String objectName, long size, String etag, String contentType,
                          ZonedDateTime lastModified, Map<String, String> userMetadata) {
            this.objectName = objectName;
            this.size = size;
            this.etag = etag;
            this.contentType = contentType;
            this.lastModified = lastModified;
            this.userMetadata = userMetadata;
        }
    }

    @Getter
    public static class ChunkUploadSession {
        private final String uploadId;
        private final String objectName;
        private final String chunkPrefix;

        public ChunkUploadSession(String uploadId, String objectName, String chunkPrefix) {
            this.uploadId = uploadId;
            this.objectName = objectName;
            this.chunkPrefix = chunkPrefix;
        }
    }

    private static class ChunkPart {
        private final int chunkIndex;
        private final String objectName;

        private ChunkPart(int chunkIndex, String objectName) {
            this.chunkIndex = chunkIndex;
            this.objectName = objectName;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static class Builder {
        private MinioClient minioClient;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String publicUrl;

        /**
         * 直接复用已有的 MinioClient(Spring 项目推荐)。设置后将忽略 credentials。
         */
        public Builder client(MinioClient minioClient) {
            this.minioClient = minioClient;
            return this;
        }

        /**
         * MinIO/OSS 服务端地址。未设置 {@link #publicUrl(String)} 时,
         * 访问 URL 会回退为 path-style:{@code endpoint/bucket/object}。
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder credentials(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            return this;
        }

        public Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * 对象访问 URL 前缀(需包含 bucket 语义),用于兼容各种部署形态:
         * <pre>
         * MinIO path-style :  http://host:9000/bucket
         * OSS virtual-host :  https://bucket.oss-cn-hangzhou.aliyuncs.com
         * 自定义域名/CDN    :  https://cdn.xxx.com
         * </pre>
         * 不设置则回退到 {@code endpoint/bucket} 的 path-style。
         */
        public Builder publicUrl(String publicUrl) {
            this.publicUrl = publicUrl;
            return this;
        }

        public MinioHelper build() {
            if (minioClient == null) {
                minioClient = MinioClient.builder()
                        .endpoint(requireText(endpoint, "endpoint"))
                        .credentials(requireText(accessKey, "accessKey"), requireText(secretKey, "secretKey"))
                        .build();
            }
            return new MinioHelper(this);
        }
    }
}
