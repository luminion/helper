package io.github.luminion.helper.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadChunksAndMergeFile() throws Exception {
        FileUploadHelper helper = new FileUploadHelper(tempDir.toString());

        helper.uploadChunk(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), "md5", "0");
        helper.uploadChunk(new ByteArrayInputStream("world".getBytes(StandardCharsets.UTF_8)), "md5", "1");

        assertTrue(helper.verifyChunk("md5", "0", 5));
        assertEquals(2, helper.getChunkUploadCount("md5"));
        assertEquals(Arrays.asList(0, 1), helper.getChunkUploadIndex("md5", 5));
        assertTrue(helper.mergeFile("md5", "txt", 2));
        assertEquals("helloworld", new String(Files.readAllBytes(tempDir.resolve("md5.txt")), StandardCharsets.UTF_8));
    }

    @Test
    void shouldCancelNonEmptyChunkDirectory() throws Exception {
        FileUploadHelper helper = new FileUploadHelper(tempDir.toString());
        helper.uploadChunk(new ByteArrayInputStream("part".getBytes(StandardCharsets.UTF_8)), "cancel-md5", "0");

        assertTrue(helper.cancel("cancel-md5"));
        assertFalse(Files.exists(tempDir.resolve("cancel-md5")));
    }

    @Test
    void shouldMergeWhenChunkSizesAreComplete() throws Exception {
        FileUploadHelper helper = new FileUploadHelper(tempDir.toString());
        // 标准分片 5 字节, 最后一片 3 字节
        helper.uploadChunk(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), "ok-md5", "0");
        helper.uploadChunk(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), "ok-md5", "1");

        assertTrue(helper.mergeFile("ok-md5", "txt", 2, 5));
        assertEquals("helloabc", new String(Files.readAllBytes(tempDir.resolve("ok-md5.txt")), StandardCharsets.UTF_8));
    }

    @Test
    void shouldRejectMergeWhenChunkIncomplete() throws Exception {
        FileUploadHelper helper = new FileUploadHelper(tempDir.toString());
        // 第一个非末尾分片只有 3 字节, 但标准大小为 5, 视为不完整
        helper.uploadChunk(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), "bad-md5", "0");
        helper.uploadChunk(new ByteArrayInputStream("de".getBytes(StandardCharsets.UTF_8)), "bad-md5", "1");

        assertFalse(helper.mergeFile("bad-md5", "txt", 2, 5));
        assertFalse(Files.exists(tempDir.resolve("bad-md5.txt")));
    }


}
