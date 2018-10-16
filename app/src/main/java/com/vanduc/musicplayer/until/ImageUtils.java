package com.vanduc.musicplayer.until;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageUtils {
    public static Bitmap getCover(Context context, long mId) {

        // ImageLoader.getInstance().getDiskCache().g
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap curThumb = null;
        try {
            Uri uri = Uri.parse("content://media/external/audio/media/" + mId);
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                curThumb = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return curThumb;
    }
}
