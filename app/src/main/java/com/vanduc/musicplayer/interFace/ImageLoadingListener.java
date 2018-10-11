package com.vanduc.musicplayer.interFace;

import android.graphics.Bitmap;
import android.view.View;

public interface ImageLoadingListener {
    void onLoadingStarted(String imageUri, View view);

    void onLoadingFailed(String imageUri, View view);

    void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

    void onLoadingCancelled(String imageUri, View view);
}

