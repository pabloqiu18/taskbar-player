package com.taskbarplayer;

import com.taskbarplayer.model.Song;
import com.taskbarplayer.service.LibraryService;
import com.taskbarplayer.service.PlaybackService;
import com.taskbarplayer.service.QueueService;
import javafx.application.Platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalPlayerApp {
    private final LibraryService libraryService = new LibraryService();
    private final PlaybackService playbackService = new PlaybackService();
    private final QueueService queueService = new QueueService();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Music folder: ");
            Path musicFolder = Path.of(scanner.nextLine());
            libraryService.setMusicDirectory(musicFolder);
            libraryService.scanLibrary();
            queueService.setQueue(libraryService.getSongs());
            if (queueService.isEmpty()) {
                System.out.println("No songs found.");
                return;
            }

            playbackService.setPlaybackListener(song -> {
                Song next = queueService.next();
                if (next != null) {
                    playbackService.play(next);
                    System.out.println("\nNow playing: " + next);
                }
            });

            boolean running = true;
            while (running) {
                Song current = queueService.getCurrentSong();
                System.out.println("----------------------------------------");
                System.out.println("Current : " + current);
                System.out.printf(
                        "Progress: %s / %s%n",
                        format(playbackService.getCurrentTime()),
                        format(current.getDuration()));
                System.out.printf("Volume  : %.0f%%%n", playbackService.getVolume() * 100);
                System.out.println("Muted   : " + playbackService.isMuted());
                System.out.println("Playing : " + playbackService.isPlaying());
                System.out.println("Shuffle : " + queueService.isShuffleEnabled());
                System.out.println("Repeat  : " + queueService.getRepeatMode());
                System.out.println("----------------------------------------");
                System.out.println("[p] Play / Pause");
                System.out.println("[n] Next");
                System.out.println("[b] Previous");
                System.out.println("[k] Seek");
                System.out.println("[u] Volume Up");
                System.out.println("[d] Volume Down");
                System.out.println("[m] Mute");
                System.out.println("[r] Repeat mode");
                System.out.println("[h] Toggle shuffle");
                System.out.println("[l] List current queue");
                System.out.println("[s] Stop");
                System.out.println("[q] Quit");
                System.out.print("> ");
                if (queueService.isEmpty()) {
                    System.out.println("Queue is empty.");
                } else {
                    switch (scanner.nextLine().trim().toLowerCase()) {
                        case "p" -> {
                            if (!playbackService.hasSongLoaded()) {
                                playbackService.play(current);
                            } else {
                                playbackService.togglePlayback();
                            }
                        }
                        case "n" -> {
                            Song next = queueService.next();
                            playbackService.play(next);
                        }
                        case "b" -> {
                            Song previous = queueService.previous();
                            playbackService.play(previous);
                        }
                        case "k" -> {
                            if (!playbackService.hasSongLoaded()) {
                                System.out.println("No song is selected.");
                                break;
                            }
                            System.out.print("Seek to progress %: ");
                            try {
                                double percent = Double.parseDouble(scanner.nextLine());
                                if (percent < 0 || percent > 100) {
                                    System.out.println("Value must be between 0 and 100.");
                                    break;
                                }
                                playbackService.seekToProgress(percent / 100.0);
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a number.");
                            }
                        }
                        case "u" -> {
                            playbackService.increaseVolume();
                            System.out.printf("Volume: %.0f%%%n",
                                    playbackService.getVolume() * 100);
                        }
                        case "d" -> {
                            playbackService.decreaseVolume();
                            System.out.printf("Volume: %.0f%%%n",
                                    playbackService.getVolume() * 100);
                        }
                        case "m" -> {
                            playbackService.mute();
                            System.out.println(
                                    playbackService.isMuted()
                                            ? "Muted"
                                            : "Unmuted");
                        }
                        case "r" -> {
                            QueueService.RepeatMode nextMode =
                                    switch (queueService.getRepeatMode()) {
                                        case OFF -> QueueService.RepeatMode.ALL;
                                        case ALL -> QueueService.RepeatMode.ONE;
                                        case ONE -> QueueService.RepeatMode.OFF;
                                    };
                            queueService.setRepeatMode(nextMode);
                            System.out.println("Repeat: " + nextMode);
                        }
                        case "h" -> {
                            queueService.toggleShuffle();
                            System.out.println(
                                    "Shuffle " +
                                            (queueService.isShuffleEnabled() ? "enabled" : "disabled"));
                        }
                        case "l" -> {
                            List<Song> active = queueService.getActiveQueue();
                            for (int i = 0; i < active.size(); i++) {
                                Song song = active.get(i);
                                System.out.println(
                                        (i == queueService.getCurrentIndex() ? "-> " : "   ")
                                                + i + ": " + song);
                            }
                        }
                        case "s" -> playbackService.stop();
                        case "q" -> {
                            playbackService.shutdown();
                            running = false;
                        }
                        default -> System.out.println("Unknown command.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String format(java.time.Duration duration) {

        long totalSeconds = duration.toSeconds();

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return "%d:%02d".formatted(minutes, seconds);
    }

    public static void main(String[] args) {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.WARNING);
        new TerminalPlayerApp().start();
    }
}