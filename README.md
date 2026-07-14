# taskbar-player

> Local music player for Windows focused on fast playback, customizable layouts, and seamless local library management.

[![License](https://img.shields.io/github/license/pabloqiu18/taskbar-player)](https://mit-license.org/)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)
![Version](https://img.shields.io/badge/Version-v0.1.0-blue)

---

## Features

- Scan and index local music libraries
- Read audio metadata automatically
- Fast local playback
- Playlist support
- Shuffle and repeat modes
- Playback persistence
- Play count and listening statistics
- Customizable layouts
- Mini player
- Transparent always-on-top taskbar player
- Built-in metadata editor
- Optional synchronization with Android
- Local network "Jam" sessions

---

## Tech Stack

- **Java 21**
- **JavaFX** - Desktop UI
- **Gradle** - Build system
- **JUnit 5** - Testing
- **Jaudiotagger** - Audio metadata

---


## Project Structure

```text
taskbar-player/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   │
│   └── test/
│       ├── java/
│       │   ├── controller/
│       │   ├── model/
│       │   ├── service/
│       │   └── util/
│       └── resources/
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

---

## Roadmap

### Library

- [x] Choose music folder
- [x] Recursively scan audio files
- [x] Read metadata
- [x] Build in-memory music library
- [ ] Detect library changes automatically
- [ ] Playlists

### Playback

- [x] Display songs
- [x] Play songs
- [x] Pause / Resume
- [x] Stop
- [ ] Seek within songs
- [x] Volume control
- [x] Mute
- [ ] Repeat (Off / One / All)
- [ ] Shuffle

### Queue

- [x] Play next
- [ ] Queue management
- [ ] Drag-and-drop queue reordering
- [ ] Clear queue
- [ ] Queue persistence

### Playlists

- [ ] Create playlists
- [ ] Rename playlists
- [ ] Delete playlists
- [ ] Reorder songs
- [ ] Import / Export playlists
- [ ] Smart playlists

### User Interface

- [ ] Search
- [ ] Sorting (Time added / Artist / Title / Duration / Custom)
- [ ] Filtering
- [ ] Album artwork
- [ ] Multiple layout options (Container based)
- [ ] Resizable interface
- [ ] Mini player
- [ ] Always-on-top mode
- [ ] Transparent taskbar player
- [ ] Keyboard shortcuts
- [ ] System tray support

### Metadata

- [ ] Metadata editor
- [ ] Album artwork editor
- [ ] Batch editing
- [ ] Automatic metadata cleanup

### Persistence

- [ ] Remember current song
- [ ] Remember playback position
- [ ] Remember queue
- [ ] Remember playlists
- [ ] Play count tracking
- [ ] Total listening time
- [ ] Last played timestamps
- [ ] Recently played

### Android

- [ ] Android app
- [ ] Library synchronization
- [ ] Export / Import library state
- [ ] Merge listening history

### Local Network

- [ ] LAN "Jam" sessions
- [ ] Host / Join sessions
- [ ] Shared playback controls

---

## License

This project is licensed under the [MIT License](https://mit-license.org/).