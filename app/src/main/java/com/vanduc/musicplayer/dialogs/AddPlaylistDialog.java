package com.vanduc.musicplayer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.dataloader.PlaylistLoader;
import com.vanduc.musicplayer.model.Playlist;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.util.ControlUtils;
import com.vanduc.musicplayer.util.ResUtil;

import java.util.List;

public class AddPlaylistDialog extends DialogFragment {

    public static AddPlaylistDialog newInstance(Song song) {
        long[] songs = new long[1];
        songs[0] = song.id;
        return newInstance(songs);
    }

    public static AddPlaylistDialog newInstance(long[] songList) {
        AddPlaylistDialog dialog = new AddPlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final List<Playlist> playlists = PlaylistLoader.getPlaylists(getActivity());
        CharSequence[] chars = new CharSequence[playlists.size() + 1];
        chars[0] = "Create new playlist";

        for (int i = 0; i < playlists.size(); i++) {
            chars[i + 1] = playlists.get(i).getName();
        }
        return new MaterialDialog.Builder(getActivity()).title(ResUtil.getInstance().getString(R.string.addToList)).items(chars).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                long[] songs = getArguments().getLongArray("songs");
                if (which == 0) {
                    CreatePlaylistDialog.newInstance(songs).show(getActivity().getSupportFragmentManager(), "CREATE_PLAYLIST");
                    return;
                }

                ControlUtils.addToPlaylist(getActivity(), songs, playlists.get(which - 1).getId());
                dialog.dismiss();

            }
        }).build();
    }
}

