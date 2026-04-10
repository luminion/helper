package io.github.luminion.helper.io;

import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Getter
public class MinioHelper {

    private static final int PART_SIZE = 10 * 1024 * 1024;
    private static final String CHUNK_PREFIX = "__chunk_upload__/";

    private final MinioClient minioClient;
    private final String endpoint;
    private final String publicEndpoint;
    private final String bucket;

    private MinioHelper(MinioClient minioClient, String endpoint, String publicEndpoint, String bucket) {
        if (minioClient == null) {
            throw new IllegalArgumentException("minioClient is null");
        }
        this.minioClient = minioClient;
        this.endpoint = requireText(endpoint, "endpoint");
        this.publicEndpoint = normalizeOptionalBaseUrl(publicEndpoint);
        this.bucket = requireBucketName(bucket);
        ensureBucketExists();
    }

    public static MinioHelper of(String endpoint, String accessKey, String secretKey, String bucket) {
        return of(endpoint, null, accessKey, secretKey, bucket);
    }

    public static MinioHelper of(String endpoint, String publicEndpoint, String accessKey, String secretKey,
                                 String bucket) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(requireText(endpoint, "endpoint"))
                .credentials(requireText(accessKey, "accessKey"), requireText(secretKey, "secretKey"))
                .build();
        return new MinioHelper(minioClient, endpoint, publicEndpoint, bucket);
    }

    public static MinioHelper of(MinioClient minioClient, String endpoint, String bucket) {
        return new MinioHelper(minioClient, endpoint, null, bucket);
    }

    public static MinioHelper of(MinioClient minioClient, String endpoint, String publicEndpoint, String bucket) {
        return new MinioHelper(minioClient, endpoint, publicEndpoint, bucket);
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
        return resolveBaseUrl() + "/" + encodePathSegment(bucket) + "/" + encodeObjectPath(objectName);
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
        try (InputStream actualInputStream = inputStream) {
            ObjectWriteResponse response = call("upload object", () -> minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(actualInputStream, -1, PART_SIZE)
                            .build()
            ));
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

    private String resolveBaseUrl() {
        return publicEndpoint != null ? publicEndpoint : normalizeBaseUrl(endpoint);
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

    private String encodePathSegment(String text) {
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
}
