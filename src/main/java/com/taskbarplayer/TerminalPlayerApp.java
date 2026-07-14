package com.taskbarplayer;

import com.taskbarplayer.model.Song;
import com.taskbarplayer.service.LibraryService;
import com.taskbarplayer.service.PlaybackService;
import com.taskbarplayer.service.QueueService;

import java.nio.file.Path;
import java.util.Scanner;

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
                System.out.printf("Volume  : %.0f%%%n", playbackService.getVolume() * 100);
                System.out.println("Muted   : " + playbackService.isMuted());
                System.out.println("Playing : " + playbackService.isPlaying());
                System.out.println("----------------------------------------");
                System.out.println("[p] Play / Pause");
                System.out.println("[n] Next");
                System.out.println("[b] Previous");
                System.out.println("[u] Volume Up");
                System.out.println("[d] Volume Down");
                System.out.println("[m] Mute");
                System.out.println("[l] List songs");
                System.out.println("[s] Stop");
                System.out.println("[q] Quit");
                System.out.print("> ");

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

                    case "l" -> {

                        for (int i = 0; i < queueService.size(); i++) {

                            Song song = queueService.getQueue().get(i);

                            if (i == queueService.getCurrentIndex()) {
                                System.out.println("-> " + i + ": " + song);
                            } else {
                                System.out.println("   " + i + ": " + song);
                            }
                        }
                    }

                    case "s" -> playbackService.stop();

                    case "q" -> {
                        playbackService.stop();
                        running = false;
                    }

                    default -> System.out.println("Unknown command.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TerminalPlayerApp().start();
    }
}