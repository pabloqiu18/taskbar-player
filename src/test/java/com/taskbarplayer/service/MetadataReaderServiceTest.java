package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class MetadataReaderServiceTest {

    private final MetadataReaderService reader = new MetadataReaderService();

    @TempDir
    Path tempDir;

    @Test
    void read_fallbacksWhenFileIsNotReadableAudio() throws IOException {
        Path file = tempDir.resolve("01 Cool Song.mp3");
        Files.writeString(file, "not really audio");

        Song song = reader.read(file);

        assertEquals("01 Cool Song", song.getTitle());
        assertEquals("Unknown artist", song.getArtist());
        assertTrue(song.getFeaturedArtists().isEmpty());
        assertEquals(file.toAbsolutePath().normalize(), song.getPath());
        assertNotNull(song.getID());
    }

    @Test
    void read_readsRealMetadata() throws Exception {
        Path file = getSampleLibrary().resolve("ヨルシカ - あぶく.mp3");

        Song song = reader.read(file);

        assertEquals("あぶく", song.getTitle());
        assertEquals("ヨルシカ", song.getArtist());

        assertNotNull(song.getAlbum());
        assertNotNull(song.getTags());
        assertNotNull(song.getID());
        assertEquals(file.toAbsolutePath().normalize(), song.getPath());
    }

    //@Disabled("Developer utility")
    @Test
    void dumpStandardMetadataFields() throws Exception {
        Path file = getSampleLibrary().resolve("Creepy Nuts, Ayase, Lilas - ばかまじめ.mp3");

        AudioFile audio = AudioFileIO.read(file.toFile());

        System.out.println("===== " + file.getFileName() + " =====");

        for (FieldKey key : FieldKey.values()) {
            try {
                String value = audio.getTag().getFirst(key);
                if (!value.isBlank()) {
                    System.out.printf("%-25s : %s%n", key, value);
                }
            } catch (Exception ignored) {
            }
        }
    }

    //@Disabled("Developer utility")
    @Test
    void dumpParsedSong() throws Exception {
        Path file = getSampleLibrary().resolve("Creepy Nuts, Ayase, Lilas - ばかまじめ.mp3");

        Song song = reader.read(file);

        System.out.println("===== PARSED SONG =====");
        System.out.println("ID               : " + song.getID());
        System.out.println("Title            : " + song.getTitle());
        System.out.println("Artist           : " + song.getArtist());
        System.out.println("Featured Artists : " + song.getFeaturedArtists());
        System.out.println("Album            : " + song.getAlbum());
        System.out.println("Track            : " + song.getTrackNumber());
        System.out.println("Year             : " + song.getYear());
        System.out.println("Genres           : " + song.getTags());
        System.out.println("Duration         : " + song.getDuration());
        System.out.println("Path             : " + song.getPath());
    }

    //@Disabled("Developer utility")
    @Test
    void dumpRawTagFrames() throws Exception {
        Path file = getSampleLibrary().resolve("ヨルシカ - あぶく.mp3");

        AudioFile audio = AudioFileIO.read(file.toFile());

        System.out.println("===== RAW TAGS =====");

        Iterator<TagField> iterator = audio.getTag().getFields();

        while (iterator.hasNext()) {
            TagField field = iterator.next();
            System.out.println(field);
        }
    }

    private Path getSampleLibrary() throws Exception {
        return Path.of(
                getClass()
                        .getClassLoader()
                        .getResource("sample-library")
                        .toURI());
    }
}