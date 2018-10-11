package com.vanduc.musicplayer.model;

public class Playlist {
        private long id;
        private  String name;
        private  int songCount;

        public Playlist() {
        }

        public Playlist(long _id, String _name, int _songCount) {
            this.id = _id;
            this.name = _name;
            this.songCount = _songCount;
        }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }
}
