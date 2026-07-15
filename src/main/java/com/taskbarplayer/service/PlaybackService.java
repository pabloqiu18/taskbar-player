package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.concurrent.CountDownLatch;

public class PlaybackService {
    private MediaPlayer player;
    private Song currentSong;
    private PlaybackListenerService playbackListener;
    private boolean javafxInitialized;
    private double volume = 0.3;
    private boolean muted = false;

    public PlaybackService() {
        initializeJavaFX();
    }

    private void initializeJavaFX() {
        if (javafxInitialized) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                javafxInitialized = true;
                latch.countDown();
            });
        } catch (IllegalStateException e) {
            // JavaFX is already running
            javafxInitialized = true;
            latch.countDown();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void play(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song is null");
        }
        Platform.runLater(() -> {
            disposePlayer();
            currentSong = song;
            Media media = new Media(song.getPath().toUri().toString());
            player = new MediaPlayer(media);
            player.setMute(muted);
            player.setVolume(volume);
            player.setOnReady(player::play);
            player.setOnEndOfMedia(() -> {
                if (playbackListener != null && currentSong != null) {
                    playbackListener.onSongFinished(currentSong);
                }
            });
            player.setOnError(() -> {
                if (player.getError() != null) {
                    System.err.println("Playback error: " + player.getError().getMessage());
                }
            });
            player.play();
        });
    }

    public void togglePlayback() {
        Platform.runLater(() -> {
            if (player == null) {
                return;
            }
            switch (player.getStatus()) {

                case PLAYING -> player.pause();
                case PAUSED,
                     READY,
                     STOPPED -> player.play();
                default -> {
                    // Ignore STALLED, HALTED, DISPOSED, UNKNOWN
                }
            }
        });
    }

    public void stop() {
        Platform.runLater(this::disposePlayer);
    }

    private void disposePlayer() {
        if (player == null) {
            return;
        }
        player.stop();
        player.dispose();
        player = null;
    }

    public boolean hasSongLoaded() {
        return player != null;
    }

    public boolean isPlaying() {
        return player != null &&
                player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isPaused() {
        return player != null &&
                player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isMuted() {
        return muted;
    }

    public void mute() {
        muted = !muted;
        Platform.runLater(() -> {
            if (player != null) {
                player.setMute(muted);
            }
        });
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        Platform.runLater(() -> {
            if (player != null) {
                player.setVolume(this.volume);
            }
        });
    }

    public void increaseVolume() {
        setVolume(getVolume() + 0.1);
    }

    public void decreaseVolume() {
        setVolume(getVolume() - 0.1);
    }

    public void setPlaybackListener(PlaybackListenerService playbackListener) {
        this.playbackListener = playbackListener;
    }

    public void shutdown() {
        Platform.runLater(() -> {
            disposePlayer();
            Platform.exit();
        });
    }
}