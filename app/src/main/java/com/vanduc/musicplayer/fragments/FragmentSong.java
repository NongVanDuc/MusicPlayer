package com.vanduc.musicplayer.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.SongAdapter;
import com.vanduc.musicplayer.dialogs.DiaLogSongOption;
import com.vanduc.musicplayer.interFace.IconClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.screens.HomeActivity;
import com.vanduc.musicplayer.util.StorageUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSong extends Fragment {
    private SongLoader mSongLoader;
    private ArrayList<Song> mSongList;
    private RecyclerView mRecyclerView;
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.vanduc.musicplayer.PlayNewAudio";
    private static final String TAG = "MainActivity";
    private Activity activity;
    private ItemClickPlaySong mItemClickPlaySong;
    private IconClickListener mIconClickListener;
    private SongAdapter songAdapter;
    private DiaLogSongOption diaLogSongOption;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        //Dialog dialogFragment = new DiaLogSongOption(activity, this,  postion);
        mItemClickPlaySong = (ItemClickPlaySong) activity;
        //mIconClickListener = (IconClickListener) dialogFragment;
    }

    public void setmIconClickListener(IconClickListener mIconClickListener) {
        this.mIconClickListener = mIconClickListener;
    }

    public void setmItemClickPlaySong(ItemClickPlaySong mItemClickPlaySong) {
        this.mItemClickPlaySong = mItemClickPlaySong;
    }

    public FragmentSong() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment\
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        mSongLoader = new SongLoader(getContext());

        mSongList = mSongLoader.getSongsFromCursor();
        Log.e(TAG, mSongList.size() + "");

        // get and play firt song of list
        mSongLoader = new SongLoader(getActivity());
        mSongList = mSongLoader.getSongsFromCursor();
        StorageUtil storage = new StorageUtil(getActivity());
        //storage.storeAudio(mSongList);
        //show Recycleview

        songAdapter = new SongAdapter(getActivity(), mSongList, new ItemClickPlaySong() {
            @Override
            public void onItemClickListener(ArrayList<Song> songList, int postion) {
                if (mItemClickPlaySong != null) {
                    mItemClickPlaySong.onItemClickListener(songList, postion);
                }
            }

            @Override
            public void onIconClickListener(ArrayList<Song> songList, int postion) {
                diaLogSongOption = new DiaLogSongOption((HomeActivity) getActivity(), postion, mSongList, songAdapter);
                diaLogSongOption.show();
                if (mIconClickListener != null) {
                    mIconClickListener.onItemClickListener(songList, postion);
                }
            }
        });
        Log.e("onCreate: ", mSongList.size() + "");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(songAdapter);
        songAdapter.notifyDataSetChanged();
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.rcv_list_music);
    }

}
