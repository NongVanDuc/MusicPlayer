package com.vanduc.musicplayer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.PlayListAdapter;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dataloader.PlaylistLoader;
import com.vanduc.musicplayer.dataloader.PlaylistSongLoader;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.dialogs.CreatePlaylistDialog;
import com.vanduc.musicplayer.dialogs.DiaLogPlayListOption;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.UpdateFragment;
import com.vanduc.musicplayer.model.Playlist;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ControlUtils;
import com.vanduc.musicplayer.until.ResUtil;
import com.vanduc.musicplayer.until.StorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentListPlay extends Fragment {
    private RecyclerView mRcvListPlay;
    private PlayListAdapter mPlayListAdapter;
    private ArrayList<Playlist> mPlaylists;
    private LinearLayout mLinearLayout;
    private UpdateFragment updateFragment;
    public FragmentListPlay() {
        // Required empty public constructor
    }

    public void setUpdateFragment(UpdateFragment updateFragment) {
        this.updateFragment = updateFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        updateFragment = (UpdateFragment) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(updateFragment != null){
            updateFragment.getFragmentListPlay(this);
        }

        return inflater.inflate(R.layout.fragment_list_play, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLinearLayout = view.findViewById(R.id.lrl_add_list_play);
        mRcvListPlay = view.findViewById(R.id.rcv_play_list);
        setDataList();
    }
    private void setDataList(){
        mPlaylists = PlaylistLoader.getPlaylists(getActivity());
        mPlayListAdapter = new PlayListAdapter(getActivity(), mPlaylists, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                long playListId = mPlaylists.get(postion).getId();
                String title = mPlaylists.get(postion).getName();
                StorageUtil storage = new StorageUtil(getActivity());
                replaceFragment(new FragmentPlayListSong(),playListId,title);
                ArrayList<Song> mSongList;
                mSongList = PlaylistSongLoader.getSongsInPlaylist(getActivity(),playListId);
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
                DiaLogPlayListOption diaLogPlayListOption = new DiaLogPlayListOption(getActivity(),postion,mPlaylists,mPlayListAdapter);
                diaLogPlayListOption.show();
            }
        });
        mRcvListPlay.setAdapter(mPlayListAdapter);
        mPlayListAdapter.notifyDataSetChanged();
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePlaylistDialog.newInstance().show(getChildFragmentManager(), "CREATE_PLAYLIST");
            }
        });
    }

    public void updatePlaylists() {
            mPlaylists.clear();
            setDataList();
    }
    private void replaceFragment(Fragment fragment, long id, String title) {
        Bundle bundle = new Bundle();
        bundle.putLong(FragmentPlayListSong.KEY_PLAY_LIST_ID, id );
        bundle.putString(FragmentPlayListSong.KEY_TITLE,title );
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fl_main, fragment);
        transaction.commit();
    }
}
