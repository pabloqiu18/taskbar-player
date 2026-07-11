package com.taskbarplayer.service;

import com.taskbarplayer.model.Library;
import com.taskbarplayer.model.Song;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LibraryService {
    private final FileScannerService fileScannerService;
    private final MetadataReaderService metadataReaderService;
    private final Library library;

    public LibraryService() {
        this(new FileScannerService(), new MetadataReaderService(), new Library());
    }

    public LibraryService(FileScannerService fileScannerService, MetadataReaderService metadataReaderService, Library library) {
        this.fileScannerService = fileScannerService;
        this.metadataReaderService = metadataReaderService;
        this.library = library;
    }

    public Library getLibrary() {
        return library;
    }

    public void setMusicDirectory(Path root) {
        if (root == null || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }
        library.setMusicRoot(root.toAbsolutePath().normalize());
    }

    public Optional<Path> getMusicDirectory() {
        return library.getMusicRoot();
    }

    public void scanLibrary() throws IOException {
        Path root = library.getMusicRoot().orElseThrow(() -> new IllegalStateException("Music directory not set"));
        List <Path> files = fileScannerService.scan(root);
        Set<Path> seenPaths = new HashSet<>();
        for (Path file : files) {
            seenPaths.add(file);
            long modified = Files.getLastModifiedTime(file).toMillis();
            Optional<Long> knownModified = library.getLastModified(file);
            if (knownModified.isPresent() && knownModified.get() == modified && library.findByPath(file).isPresent()) continue;
            Song song = metadataReaderService.read(file);
            library.upsert(song, modified);
        }
        Set<Path> stalePaths = new HashSet<>();
        for (Song song : library.getSongs()) {
            if (!seenPaths.contains(song.getPath())) {
                stalePaths.add(song.getPath());
            }
        }
        stalePaths.forEach(library::removePath);
    }

    public List<Song> getSongs() {
        return library.getSongs();
    }

    public Optional<Song> findByPath(Path path) {
        return library.findByPath(path);
    }

    public Optional<Song> findByID(UUID id) {
        return library.findByID(id);
    }

    public void clearLibrary() {
        library.clear();
    }
}
