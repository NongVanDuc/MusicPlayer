package com.vanduc.musicplayer.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.AlbumAdapter;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dataloader.ArtistAlbumLoader;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Album;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.util.ResUtil;
import com.vanduc.musicplayer.util.StorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentArtistAlbum extends Fragment {
    private RecyclerView rcvAlbum;
    private AlbumAdapter mAlbumAdapter;
    private List<Album> mAlbumList;
    public static final String KEY_TITLE = "title";
    public static final String KEY_ID = "artistId";
    private Toolbar toolBar;


    public FragmentArtistAlbum() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rcvAlbum = (RecyclerView) view.findViewById(R.id.rcv_album);
        toolBar = (Toolbar) view.findViewById(R.id.toolBar);
        Bundle bundle = this.getArguments();
        long artistId = bundle.getLong(KEY_ID, -1);
        String title = bundle.getString(KEY_TITLE, "Music");
        if(title.equals("<unknown>")){
            toolBar.setTitle(ResUtil.getInstance().getString(R.string.unknown_artist));
        }
        else {
            toolBar.setTitle(title);
        }
        setDataListViewAlbum(artistId);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolBar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }
    private void setDataListViewAlbum(long artistId) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rcvAlbum.setLayoutManager(layoutManager);
        mAlbumList = ArtistAlbumLoader.getAlbumsForArtist(getActivity(),artistId);
        mAlbumAdapter = new AlbumAdapter(getActivity(), mAlbumList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                long albumId = mAlbumList.get(postion).getId();
                String title = mAlbumList.get(postion).getTitle();
                replaceFragment(new FragmentAlbumSong(),albumId,title);
//                ArrayList<Song> mSongList;
//                AlbumSongLoader albumSongLoader = new AlbumSongLoader(getActivity());
//                mSongList = albumSongLoader.getSongsFromCursor(albumId);
//                StorageUtil storage = new StorageUtil(getActivity());
//                if(mSongList.size() <=0){
//                    SongLoader songLoader = new SongLoader(getContext());
//                    mSongList = songLoader.getSongsFromCursor();
//                    if(mSongList.size()>0){
//                        storage.storeAudio(mSongList);
//                    }
//                    else Toast.makeText(getActivity(), ResUtil.getInstance().getString(R.string.no_song), Toast.LENGTH_SHORT).show();
//                }
//                else storage.storeAudio(mSongList);
            }
            @Override
            public void onIconClick(View view, int postion) {

            }
        });
        rcvAlbum.setAdapter(mAlbumAdapter);
        mAlbumAdapter.notifyDataSetChanged();
    }

    private void replaceFragment(Fragment fragment, long id, String title) {
        Bundle bundle = new Bundle();
        bundle.putLong(FragmentAlbumSong.KEY_ALBUM_ID, id );
        bundle.putString(FragmentAlbumSong.KEY_TITLE,title );
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fl_main, fragment);
        transaction.commit();
        RelativeLayout relativeLayout= getActivity().findViewById(R.id.rll_root_view);
        relativeLayout.setVisibility(View.GONE);
    }
}
