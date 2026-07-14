package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

public class QueueService {
    private final List<Song> queue = new ArrayList<>();
    private int currentIndex = -1;

    public void setQueue(List<Song> songs) {
        queue.clear();
        queue.addAll(songs);
        currentIndex = queue.isEmpty() ? -1 : 0;
    }

    public Song getCurrentSong() {
        if (currentIndex < 0 || currentIndex >= queue.size()) {
            return null;
        }
        return queue.get(currentIndex);
    }

    public Song next() {
        if (queue.isEmpty()) {
            return null;
        }
        currentIndex++;

        if (currentIndex >= queue.size()) {
            currentIndex = 0;
        }
        return queue.get(currentIndex);
    }

    public Song previous() {
        if (queue.isEmpty()) {
            return null;
        }
        currentIndex--;

        if (currentIndex < 0) {
            currentIndex = queue.size() - 1;
        }
        return queue.get(currentIndex);
    }

    public Song setCurrentSong(int index) {
        if (index < 0 || index >= queue.size()) {
            throw new IndexOutOfBoundsException();
        }
        currentIndex = index;
        return queue.get(currentIndex);
    }

    public void clear() {
        queue.clear();
        currentIndex = -1;
    }

    public boolean isEmpty() { return queue.isEmpty(); }

    public int size() { return queue.size(); }

    public List<Song> getQueue() { return List.copyOf(queue); }

    public int getCurrentIndex() { return currentIndex; }
}
