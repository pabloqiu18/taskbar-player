package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private LibraryService libraryService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService();
    }

    @Test
    void scanLibrary_requiresDirectoryFirst() {
        assertThrows(IllegalStateException.class, () -> libraryService.scanLibrary());
    }

    @Test
    void scanLibrary_indexesAudioFiles() throws Exception {
        libraryService.setMusicDirectory(getSampleLibrary());

        libraryService.scanLibrary();

        List<Song> songs = libraryService.getSongs();

        assertEquals(4, songs.size());

        assertTrue(songs.stream().anyMatch(s -> s.getTitle().equals("ばかまじめ")));
        assertTrue(songs.stream().anyMatch(s -> s.getTitle().equals("夜に駆ける")));
        assertTrue(songs.stream().anyMatch(s -> s.getTitle().equals("あぶく")));
        assertTrue(songs.stream().anyMatch(s -> s.getTitle().equals("歩く")));
    }

    @Test
    void scanLibrary_skipsUnchangedFiles() throws Exception {
        libraryService.setMusicDirectory(getSampleLibrary());
        libraryService.scanLibrary();

        Path file = getSampleLibrary().resolve("ヨルシカ - あぶく.mp3");
        Song first = libraryService.findByPath(file).orElseThrow();
        libraryService.scanLibrary();
        Song second = libraryService.findByPath(file).orElseThrow();

        assertEquals(first.getID(), second.getID());
    }

    @Test
    void scanLibrary_reindexesChangedFiles() throws Exception {
        copyDirectory(getSampleLibrary(), tempDir);
        Path file = tempDir.resolve("nested/ヨルシカ - 歩く.mp3");

        libraryService.setMusicDirectory(tempDir);
        libraryService.scanLibrary();

        Song before = libraryService.findByPath(file).orElseThrow();
        long oldTimestamp = libraryService.getLibrary()
                .getLastModified(file)
                .orElseThrow();

        Thread.sleep(1100); // ensure filesystem timestamp changes

        Files.write(file, new byte[]{1, 2, 3}, StandardOpenOption.APPEND);
        libraryService.scanLibrary();
        Song after = libraryService.findByPath(file).orElseThrow();

        long newTimestamp = libraryService.getLibrary()
                .getLastModified(file)
                .orElseThrow();

        assertTrue(newTimestamp > oldTimestamp);
        assertEquals(before.getID(), after.getID());
    }

    @Test
    void scanLibrary_removesDeletedFiles() throws Exception {
        copyDirectory(getSampleLibrary(), tempDir);
        libraryService.setMusicDirectory(tempDir);
        libraryService.scanLibrary();

        Path remove = tempDir.resolve("ヨルシカ - あぶく.mp3");
        Files.delete(remove);
        libraryService.scanLibrary();

        assertEquals(3, libraryService.getSongs().size());
        assertTrue(libraryService.findByPath(remove).isEmpty());
    }

    @Test
    void findById_returnsIndexedSong() throws Exception {
        libraryService.setMusicDirectory(getSampleLibrary());
        libraryService.scanLibrary();

        Path file = getSampleLibrary().resolve("nested/YOASOBI - 夜に駆ける.mp3");
        Song song = libraryService.findByPath(file).orElseThrow();

        assertTrue(libraryService.findByID(song.getID()).isPresent());
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path destination = target.resolve(source.relativize(path));

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Path getSampleLibrary() throws Exception {
        return Path.of(
                getClass()
                        .getClassLoader()
                        .getResource("sample-library")
                        .toURI());
    }
}