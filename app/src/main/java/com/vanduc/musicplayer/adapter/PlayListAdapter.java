package com.vanduc.musicplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.model.Playlist;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.AudioHolder>{
    private Context mContext;
    private List<Playlist> mPlaylists;
    private ItemClickListener mItemClickListener;

    public PlayListAdapter(Context context, List<Playlist> playlists, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.mPlaylists = playlists;
        this.mItemClickListener = itemClickListener;
    }

    public void setmItemClickListener(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_play_list,parent,false);

            return new AudioHolder(view);
        }
    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int i) {
        holder.bind(mPlaylists.get(i), mItemClickListener);

    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }
    public class AudioHolder extends RecyclerView.ViewHolder {
        public ImageView imgAlbumName;
        public TextView tvAlbumName;
        public LinearLayout lrlAlbumDetail;
        public TextView tvCountSong;
        public ImageView imgMoreOption;

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbumName = (ImageView) itemView.findViewById(R.id.img_album_name);
            tvAlbumName = (TextView) itemView.findViewById(R.id.tv_album_name);
            lrlAlbumDetail = (LinearLayout) itemView.findViewById(R.id.lrl_album_detail);
            tvCountSong = (TextView) itemView.findViewById(R.id.tv_count_song);
            imgMoreOption = (ImageView) itemView.findViewById(R.id.img_song_more_option);
        }
        public void bind(final Playlist item , final ItemClickListener itemClickListener) {
            tvAlbumName.setText(item.getName());
            tvCountSong.setText(String.valueOf(item.getSongCount()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    // onclick item
                    itemClickListener.onItemClick(v, getAdapterPosition());
//                    Intent intent = new Intent(mContext,AlbumSongActivity.class);
//                    intent.putExtra(AlbumSongActivity.KEY_ALBUM_ID,mPlaylists.get(getAdapterPosition()).getId());
//                    intent.putExtra(AlbumSongActivity.KEY_TITLE,mPlaylists.get(getAdapterPosition()).getName());
//                    mContext.startActivity(intent);
                }
            });
            imgMoreOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.onIconClick(view,getAdapterPosition());
                    Log.e( "onClick: ","Heloo2" );
                }
            });
        }
    }
}
