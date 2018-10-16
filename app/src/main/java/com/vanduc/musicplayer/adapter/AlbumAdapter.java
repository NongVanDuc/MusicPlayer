package com.vanduc.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.dataloader.AlbumSongLoader;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Album;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ImageUtils;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AudioHolder>{
    private Context mContext;
    private List<Album> mAlbum;
    private ItemClickListener mItemClickListener;

    public AlbumAdapter(Context context, List<Album> albums, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.mAlbum = albums;
        this.mItemClickListener = itemClickListener;
    }

    public void setmItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_album,parent,false);

            return new AudioHolder(view);
        }
    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int i) {
        holder.bind(mAlbum.get(i), mItemClickListener);
        AlbumSongLoader songLoader = new AlbumSongLoader(mContext);
        Song songs = songLoader.getSongFromCursor(mAlbum.get(i).getId());
        Bitmap bitmapAlbum = songs.getCover(mContext);
        if(bitmapAlbum != null){
            holder.imgAlbum.setImageBitmap(bitmapAlbum);
        }
        else holder.imgAlbum.setImageResource(R.drawable.icon_album);



    }

    @Override
    public int getItemCount() {
        return mAlbum.size();
    }
    public class AudioHolder extends RecyclerView.ViewHolder {
        public ImageView imgAlbum;
        public TextView tvAlbumName;
        public TextView tvArtistNameAlbum;
        public TextView tvCountSongAlbum;
        public ImageView imgMoreOption;



        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum = (ImageView) itemView.findViewById(R.id.img_album);
            tvAlbumName = (TextView) itemView.findViewById(R.id.tv_album_name);
            tvArtistNameAlbum = (TextView) itemView.findViewById(R.id.tv_artist_name_album);
            tvCountSongAlbum = itemView.findViewById(R.id.tv_count_song_album);
            imgMoreOption = (ImageView) itemView.findViewById(R.id.img_song_more_option);
        }
        public void bind(final Album item , final ItemClickListener itemClickListener) {
//            tvArtistName.setText(item.getName());
//            tvCountSong.setText(String.valueOf(item.getSongCount()));
//            tvCountArtis.setText(String.valueOf(item.getAlbumCount()));
            tvAlbumName.setText(item.getTitle());
            tvArtistNameAlbum.setText(item.getArtistName());
            tvCountSongAlbum.setText(String.valueOf(item.getSongCount()));
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
