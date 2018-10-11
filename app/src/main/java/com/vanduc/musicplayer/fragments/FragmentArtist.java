package com.vanduc.musicplayer.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.ArtistAdapter;
import com.vanduc.musicplayer.common.Common;
import com.vanduc.musicplayer.dataloader.ArtistLoader;
import com.vanduc.musicplayer.dialogs.AlbumOptionDialog;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Artist;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentArtist extends Fragment {
    private List<Artist> mArtistList ;
    private RecyclerView rcvArtist;
    private ArtistAdapter mArtistAdapter;
    public FragmentArtist() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcvArtist = (RecyclerView) view.findViewById(R.id.rcv_artist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rcvArtist.setLayoutManager(layoutManager);
        mArtistList = ArtistLoader.getAllArtists(getActivity());
        mArtistAdapter = new ArtistAdapter(getActivity(), mArtistList, new ItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
//                Intent intent = new Intent(getActivity(),ArtistSongActivity.class);
//                intent.putExtra(ArtistSongActivity.KEY_ID,mArtistList.get(postion).getId());
//                intent.putExtra(ArtistSongActivity.KEY_TITLE,mArtistList.get(postion).getName());
//                getActivity().startActivity(intent);
                long artisId = mArtistList.get(postion).getId();
                String title = mArtistList.get(postion).getName();
                Common.replaceFragment(getActivity().getSupportFragmentManager(), new FragmentArtistAlbum(),artisId,title);
            }

            @Override
            public void onIconClick(View view, int postion) {
                AlbumOptionDialog.newInstance().show(getActivity().getSupportFragmentManager(),"OPTION_MORE");
            }
        });
        rcvArtist.setAdapter(mArtistAdapter);
        mArtistAdapter.notifyDataSetChanged();

    }
}
