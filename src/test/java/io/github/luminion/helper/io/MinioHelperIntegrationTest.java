package io.github.luminion.helper.io;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinioHelperIntegrationTest {
    private static final int MIN_CHUNK_SIZE = 5 * 1024 * 1024;
    private static final int BUFFER_SIZE = 16 * 1024;

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
    void shouldCompleteChunkUpload() throws IOException {
        MinioHelper helper = createHelperOrSkip();
        String filePath = "D:\\Develop\\apache-jmeter-5.6.3.zip";
        Path path = Paths.get(filePath);
        Assumptions.assumeTrue(Files.exists(path), "Local test file does not exist: " + filePath);

        long fileSize = Files.size(path);
        Assumptions.assumeTrue(fileSize >= MIN_CHUNK_SIZE * 2L,
                "Local test file must be at least 10 MB so every chunk stays >= 5 MB: " + filePath);

        String objectName = "codex-test/chunk-" + UUID.randomUUID() + ".zip";
        MinioHelper.ChunkUploadSession session = helper.initChunkUpload(objectName);
        List<Long> chunkSizes = calculateChunkSizes(fileSize);
        try (InputStream inputStream = Files.newInputStream(path)) {
            for (int i = 0; i < chunkSizes.size(); i++) {
                helper.uploadChunk(session, i, new ByteArrayInputStream(readChunk(inputStream, chunkSizes.get(i))));
            }
        }
        assertEquals(chunkSizes.size(), helper.getUploadedChunkIndexes(session).size());

        MinioHelper.UploadResult result = helper.completeChunkUpload(session, chunkSizes.size());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        helper.download(objectName, outputStream);

        assertEquals(objectName, result.getObjectName());
        assertEquals(fileSize, (long) outputStream.size());
        assertEquals(md5Hex(path), md5Hex(outputStream.toByteArray()));
    }

    private MinioHelper createHelperOrSkip() {
        String endpoint = "http://127.0.0.1:9000";
        String publicEndpoint = "http://127.0.0.1:9000";
        String accessKey = "admin";
        String secretKey = "12345678";
        String bucket = "test";
        return isNotBlank(publicEndpoint)
                ? MinioHelper.of(endpoint, publicEndpoint, accessKey, secretKey, bucket)
                : MinioHelper.of(endpoint, accessKey, secretKey, bucket);
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private List<Long> calculateChunkSizes(long fileSize) {
        int chunkCount = (int) (fileSize / MIN_CHUNK_SIZE);
        long baseChunkSize = fileSize / chunkCount;
        long remainder = fileSize % chunkCount;
        List<Long> chunkSizes = new ArrayList<Long>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            chunkSizes.add(baseChunkSize + (i < remainder ? 1 : 0));
        }
        return chunkSizes;
    }

    private byte[] readChunk(InputStream inputStream, long chunkSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) Math.min(chunkSize, Integer.MAX_VALUE));
        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = chunkSize;
        while (remaining > 0) {
            int length = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (length < 0) {
                throw new IllegalStateException("Unexpected end of file while reading chunk");
            }
            outputStream.write(buffer, 0, length);
            remaining -= length;
        }
        return outputStream.toByteArray();
    }

    private String md5Hex(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return md5Hex(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Read local file failed: " + path, e);
        }
    }

    private String md5Hex(byte[] content) {
        return md5Hex(new ByteArrayInputStream(content));
    }

    private String md5Hex(InputStream inputStream) {
        try (InputStream actualInputStream = inputStream) {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = actualInputStream.read(buffer)) >= 0) {
                if (length > 0) {
                    messageDigest.update(buffer, 0, length);
                }
            }
            return toHex(messageDigest.digest());
        } catch (IOException e) {
            throw new IllegalStateException("Read stream failed while calculating md5", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0x0F, 16));
            builder.append(Character.forDigit(value & 0x0F, 16));
        }
        return builder.toString();
    }
}
