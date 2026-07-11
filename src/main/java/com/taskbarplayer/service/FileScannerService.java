package com.taskbarplayer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class FileScannerService {
    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
            "aac", "flac", "m4a", "mp3", "ogg", "opus", "wav", "wma"
    );

    public List<Path> scan(Path directory) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        try (Stream<Path> walk = Files.walk(normalizedDirectory)) {
            return walk.filter(Files::isRegularFile).filter(this::isAudioFile)
                    .map(path -> path.toAbsolutePath().normalize()).sorted().toList();
        }
    }

    public boolean isAudioFile(Path file) {
        String name =  file.getFileName().toString();
        int dot =  name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) {
            return false;
        }
        String extension = name.substring(dot + 1).toLowerCase();
        return AUDIO_EXTENSIONS.contains(extension);
    }
}
