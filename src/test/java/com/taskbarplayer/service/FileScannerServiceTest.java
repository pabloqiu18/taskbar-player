package com.taskbarplayer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileScannerServiceTest {

    private final FileScannerService scanner = new FileScannerService();

    private Path sampleLibrary;

    @BeforeEach
    void setUp() throws URISyntaxException {
        sampleLibrary = Path.of(
                getClass()
                        .getClassLoader()
                        .getResource("sample-library")
                        .toURI());
    }

    @Test
    void scan_findsAllAudioFilesInSampleLibrary() throws Exception {
        List<Path> result = scanner.scan(sampleLibrary);
        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(p ->
                p.getFileName().toString().equals("Monkeys Spinning Monkeys (Kevin MacLeod).mp3")));
        assertTrue(result.stream().anyMatch(p ->
                p.getFileName().toString().equals("Sneaky Snitch (Kevin MacLeod).mp3")));
        assertTrue(result.stream().anyMatch(p ->
                p.getFileName().toString().equals("Bagatelle no. 25 ''Für Elise'', WoO 59.mp3")));
        assertTrue(result.stream().anyMatch(p ->
                p.getFileName().toString().equals("Piano Sonata no. 11, K. 331 - III. Alla Turca.mp3")));
    }

    @Test
    void scan_findsFilesRecursively() throws Exception {
        List<Path> result = scanner.scan(sampleLibrary);
        assertTrue(result.stream().anyMatch(p ->
                p.getParent().getFileName().toString().equals("nested")));
    }

    @Test
    void scan_rejectsNonDirectory(@TempDir Path tempDir) throws Exception {
        Path file = Files.createFile(tempDir.resolve("file.txt"));
        assertThrows(
                IllegalArgumentException.class,
                () -> scanner.scan(file));
    }

    @Test
    void isAudioFile_detectsSupportedExtensions() {
        assertAll(
                () -> assertTrue(scanner.isAudioFile(Path.of("song.mp3"))),
                () -> assertTrue(scanner.isAudioFile(Path.of("song.flac"))),
                () -> assertFalse(scanner.isAudioFile(Path.of("song.txt"))),
                () -> assertFalse(scanner.isAudioFile(Path.of("song")))
        );
    }
}