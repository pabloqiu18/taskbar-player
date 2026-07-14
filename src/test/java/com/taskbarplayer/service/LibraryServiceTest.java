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
    private Path sampleLibrary;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        libraryService = new LibraryService();
        sampleLibrary = Path.of(
                getClass()
                        .getClassLoader()
                        .getResource("sample-library")
                        .toURI());
    }

    @Test
    void scanLibrary_requiresDirectoryFirst() {
        assertThrows(IllegalStateException.class, () -> libraryService.scanLibrary());
    }

    @Test
    void scanLibrary_indexesAudioFiles() throws Exception {
        libraryService.setMusicDirectory(sampleLibrary);
        libraryService.scanLibrary();
        List<Song> songs = libraryService.getSongs();
        assertEquals(4, songs.size());
        assertTrue(songs.stream().anyMatch(s ->
                s.getPath().getFileName().toString().equals("Monkeys Spinning Monkeys (Kevin MacLeod).mp3")));
        assertTrue(songs.stream().anyMatch(s ->
                s.getPath().getFileName().toString().equals("Sneaky Snitch (Kevin MacLeod).mp3")));
        assertTrue(songs.stream().anyMatch(s ->
                s.getPath().getFileName().toString().equals("Bagatelle no. 25 ''Für Elise'', WoO 59.mp3")));
        assertTrue(songs.stream().anyMatch(s ->
                s.getPath().getFileName().toString().equals("Piano Sonata no. 11, K. 331 - III. Alla Turca.mp3")));
    }

    @Test
    void scanLibrary_skipsUnchangedFiles() throws Exception {
        libraryService.setMusicDirectory(sampleLibrary);
        libraryService.scanLibrary();

        Path file = sampleLibrary.resolve("Monkeys Spinning Monkeys (Kevin MacLeod).mp3");
        Song first = libraryService.findByPath(file).orElseThrow();
        libraryService.scanLibrary();
        Song second = libraryService.findByPath(file).orElseThrow();
        assertEquals(first.getID(), second.getID());
    }

    @Test
    void scanLibrary_reindexesChangedFiles() throws Exception {
        copyDirectory(sampleLibrary, tempDir);
        Path file = tempDir.resolve("nested/Bagatelle no. 25 ''Für Elise'', WoO 59.mp3");
        libraryService.setMusicDirectory(tempDir);
        libraryService.scanLibrary();
        Song before = libraryService.findByPath(file).orElseThrow();
        long oldTimestamp = libraryService.getLibrary()
                .getLastModified(file)
                .orElseThrow();
        Thread.sleep(1100);
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
        copyDirectory(sampleLibrary, tempDir);
        libraryService.setMusicDirectory(tempDir);
        libraryService.scanLibrary();
        Path remove = tempDir.resolve("Sneaky Snitch (Kevin MacLeod).mp3");
        Files.delete(remove);
        libraryService.scanLibrary();
        assertEquals(3, libraryService.getSongs().size());
        assertTrue(libraryService.findByPath(remove).isEmpty());
    }

    @Test
    void findById_returnsIndexedSong() throws Exception {
        libraryService.setMusicDirectory(sampleLibrary);
        libraryService.scanLibrary();
        Path file = sampleLibrary.resolve("nested/Piano Sonata no. 11, K. 331 - III. Alla Turca.mp3");
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
}