package io.github.luminion.helper.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCopyDirectoryRecursively() throws Exception {
        Path source = tempDir.resolve("source");
        Path nested = source.resolve("nested");
        Files.createDirectories(nested);
        Files.write(nested.resolve("demo.txt"), "hello".getBytes(StandardCharsets.UTF_8));

        Path target = tempDir.resolve("target");
        FileHelper.copyDir(source.toFile(), target.toFile());

        assertTrue(Files.exists(target.resolve("nested/demo.txt")));
    }

    @Test
    void shouldDeleteFilesByPattern() throws Exception {
        Path source = tempDir.resolve("pattern");
        Files.createDirectories(source);
        Files.write(source.resolve("keep.txt"), new byte[] {1});
        Files.write(source.resolve("delete.log"), new byte[] {1});

        FileHelper.deleteFileByPattern(source.toFile(), ".*\\.log");

        assertTrue(Files.exists(source.resolve("keep.txt")));
        assertFalse(Files.exists(source.resolve("delete.log")));
    }
}
