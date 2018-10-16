package com.vanduc.musicplayer.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.SongAdapter;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dialogs.SongOptionDialog;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ResUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentArtistSong extends Fragment {
    private ArrayList<Song> mSongList;
    private SongAdapter mSongAdapter;
    private RecyclerView mRcvListAlbumSong;
    public static final String KEY_ALBUM_ID = "albumId";
    public static final String KEY_TITLE = "title";
    private Toolbar toolBar;
    private Activity activity ;
    private ItemClickPlaySong mItemClickPlaySong;
    public FragmentArtistSong() {
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
        return inflater.inflate(R.layout.fragment_artist_song, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRcvListAlbumSong = view.findViewById(R.id.rcv_album_song);
        toolBar = (Toolbar) view.findViewById(R.id.toolBar);
        Bundle bundle = this.getArguments();
        long albumId = bundle.getLong(KEY_ALBUM_ID, -1);
        String title = bundle.getString(KEY_TITLE, "Music");
        if(title.equals("<unknown>")){
            toolBar.setTitle(ResUtil.getInstance().getString(R.string.unknown_album));
        }
        else {
            toolBar.setTitle(title);
        }
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolBar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRcvListAlbumSong.setLayoutManager(linearLayoutManager);
        AlbumSongLoader albumSongLoader = new AlbumSongLoader(getActivity());
        mSongList = albumSongLoader.getSongsFromCursor(albumId);
        Log.e("onCreate: ", mSongList.size() + "/" + albumId);
        mSongAdapter = new SongAdapter(getActivity(), mSongList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                if(mItemClickPlaySong!=null){
                    mItemClickPlaySong.onItemClickListener(mSongList,postion);
                }
            }

            @Override
            public void onIconClick(View view, int postion) {
                //SongOptionDialog.newInstance().show(getActivity().getSupportFragmentManager(), "OPTION_MORE");
            }
        });
        mRcvListAlbumSong.setAdapter(mSongAdapter);
        mSongAdapter.notifyDataSetChanged();

    }
}
