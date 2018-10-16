package com.vanduc.musicplayer.until;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.SongAdapter;
import com.vanduc.musicplayer.interFace.UpdateFragment;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.screens.HomeActivity;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControlUtils {
    private static HomeActivity homeActivity;
    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    public static String getAlbumArtForFile(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static int getActionBarHeight(Context context) {
        int mActionBarHeight;
        TypedValue mTypedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, context.getResources().getDisplayMetrics());

        return mActionBarHeight;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }

    public static void removeFromPlaylist(final Context context, final long id,
                                          final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ? ", new String[]{
                Long.toString(id)
        });
    }

    public static Uri getSongUri(Context context, long id) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        selection.append(id);
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);

        if (c == null) {
            return null;
        }
        c.moveToFirst();


        try {

            Uri uri = Uri.parse(c.getString(1));
            c.close();

            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void shareTrack(final Context context, long id) {

        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, getSongUri(context, id));
            context.startActivity(Intent.createChooser(share, "Share"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDeleteDialog(final Context context, final Song song, final SongAdapter adapter, final int postion, final ArrayList<Song> songList) {
        new MaterialDialog.Builder(context)
                .title(ResUtil.getInstance().getString(R.string.delete))
                .content(ResUtil.getInstance().getString(R.string.confirm_delete) + " '" + song.getTitle() + "' ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeTracks(context, song);
                        songList.remove(postion);
                        adapter.notifyDataSetChanged();
                        StorageUtil storage = new StorageUtil(context);
                        storage.storeAudio(songList);
                        if(postion >=1){
                            storage.storeAudioIndex(postion-1);
                        }


                        Log.e("onClickDelete: ", songList.size() + "");
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void removeTracks(final Context context, final Song song) {
        //remove item from database, recyclerview and storage
        //delete file from storage
        homeActivity = (HomeActivity) context;
        File file = new File(song.getPath());
        if (file.exists()) {
            file.delete();
            StorageUtil storageUtil = new StorageUtil(context);
            storageUtil.clearCachedAudioPlaylist();
            homeActivity.updateFragmentListPlay();
            homeActivity.updateFragmentArtist();
            homeActivity.updateFragmentAlbum();
            Toast.makeText(context, ResUtil.getInstance().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
        }
        notifyMediaScannerService(context, file.getAbsolutePath());


    }

    static public void notifyMediaScannerService(Context context, String path) {
        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }
    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }
    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }
    public static void AddSongToPlaylist(long songID, long pID, Context context ){
        homeActivity = (HomeActivity) context;
        Uri pUri = MediaStore.Audio.Playlists.Members.getContentUri("external", pID);

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        String[] cols = new String[] {
                "count(*)"
        };
        Cursor cur = resolver.query(pUri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0)+1;
        cur.close();

        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,base);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songID);
        resolver.insert(pUri,values);
        resolver.notifyChange(Uri.parse("content://media"), null);
        Toast.makeText(context, ResUtil.getInstance().getString(R.string.songAdded), Toast.LENGTH_SHORT).show();
        homeActivity.updateFragmentListPlay();
        Log.i("Song ID:", String.valueOf(songID));

    }

}
