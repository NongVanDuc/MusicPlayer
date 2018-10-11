package com.vanduc.musicplayer.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.fragments.FragmentArtistAlbum;

public class Common {
    public static void replaceFragment(FragmentManager fragmentManager, Fragment fragment, long id,String title) {
        Bundle bundle = new Bundle();
        bundle.putLong(FragmentArtistAlbum.KEY_ID, id );
        bundle.putString(FragmentArtistAlbum.KEY_TITLE,title );
        fragment.setArguments(bundle);
        FragmentTransaction transaction =fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fl_main, fragment);
        transaction.commit();
    }
}
