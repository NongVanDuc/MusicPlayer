package com.vanduc.musicplayer.interFace;

import com.vanduc.musicplayer.model.Song;

import java.util.ArrayList;

public interface ItemClickPlaySong {
    void onItemClickListener(ArrayList<Song> songList , int postion);
}
