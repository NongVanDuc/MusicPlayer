package com.vanduc.musicplayer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.AlbumAdapter;
import com.vanduc.musicplayer.common.Common;
import com.vanduc.musicplayer.dataloader.AlbumLoader;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.dialogs.AlbumOptionDialog;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.UpdateFragment;
import com.vanduc.musicplayer.model.Album;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ResUtil;
import com.vanduc.musicplayer.until.StorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAlbum extends Fragment {
    private RecyclerView mRcv;
    private List<Album> mAlbumList;
    private AlbumAdapter mAlbumAdapter;
    private UpdateFragment updateFragment;
    public FragmentAlbum() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.updateFragment = (UpdateFragment) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (updateFragment != null){
            updateFragment.getFragmentAlbum(this);
        }
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRcv = (RecyclerView) view.findViewById(R.id.rcv_album_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRcv.setLayoutManager(linearLayoutManager);
        setDataList();

    }
    private void setDataList(){
        mAlbumList = AlbumLoader.getAllAlbums(getActivity());

        mAlbumAdapter = new AlbumAdapter(getActivity(), mAlbumList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                long albumId = mAlbumList.get(postion).getId();
                String title = mAlbumList.get(postion).getArtistName();
                replaceFragment(new FragmentAlbumSong(),albumId,title);
                ArrayList<Song> mSongList;
                AlbumSongLoader albumSongLoader = new AlbumSongLoader(getActivity());
                mSongList = albumSongLoader.getSongsFromCursor(albumId);
                StorageUtil storage = new StorageUtil(getActivity());
                if(mSongList.size() <=0){
                    SongLoader songLoader = new SongLoader(getContext());
                    mSongList = songLoader.getSongsFromCursor();
                    if(mSongList.size()>0){
                        storage.storeAudio(mSongList);
                    }
                    else Toast.makeText(getActivity(), ResUtil.getInstance().getString(R.string.no_song), Toast.LENGTH_SHORT).show();
                }
                else storage.storeAudio(mSongList);

            }

            @Override
            public void onIconClick(View view, int postion) {
                AlbumOptionDialog.newInstance().show(getActivity().getSupportFragmentManager(),"OPTION_MORE");
            }
        });
        mRcv.setAdapter(mAlbumAdapter);
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
    }
    public void updateListAlbum(){
        if(mAlbumList != null && mAlbumList.size()>0){
            mAlbumList.clear();
            setDataList();
        }

    }
}
