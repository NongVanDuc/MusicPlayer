package com.vanduc.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.dataloader.AlbumLoader;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.dataloader.ArtistAlbumLoader;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Album;
import com.vanduc.musicplayer.model.Artist;
import com.vanduc.musicplayer.model.Song;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.AudioHolder>{
    private Context mContext;
    private List<Artist> mArtists;
    private ItemClickListener mItemClickListener;
    public ArtistAdapter(Context context, List<Artist> artists, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.mArtists = artists;
        this.mItemClickListener = itemClickListener;
    }

    public void setmItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_artist,parent,false);

            return new AudioHolder(view);
        }
    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int i) {
        holder.bind(mArtists.get(i), mItemClickListener);
        AlbumSongLoader songLoader = new AlbumSongLoader(mContext);
        Album album = ArtistAlbumLoader.getAlbum(mContext,mArtists.get(i).getId());
        Song songs = songLoader.getSongFromCursor(album.getId());
        Bitmap bitmapAlbum = songs.getCover(mContext);
        if(bitmapAlbum != null){
            holder.imgArtist.setImageBitmap(bitmapAlbum);
        }
        else holder.imgArtist.setImageResource(R.drawable.icon_singer);
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }
    public static class AudioHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgArtist;
        public TextView tvArtistName;
        public TextView tvCountArtis;
        public TextView tvCountSong;
        public ImageView imgMoreOption;

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            imgArtist = (CircleImageView)itemView.findViewById(R.id.img_artist_name);
            tvArtistName = (TextView)itemView.findViewById(R.id.tv_artist_name);
            tvCountArtis = (TextView)itemView.findViewById(R.id.tv_count_artist);
            tvCountSong = (TextView)itemView.findViewById(R.id.tv_count_song);
            imgMoreOption = (ImageView) itemView.findViewById(R.id.img_song_more_option);
        }
        public void bind(final Artist item , final ItemClickListener itemClickListener) {
            tvArtistName.setText(item.getName());
            tvCountSong.setText(String.valueOf(item.getSongCount()));
            tvCountArtis.setText(String.valueOf(item.getAlbumCount()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    // onclick item
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            imgMoreOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onIconClick(view,getAdapterPosition());
                }
            });
        }
    }
}
