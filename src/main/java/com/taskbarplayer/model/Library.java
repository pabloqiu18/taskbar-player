package com.taskbarplayer.model;

import java.nio.file.Path;
import java.util.*;

public class Library {
    private Path musicRoot;
    private final Map<Path, Song> songsByPath = new LinkedHashMap<>();
    private final Map<UUID, Song> songsByID = new HashMap<>();
    private final Map<Path, Long> lastModifiedByPath = new HashMap<>();

    public Optional<Path> getMusicRoot() {
        return Optional.ofNullable(musicRoot);
    }

    public void setMusicRoot(Path musicRoot) {
        this.musicRoot = musicRoot;
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songsByPath.values());
    }

    public int size() {
        return songsByPath.size();
    }

    public boolean isEmpty() {
        return songsByPath.isEmpty();
    }

    public Optional<Song> findByPath(Path path) {
        return Optional.ofNullable(songsByPath.get(normalize(path)));
    }

    public Optional<Song> findByID(UUID id) {
        return Optional.ofNullable(songsByID.get(id));
    }

    public Optional<Long> getLastModified(Path path) {
        return Optional.ofNullable(lastModifiedByPath.get(normalize(path)));
    }

    public void upsert(Song song, long lastModifiedMillis) {
        Path path = normalize(song.getPath());
        Song previousAtPath = songsByPath.get(path);
        if (previousAtPath != null) {
            songsByID.remove(previousAtPath.getID());
        }
        Song previousWithSameID = songsByID.get(song.getID());
        if (previousWithSameID != null && !previousWithSameID.getPath().equals(path)) {
            songsByPath.remove(normalize(previousWithSameID.getPath()));
            lastModifiedByPath.remove(normalize(previousWithSameID.getPath()));
        }
        songsByPath.put(path, song);
        songsByID.put(song.getID(), song);
        lastModifiedByPath.put(path, lastModifiedMillis);
    }

    public void removePath(Path path) {
        Path normalized = normalize(path);
        Song removed = songsByPath.remove(normalized);
        lastModifiedByPath.remove(normalized);
        if (removed != null) {
            songsByID.remove(removed.getID());
        }
    }

    public void clear() {
        songsByPath.clear();
        songsByID.clear();
        lastModifiedByPath.clear();
        musicRoot = null;
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
