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
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dialogs.SongOptionDialog;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.until.StorageUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSong extends Fragment{
    private SongLoader mSongLoader;
    private ArrayList<Song> mSongList;
    private RecyclerView mRecyclerView;
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.vanduc.musicplayer.PlayNewAudio";
    private static final String TAG = "MainActivity";
    private Activity activity ;
    private ItemClickPlaySong mItemClickPlaySong;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        mItemClickPlaySong = (ItemClickPlaySong) activity;
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
        // Inflate the layout for this fragment
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
        AlbumSongLoader albumSongLoader = new AlbumSongLoader(getActivity());
        StorageUtil storage = new StorageUtil(getActivity());
        storage.storeAudio(mSongList);
        //show Recycleview

        SongAdapter songAdapter = new SongAdapter(getActivity(), mSongList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, final int postion) {
                if(mItemClickPlaySong!=null){
                    mItemClickPlaySong.onItemClickListener(mSongList,postion);
                }
            }

            @Override
            public void onIconClick(View view, int postion) {
                SongOptionDialog.newInstance().show(getActivity().getSupportFragmentManager(), "OPTION_MORE");
            }
        });
        Log.e("onCreate: ", mSongList.size() + "");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(songAdapter);
        songAdapter.notifyDataSetChanged();
    }

    private void initView(View view) {
        mRecyclerView =view.findViewById(R.id.rcv_list_music);
    }

}
