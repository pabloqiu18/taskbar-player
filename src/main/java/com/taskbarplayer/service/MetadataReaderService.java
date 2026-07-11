package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class MetadataReaderService {
    public Song read(Path file) {
        Path normalizedPath = file.toAbsolutePath().normalize();
        try {
            AudioFile audioFile = AudioFileIO.read(normalizedPath.toFile());
            Tag tag = audioFile.getTag();
            String title = firstNonBlank(tag == null ? null : tag.getFirst(FieldKey.TITLE), stripExtension(normalizedPath.getFileName().toString()));
            ParsedArtists parsedArtists = (tag == null) ? new ParsedArtists("Unknown artist", List.of()) : parseArtists(tag);
            String album = tag == null ? "" : nullToEmpty(tag.getFirst(FieldKey.ALBUM));
            int trackNumber = tag == null ? 0 : parseTrackNumber(tag.getFirst(FieldKey.TRACK));
            int year = tag == null ? 0 : parseYear(tag.getFirst(FieldKey.YEAR));
            List<String> genres = tag == null ? List.of() : parseGenres(tag.getFirst(FieldKey.GENRE));
            Duration duration = Duration.ZERO;
            if (audioFile.getAudioHeader() != null) {
                duration = Duration.ofSeconds(audioFile.getAudioHeader().getTrackLength());
            }
            return new Song(title, parsedArtists.artist(), parsedArtists.featuredArtists(), album, trackNumber, year, genres, duration, normalizedPath);
        } catch (CannotReadException e) {
            return fallbackSong(normalizedPath);
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackSong(normalizedPath);
        }
    }

    private Song fallbackSong(Path path) {
        return new Song(null, null, null, "", 0, 0, null, null, path);
    }

    private String firstNonBlank(String title, String filename) {
        return (title == null || title.isBlank()) ? filename : title;
    }

    private String nullToEmpty(String filename) {
        return filename == null ? "" : filename.trim();
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private int parseTrackNumber(String track) {
        if (track == null || track.isBlank()) {
            return 0;
        }
        track = track.trim();
        int slash = track.indexOf('/');
        if (slash >= 0) {
            track = track.substring(0, slash);
        }
        try {
            return Integer.parseInt(track);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // IMPORTANT make sure to test if YEAR field is standardized so year is the first value
    private int parseYear(String year) {
        if (year == null || year.isBlank()) {
            return 0;
        }
        year =  year.trim();
        if  (year.length() >= 4) {
            year =  year.substring(0, 4);
        }
        try {
            return Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<String> parseGenres(String genre) {
        if (genre == null || genre.isBlank()) {
            return List.of();
        }
        return Arrays.stream(genre.split("[,;/]")).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();
    }

    private ParsedArtists parseArtists(Tag tag) {

        String albumArtist = clean(tag.getFirst(FieldKey.ALBUM_ARTIST));
        String artistField = clean(tag.getFirst(FieldKey.ARTIST));

        String primary;

        if (!albumArtist.isBlank()) {
            primary = splitArtists(albumArtist).getFirst();
        } else {
            List<String> artists = splitArtists(artistField);

            if (artists.isEmpty()) {
                return new ParsedArtists("Unknown artist", List.of());
            }

            primary = artists.getFirst();
        }

        LinkedHashSet<String> featured = new LinkedHashSet<>();

        splitArtists(artistField)
                .stream()
                .skip(1)
                .forEach(featured::add);

        for (String value : tag.getAll(FieldKey.ARTISTS)) {
            splitArtists(value)
                    .stream()
                    .filter(a -> !a.equalsIgnoreCase(primary))
                    .forEach(featured::add);
        }

        return new ParsedArtists(primary, List.copyOf(featured));
    }

    private List<String> collectFeaturedArtists(String primaryArtist, List<String> albumArtists, List<String> artists) {
        return Stream.concat(albumArtists.stream(), artists.stream())
                .flatMap(a -> splitArtists(a).stream())
                .filter(a -> !a.equalsIgnoreCase(primaryArtist))
                .distinct()
                .toList();
    }

    private List<String> mergeFeaturedArtists(List<String> metadataArtists, List<String> parsedArtists) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(metadataArtists);
        merged.addAll(parsedArtists);
        return List.copyOf(merged);
    }

    private String clean(String artist) {
        if (artist == null) {
            return "";
        }
        return artist.trim().replaceAll("\\s+", " ");
    }

    private List<String> clean(List<String> artists) {
        if (artists == null) {
            return List.of();
        }
        return artists.stream().map(this::clean).filter(s -> !s.isBlank()).distinct().toList();
    }

    private ParsedArtists parseArtistString(String raw) {
        List<String> artists = splitArtists(raw);

        if (artists.isEmpty()) {
            return new ParsedArtists("Unknown artist", List.of());
        }

        return new ParsedArtists(
                artists.getFirst(),
                artists.stream().skip(1).toList()
        );
    }

    private List<String> splitArtists(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(
                        "(?i)\\s*(?:/|;|\\bfeat\\.?\\b|\\bft\\.?\\b|\\bfeaturing\\b)\\s*"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private record ParsedArtists(String artist, List<String> featuredArtists) {}
}


