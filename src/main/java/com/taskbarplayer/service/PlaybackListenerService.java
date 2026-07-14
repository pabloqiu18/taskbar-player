package com.taskbarplayer.service;

import com.taskbarplayer.model.Song;

public interface PlaybackListenerService {

    void onSongFinished(Song song);

}