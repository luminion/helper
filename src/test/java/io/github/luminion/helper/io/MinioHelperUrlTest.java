package io.github.luminion.helper.io;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinioHelperUrlTest {

    private MinioClient client() {
        return MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("ak", "sk")
                .region("us-east-1")
                .build();
    }

    private MinioHelper helper() {
        return MinioHelper.builder()
                .client(client())
                .endpoint("http://127.0.0.1:9000")
                .bucket("test")
                .build();
    }

    @Test
    void pathStyleFallbackMatchesLegacyOutput() {
        MinioHelper helper = MinioHelper.builder()
                .client(client())
                .endpoint("http://127.0.0.1:9000/")
                .bucket("test")
                .build();
        assertEquals("http://127.0.0.1:9000/test/a/b.txt", helper.getObjectUrl("a/b.txt"));
    }

    @Test
    void ossVirtualHostedStyle() {
        MinioHelper helper = MinioHelper.builder()
                .client(client())
                .endpoint("https://oss-cn-hangzhou.aliyuncs.com")
                .publicUrl("https://bucket.oss-cn-hangzhou.aliyuncs.com")
                .bucket("bucket")
                .build();
        assertEquals("https://bucket.oss-cn-hangzhou.aliyuncs.com/dir/x.png", helper.getObjectUrl("dir/x.png"));
    }

    @Test
    void customCdnDomainWithoutBucketInPath() {
        MinioHelper helper = MinioHelper.builder()
                .client(client())
                .endpoint("http://127.0.0.1:9000")
                .publicUrl("https://cdn.xxx.com")
                .bucket("bucket")
                .build();
        assertEquals("https://cdn.xxx.com/x.png", helper.getObjectUrl("x.png"));
    }

    @Test
    void legacyPublicEndpointStillAppendsBucket() {
        MinioHelper helper = MinioHelper.of(
                "http://127.0.0.1:9000", "https://cdn.xxx.com", "ak", "sk", "test");
        assertEquals("https://cdn.xxx.com/test/x.png", helper.getObjectUrl("x.png"));
    }

    @Test
    void presignedUploadUrlIsPutAndContainsObject() {
        String url = helper().presignedUploadUrl("dir/x.png", Duration.ofMinutes(10));
        assertTrue(url.contains("/test/dir/x.png"), url);
        assertTrue(url.contains("X-Amz-Signature"), url);
    }

    @Test
    void presignedGetUrlIsSigned() {
        String url = helper().presignedUrl("a.txt", Duration.ofMinutes(5));
        assertTrue(url.contains("/test/a.txt"), url);
        assertTrue(url.contains("X-Amz-Expires=300"), url);
    }

    @Test
    void expiryMustBePositiveAndWithinSevenDays() {
        assertThrows(IllegalArgumentException.class, () -> helper().presignedUrl("a", Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> helper().presignedUrl("a", Duration.ofDays(8)));
    }
}
