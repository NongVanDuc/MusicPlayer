package com.vanduc.musicplayer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.model.Song;

public class AlbumOptionDialog extends DialogFragment {
    public static AlbumOptionDialog newInstance() {
        return newInstance((Song) null);
    }

    public static AlbumOptionDialog newInstance(Song song) {
        long[] songs;
        if (song == null) {
            songs = new long[0];
        } else {
            songs = new long[1];
            songs[0] = song.id;
        }
        return newInstance(songs);
    }

    public static AlbumOptionDialog newInstance(long[] songList) {
        AlbumOptionDialog dialog = new AlbumOptionDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity()).customView(R.layout.item_dialog_album_option,true).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(final MaterialDialog dialog, final View itemView, int which, CharSequence text) {

            }
        }).build();
        View view =materialDialog.getCustomView();
        LinearLayout mlrlPlayAll = view.findViewById(R.id.lrl_play_all);
        mlrlPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "hello", Toast.LENGTH_SHORT).show();
            }
        });

        return materialDialog;
    }
}

