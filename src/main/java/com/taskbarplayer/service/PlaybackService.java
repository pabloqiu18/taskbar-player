package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class PlaybackService {
    private MediaPlayer player;
    private Song currentSong;
    private PlaybackListenerService playbackListener;
    private boolean javafxInitialized;
    private double volume = 0.3;
    private boolean muted = false;
    private ObjectProperty<Song> currentSongProperty = new SimpleObjectProperty<>();
    private BooleanProperty playingProperty = new SimpleBooleanProperty();
    private DoubleProperty volumeProperty = new SimpleDoubleProperty(0.3);

    public PlaybackService() {
        initializeJavaFX();
    }

    public ReadOnlyObjectProperty<Song> currentSongProperty() {
        return currentSongProperty;
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
            currentSongProperty.set(song);
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
        currentSong = null;
        currentSongProperty.set(null);
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
        this.volume = Math.clamp(volume, 0.0, 1.0);
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

    public Duration getCurrentTime() {
        if (player == null) {
            return Duration.ZERO;
        }
        return Duration.ofMillis((long) player.getCurrentTime().toMillis());
    }

    public double getProgress() {
        if (player == null) {
            return 0.0;
        }
        Duration duration = currentSong.getDuration();
        if (duration.isZero()) {
            return 0.0;
        }
        return (double) getCurrentTime().toMillis() / (double) duration.toMillis();
    }

    public void seek(Duration position) {
        if (player == null) {
            return;
        }
        if (position.isNegative()) {
            position = Duration.ZERO;
        }
        Duration duration = currentSong.getDuration();
        if (position.compareTo(duration) > 0) {
            position = duration;
        }
        Duration finalPosition = position;
        Platform.runLater(() -> player.seek(javafx.util.Duration.millis(finalPosition.toMillis())));
    }

    public void seekToProgress(double progress) {
        if (player == null) {
            return;
        }
        progress = Math.clamp(progress, 0.0, 1.0);
        long timestamp = Math.round(currentSong.getDuration().toMillis() * progress);
        seek(Duration.ofMillis(timestamp));
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