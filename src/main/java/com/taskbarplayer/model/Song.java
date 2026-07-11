package com.taskbarplayer.model;

import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class Song {
    private UUID id;
    private String title;
    private String artist;
    private List<String> featuredArtists = new ArrayList<>();
    private String album;
    private int trackNumber;
    private int year;
    private List<String> tags = new  ArrayList<>();
    private Duration duration;
    private Path path;

    public Song() {
        this.title = "Fallback Song";
        this.path = null;
        this.artist = "Fallback Artist";
        this.album = "";
        this.duration = Duration.ZERO;
        this.id = generateID(this.title, this.artist, this.featuredArtists, this.year);
    }

    public Song(String title, String artist, List<String> featuredArtists, String album, int trackNumber, int year, List<String> tags, Duration duration, Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        this.title = deriveTitle(title, path);
        this.path = path;
        this.artist = (artist == null || artist.isBlank()) ? "Unknown artist" : artist;
        this.featuredArtists = (featuredArtists == null || featuredArtists.isEmpty()) ? new ArrayList<>() : new ArrayList<>(featuredArtists);
        this.album = album == null ? "" : album;
        this.trackNumber = trackNumber;
        this.year = year;
        this.tags = (tags == null || tags.isEmpty()) ? new  ArrayList<>() : new ArrayList<>(tags);
        this.duration = duration == null ? Duration.ZERO : duration;
        this.id = generateID(this.title, this.artist, this.featuredArtists, this.year);
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")      // Remove accents
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");      // Collapse whitespace
    }

    private static String deriveTitle(String title, Path path) {
        if (title != null && !title.isBlank()) {
            return title;
        }
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }

    public static UUID generateID(String title, String artist, List<String> featuredArtists, int year) {
        List<String> features = new ArrayList<>();
        String normalized;
        if (featuredArtists != null && !featuredArtists.isEmpty()) {
            features = featuredArtists.stream().map(Song::normalize).sorted().toList();
            normalized = normalize(title) + "|" + normalize(artist) + "|" + String.join("|", features) + "|" + year;
        } else {
            normalized = normalize(title) + "|" + normalize(artist) + "|" + year;
        }
        return UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8));
    }

    public UUID getID() { return id; }

    protected void refreshID() {
        this.id = generateID(this.title, this.artist, this.featuredArtists, this.year);
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = deriveTitle(title, this.path); }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = (artist ==  null || artist.isBlank()) ? "Unknown artist" : artist; }

    public List<String> getFeaturedArtists() { return this.featuredArtists; }
    public void setFeaturedArtists(List<String> featuredArtists) {
        this.featuredArtists = featuredArtists == null ? new ArrayList<>() : new ArrayList<>(featuredArtists);
    }
    public void addFeaturedArtist(String featuredArtist) {
        if  (featuredArtist != null && !featuredArtist.isBlank() && !this.featuredArtists.contains(featuredArtist)) {
            this.featuredArtists.add(featuredArtist);
        }
    }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album == null ? "" : album; }

    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }
    public void addTag(String tag) { this.tags.add(tag); }

    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration ==  null ? Duration.ZERO : duration; }

    public Path getPath() { return path; }
    public void setPath(Path path) { this.path = path; }

    @Override
    public String toString() {
        if (this.featuredArtists.isEmpty()) {
            return artist + " - " + title;
        }
        return artist + "ft. " + String.join(", ", featuredArtists) + " - " + title;
    }
}