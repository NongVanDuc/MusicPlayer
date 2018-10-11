package com.vanduc.musicplayer.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.fragments.FragmentAlbum;
import com.vanduc.musicplayer.fragments.FragmentArtist;
import com.vanduc.musicplayer.fragments.FragmentListPlay;
import com.vanduc.musicplayer.fragments.FragmentSong;
import com.vanduc.musicplayer.until.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragmentList = new ArrayList<>();
    List<String> titleList = new ArrayList<>();
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList.add(new FragmentSong());
        fragmentList.add(new FragmentArtist());
        fragmentList.add(new FragmentAlbum());
        fragmentList.add(new FragmentListPlay());
        titleList.add(ResUtil.getInstance().getString(R.string.song));
        titleList.add(ResUtil.getInstance().getString(R.string.artist));
        titleList.add(ResUtil.getInstance().getString(R.string.album));
        titleList.add(ResUtil.getInstance().getString(R.string.list_play));

    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
