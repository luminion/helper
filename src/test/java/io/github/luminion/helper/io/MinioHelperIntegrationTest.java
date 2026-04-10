package io.github.luminion.helper.io;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinioHelperIntegrationTest {

    /**
     * 运行前设置：
     * MINIO_ENDPOINT
     * MINIO_ACCESS_KEY
     * MINIO_SECRET_KEY
     * MINIO_BUCKET
     * 可选：
     * MINIO_PUBLIC_ENDPOINT
     */
    @Test
    void shouldUploadAndDownloadObject() {
        MinioHelper helper = createHelperOrSkip();
        String objectName = "codex-test/" + UUID.randomUUID() + ".txt";
        String body = "hello minio";

        MinioHelper.UploadResult result = helper.upload(objectName,
                new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        helper.download(objectName, outputStream);

        assertTrue(helper.fileExists(objectName));
        assertEquals(body, new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
        assertTrue(result.getUrl().contains(objectName.replace("/", "%2F")) || result.getUrl().contains(objectName));
    }

    @Test
    void shouldCompleteChunkUpload() {
        MinioHelper helper = createHelperOrSkip();
        String objectName = "codex-test/chunk-" + UUID.randomUUID() + ".txt";
        MinioHelper.ChunkUploadSession session = helper.initChunkUpload(objectName);

        helper.uploadChunk(session, 0, new ByteArrayInputStream("hello ".getBytes(StandardCharsets.UTF_8)));
        helper.uploadChunk(session, 1, new ByteArrayInputStream("chunk".getBytes(StandardCharsets.UTF_8)));

        MinioHelper.UploadResult result = helper.completeChunkUpload(session, 2);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        helper.download(objectName, outputStream);

        assertEquals(objectName, result.getObjectName());
        assertEquals("hello chunk", new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    private MinioHelper createHelperOrSkip() {
        String endpoint = System.getenv("MINIO_ENDPOINT");
        String accessKey = System.getenv("MINIO_ACCESS_KEY");
        String secretKey = System.getenv("MINIO_SECRET_KEY");
        String bucket = System.getenv("MINIO_BUCKET");

        Assumptions.assumeTrue(isNotBlank(endpoint) && isNotBlank(accessKey)
                        && isNotBlank(secretKey) && isNotBlank(bucket),
                "Set MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, MINIO_BUCKET before running");

        String publicEndpoint = System.getenv("MINIO_PUBLIC_ENDPOINT");
        return isNotBlank(publicEndpoint)
                ? MinioHelper.of(endpoint, publicEndpoint, accessKey, secretKey, bucket)
                : MinioHelper.of(endpoint, accessKey, secretKey, bucket);
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
