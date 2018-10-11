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
import com.vanduc.musicplayer.function.MusicPlayer;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Song;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.AudioHolder>{
    private Context mContext;
    private List<Song> mListSong;
    private ItemClickListener mItemClickListener;
    public SongAdapter(Context Context, List<Song> mListSong, ItemClickListener mItemClickListener) {
        this.mContext = Context;
        this.mListSong = mListSong;
        this.mItemClickListener = mItemClickListener;

    }




    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_song,parent,false);

            return new AudioHolder(view);
        }
    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int i) {
        holder.bind(mListSong.get(i), mItemClickListener);
        Bitmap bitmap = mListSong.get(i).getSmallCover(mContext);
        if(bitmap != null){
            holder.imgSinger.setImageBitmap(bitmap);
        }
        else holder.imgSinger.setImageResource(R.drawable.icon_music);
    }

    @Override
    public int getItemCount() {
        return mListSong.size();
    }
    public static class AudioHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgSinger;
        public TextView tvSongName;
        public TextView tvSinger;
        public ImageView imgMoreOption;

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            imgSinger = (CircleImageView)itemView.findViewById(R.id.img_singer);
            tvSongName = (TextView)itemView.findViewById(R.id.tv_song_name);
            tvSinger = (TextView)itemView.findViewById(R.id.tv_singer);
            imgMoreOption = (ImageView) itemView.findViewById(R.id.img_song_more_option);

        }

        public void bind(final Song item , final ItemClickListener itemClickListener) {
            String songNameDefault =item.getTitle();
            String songName = "";
            if(songNameDefault.length()>40){
                songName = songNameDefault.substring(0,40) +"...";
            }
            else {
                songName = songNameDefault;
            }
            tvSongName.setText(songName);
            tvSinger.setText(item.getArtist());
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
