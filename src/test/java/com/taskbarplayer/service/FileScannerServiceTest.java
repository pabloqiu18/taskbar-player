package com.taskbarplayer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileScannerServiceTest {

    private final FileScannerService scanner = new FileScannerService();

    @TempDir
    Path tempDir;

    @Test
    void scan_findsOnlyAudioFilesRecursively() throws IOException {
        Files.createDirectories(tempDir.resolve("nested"));
        Files.createFile(tempDir.resolve("song.mp3"));
        Files.createFile(tempDir.resolve("nested/track.flac"));
        Files.createFile(tempDir.resolve("notes.txt"));
        Files.createFile(tempDir.resolve("image.jpg"));

        List<Path> result = scanner.scan(tempDir);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("song.mp3")));
        assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("track.flac")));
    }

    @Test
    void scan_rejectsNonDirectory() {
        Path file = tempDir.resolve("not-a-directory.txt");
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(file));
    }

    @Test
    void isAudioFile_detectsKnownExtensions() {
        assertTrue(scanner.isAudioFile(Path.of("a.mp3")));
        assertTrue(scanner.isAudioFile(Path.of("a.FLAC")));
        assertFalse(scanner.isAudioFile(Path.of("a.txt")));
        assertFalse(scanner.isAudioFile(Path.of("noextension")));
    }

    @Test
    void scan_findsAudioFilesInSampleLibrary() throws Exception {
        Path library = Path.of(
                getClass().getClassLoader()
                        .getResource("sample-library")
                        .toURI());

        List<Path> result = scanner.scan(library);

        assertEquals(4, result.size());

        assertTrue(result.stream().anyMatch(p -> p.endsWith("Creepy Nuts, Ayase, Lilas - ばかまじめ.mp3")));
        assertTrue(result.stream().anyMatch(p -> p.endsWith("YOASOBI - 夜に駆ける.mp3")));
        assertTrue(result.stream().anyMatch(p -> p.endsWith("ヨルシカ - あぶく.mp3")));
        assertTrue(result.stream().anyMatch(p -> p.endsWith("ヨルシカ - 歩く.mp3")));
    }

    @Test
    void scan_realMusicFolder() throws Exception {
        Path music = Path.of("C:/Users/PabloQ/Music");

        List<Path> result = scanner.scan(music);

        assertFalse(result.isEmpty());
    }
}