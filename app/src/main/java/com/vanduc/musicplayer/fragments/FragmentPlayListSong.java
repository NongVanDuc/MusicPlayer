package com.vanduc.musicplayer.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.SongAdapter;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dataloader.PlaylistSongLoader;
import com.vanduc.musicplayer.dialogs.SongOptionDialog;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ResUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentPlayListSong extends Fragment {
    private ArrayList<Song> mSongList;
    private SongAdapter mSongAdapter;
    private RecyclerView mRcvListAlbumSong;
    public static final String KEY_PLAY_LIST_ID = "playListId";
    public static final String KEY_TITLE = "title";
    private Toolbar toolBar;
    private Activity activity ;
    private ItemClickPlaySong mItemClickPlaySong;

    public FragmentPlayListSong() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        mItemClickPlaySong = (ItemClickPlaySong) activity;
    }

    public void setmItemClickPlaySong(ItemClickPlaySong mItemClickPlaySong) {
        this.mItemClickPlaySong = mItemClickPlaySong;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_list_song, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRcvListAlbumSong = view.findViewById(R.id.rcv_album_song);
        toolBar = (Toolbar) view.findViewById(R.id.toolBar);
        Bundle bundle = this.getArguments();
        long playListId = bundle.getLong(KEY_PLAY_LIST_ID, -1);
        String title = bundle.getString(KEY_TITLE, "Music");
        if(title.equals("<unknown>")){
            toolBar.setTitle(ResUtil.getInstance().getString(R.string.unknown_album));
        }
        else {
            toolBar.setTitle(title);
        }
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolBar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRcvListAlbumSong.setLayoutManager(linearLayoutManager);
        mSongList = PlaylistSongLoader.getSongsInPlaylist(getActivity(),playListId);
        mSongAdapter = new SongAdapter(getActivity(), mSongList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if(mItemClickPlaySong!=null){
                    mItemClickPlaySong.onItemClickListener(mSongList,postion);
                }
            }

            @Override
            public void onIconClick(View view, int postion) {
                SongOptionDialog.newInstance().show(getActivity().getSupportFragmentManager(), "OPTION_MORE");
            }
        });
        mRcvListAlbumSong.setAdapter(mSongAdapter);
        mSongAdapter.notifyDataSetChanged();

    }
}