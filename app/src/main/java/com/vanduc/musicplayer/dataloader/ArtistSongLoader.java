package com.vanduc.musicplayer.dataloader;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.vanduc.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistSongLoader {
        private static Cursor cursor = null;
        // Path
        private static final String[] projectionSongs = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION};
        private static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
        public static ArrayList<Song> getSongsForArtist(Context context,long artistID) {
            ArrayList<Song> mSongsList = new ArrayList<>();
            String selection = "is_music=1 AND title != '' AND artist_id=" + artistID;
            cursor = ((Activity) context).getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projectionSongs, selection, null, null);
            ArrayList<Song> generassongsList = new ArrayList<Song>();
            try {
                if (cursor != null && cursor.getCount() >= 1) {
                    int _id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int album_id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                    int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int display_name = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                    while (cursor.moveToNext()) {

                        int ID = cursor.getInt(_id);
                        String ARTIST = cursor.getString(artist);
                        String TITLE = cursor.getString(title);
                        String DISPLAY_NAME = cursor.getString(display_name);
                        String DURATION = cursor.getString(duration);
                        String Path = cursor.getString(data);
                        Song mSong = new Song(ID, album_id, ARTIST, TITLE, Path, DISPLAY_NAME, DURATION);
                        generassongsList.add(mSong);
                    }
                }
                closeCrs();
            } catch (Exception e) {
                closeCrs();
                e.printStackTrace();
            }
            return generassongsList;
        }
        private static void closeCrs() {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e("tmessages", e.toString());
                }
            }
        }

    }