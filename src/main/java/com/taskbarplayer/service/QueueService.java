package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueService {
    private final List<Song> queue = new ArrayList<>();
    private final List<Song> shuffleOrder = new ArrayList<>();
    private int currentIndex = -1;
    private boolean shuffleEnabled = false;
    public enum RepeatMode {
        OFF,
        ALL,
        ONE
    }
    private RepeatMode repeatMode = RepeatMode.ALL;

    public void setQueue(List<Song> songs) {
        queue.clear();
        queue.addAll(songs);
        shuffleOrder.clear();
        currentIndex = queue.isEmpty() ? -1 : 0;
        if (shuffleEnabled) {
            generateShuffleOrder(queue.get(currentIndex));
        }
    }

    public List<Song> getActiveQueue() {
        return List.copyOf(activeQueue());
    }

    private void generateShuffleOrder(Song current) {
        shuffleOrder.clear();
        if (queue.isEmpty()) {
            return;
        }
        shuffleOrder.addAll(queue);
        shuffleOrder.remove(current);
        Collections.shuffle(shuffleOrder);
        shuffleOrder.addFirst(current);
        currentIndex = 0;
    }

    public Song getCurrentSong() {
        List<Song> active = activeQueue();
        if (currentIndex < 0 || currentIndex >= active.size()) {
            return null;
        }
        return active.get(currentIndex);
    }

    public Song next() {
        List<Song> active = activeQueue();
        if (repeatMode == RepeatMode.ONE) {
            return active.get(currentIndex);
        } else if (repeatMode == RepeatMode.ALL) {
            if (active.isEmpty()) {
                return null;
            }
            currentIndex++;
            if (currentIndex >= active.size()) {
                currentIndex = 0;
            }
            return active.get(currentIndex);
        } else if (repeatMode == RepeatMode.OFF) {
            currentIndex++;
            if (active.isEmpty() || currentIndex >= active.size()) {
                currentIndex = active.size() - 1;
                return null;
            }
            return active.get(currentIndex);
        }
        return null;
    }

    public Song previous() {
        List<Song> active = activeQueue();
        if (repeatMode == RepeatMode.ONE) {
            return active.get(currentIndex);
        } else if (repeatMode == RepeatMode.ALL) {
            if (active.isEmpty()) {
                return null;
            }
            currentIndex--;
            if (currentIndex < 0) {
                currentIndex = active.size() - 1;
            }
            return active.get(currentIndex);
        } else if (repeatMode == RepeatMode.OFF) {
            if (active.isEmpty()) {
                return null;
            }
            if (currentIndex == 0) {
                if (shuffleEnabled) {
                    Song last =  shuffleOrder.removeLast();
                    shuffleOrder.addFirst(last);
                    return shuffleOrder.getFirst();
                }
                return active.get(currentIndex);
            } else {
                currentIndex--;
                return active.get(currentIndex);
            }
        }
        return null;
    }

    public Song setCurrentSong(int index) {
        if (index < 0 || index >= queue.size()) {
            throw new IndexOutOfBoundsException();
        }
        Song song = queue.get(index);
        if (shuffleEnabled) {
            currentIndex = shuffleOrder.indexOf(song);
        } else {
            currentIndex = index;
        }
        return song;
    }

    public void clear() {
        queue.clear();
        shuffleOrder.clear();
        currentIndex = -1;
    }

    public boolean isEmpty() { return queue.isEmpty(); }

    public void setRepeatMode(RepeatMode repeatMode) {
        if (repeatMode == null) {
            throw new IllegalArgumentException("Repeat mode is null");
        }
        this.repeatMode = repeatMode;
    }

    public RepeatMode getRepeatMode() { return repeatMode; }

    public void toggleShuffle() {
        Song current = getCurrentSong();
        shuffleEnabled = !shuffleEnabled;
        if (shuffleEnabled) {
            generateShuffleOrder(current);
        } else {
            currentIndex = queue.indexOf(current);
        }
    }

    public boolean isShuffleEnabled() { return shuffleEnabled; }

    public int size() { return queue.size(); }

    private List<Song> activeQueue() {
        return shuffleEnabled ? shuffleOrder : queue;
    }

    public int getCurrentIndex() { return currentIndex; }
}
