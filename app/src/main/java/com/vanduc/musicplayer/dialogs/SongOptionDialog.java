package com.vanduc.musicplayer.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.fragments.FragmentListPlay;
import com.vanduc.musicplayer.fragments.FragmentSong;
import com.vanduc.musicplayer.interFace.IconClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongOptionDialog extends DialogFragment implements IconClickListener, View.OnClickListener {
    private List<Song> songList;
    private int postion = -1;
    LinearLayout mlrlPlayAll;

    public static SongOptionDialog newInstance() {
        return newInstance((Song) null);
    }

    public static SongOptionDialog newInstance(Song song) {
        long[] songs;
        if (song == null) {
            songs = new long[0];
        } else {
            songs = new long[1];
            songs[0] = song.id;
        }
        return newInstance(songs);
    }

    public int getPostion() {
        return postion;
    }

    public static SongOptionDialog newInstance(long[] songList) {
        SongOptionDialog dialog = new SongOptionDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentSong fragmentSong = new FragmentSong();
        fragmentSong.setmIconClickListener(this);
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity()).customView(R.layout.item_dialog_song_option, true).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(final MaterialDialog dialog, final View itemView, int which, CharSequence text) {

            }
        }).build();
        View view = materialDialog.getCustomView();
        mlrlPlayAll = view.findViewById(R.id.lrl_play_all);
        Log.e("onItemClickListener: ", "Hello: " + postion);
        mlrlPlayAll.setOnClickListener(this);

        return materialDialog;
    }

    @Override
    public void onItemClickListener(ArrayList<Song> songList, final int postion) {
        this.postion = postion;
        this.songList = songList;
        if (postion != -1) {
            Log.e("onItemClickListener: ", "Hello: " + postion);
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getActivity(), "hello: " + getPostion(), Toast.LENGTH_SHORT).show();

    }
}

