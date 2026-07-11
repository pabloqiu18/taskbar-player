package com.taskbarplayer.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {

    @Test
    void generateID_isStableForSameMetadata() {
        UUID first = Song.generateID("Test Song", "Artist A", List.of("Feat B"), 2020);
        UUID second = Song.generateID("Test Song", "Artist A", List.of("Feat B"), 2020);
        assertEquals(first, second);
    }

    @Test
    void generateID_changesWhenFeaturedArtistChanges() {
        UUID withoutFeature = Song.generateID("Song", "Artist", List.of(), 2020);
        UUID withFeature = Song.generateID("Song", "Artist", List.of("Guest"), 2020);
        assertNotEquals(withoutFeature, withFeature);
    }

    @Test
    void constructorDerivesTitleFromFilenameWhenMissing() {
        Path path = Path.of("music/01 - Mystery Track.mp3");

        Song song = new Song(null, "Artist", List.of(), "Album", 1, 2020, List.of(), Duration.ZERO, path);

        assertEquals("01 - Mystery Track", song.getTitle());
        assertEquals("Artist", song.getArtist());
        assertNotNull(song.getID());
    }
}