package com.vanduc.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.interFace.IconClickListener;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.AudioHolder> implements Filterable {
    private Context mContext;
    private ArrayList<Song> mListSong;
    private ArrayList<Song> listSong;
    private ItemClickPlaySong mItemClickPlaySong;
    public SongAdapter(Context Context, ArrayList<Song> mListSong, ItemClickPlaySong itemClickPlaySong) {
        this.mContext = Context;
        this.mListSong = mListSong;
        this.listSong = mListSong;
        this.mItemClickPlaySong = itemClickPlaySong;

    }




    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_song,parent,false);

            return new AudioHolder(view);
        }
    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int i) {
        holder.bind(mListSong.get(i), mItemClickPlaySong);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.icon_music)
                .error(R.drawable.icon_music);
        Glide.with(mContext)
                .applyDefaultRequestOptions(options)
                .load(mListSong.get(i).getUriImage())

                .into(holder.imgSinger);
    }

    @Override
    public int getItemCount() {
        return mListSong.size();
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mListSong = listSong;
                } else {
                    ArrayList<Song> filteredList = new ArrayList<>();
                    for (Song row : listSong) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) || row.getArtist().toLowerCase().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mListSong = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mListSong;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mListSong = (ArrayList<Song>) filterResults.values;
                Log.e( "publishResults: ", mListSong.size()+"");
                notifyDataSetChanged();
            }
        };
    }
    public  class AudioHolder extends RecyclerView.ViewHolder {
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
        public void bind(final Song item , final ItemClickPlaySong itemClickListener) {
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
                    itemClickListener.onItemClickListener(mListSong, getAdapterPosition());
                }
            });
            imgMoreOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   itemClickListener.onIconClickListener(mListSong,getAdapterPosition());
                }
            });
        }
    }
}
