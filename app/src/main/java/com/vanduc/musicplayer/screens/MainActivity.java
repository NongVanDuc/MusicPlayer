package com.vanduc.musicplayer.screens;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.adapter.ViewPagerAdapter;
import com.vanduc.musicplayer.dataloader.SongLoader;
import com.vanduc.musicplayer.fragments.FragmentSong;
import com.vanduc.musicplayer.interFace.ItemClickListener;
import com.vanduc.musicplayer.interFace.ItemClickPlaySong;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.sevices.MediaPlayerService;
import com.vanduc.musicplayer.until.ResUtil;
import com.vanduc.musicplayer.until.StorageUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemClickPlaySong, View.OnClickListener {


    private Toolbar mToolBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ImageView mImgSearch;
    private AppBarLayout appBarLayout;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private LinearLayout mLinearLayoutControlBottom;
    private Toolbar mToolbarPlaying;
    private FragmentSong fragmentSong;
    private Handler  mHandler;
    private Runnable mRunnable;
    private boolean doubleBackToExitPressedOnce = false;


    private SlidingUpPanelLayout slidingLayout;
    public static final String KEY_MEDIA = "media";
    private MediaPlayerService player;
    private MediaPlayerService mMediaPlayerService;
    private ServiceConnection serviceConnection;
    private SongLoader mSongLoader;
    private ArrayList<Song> mSongList;
    private RecyclerView mRecyclerView;
    public static final String BROADCAST_PLAY_NEW_AUDIO = "com.vanduc.musicplayer.PlayNewAudio";
    private RelativeLayout mRelativeLayout;
    private RelativeLayout mRelativeLayoutControl;
    private int mPostion;
    private static final String TAG = "MainActivity";
    private RelativeLayout rlControl;
    private LinearLayout lrlControl;
    private ImageButton imbLoopSong;
    private ImageButton imbPrevSong;
    private ImageButton imbPlaySong;
    private ImageButton imbNextSong;
    private ImageButton imbFavorite;
    private RelativeLayout rlInfor;
    private ImageView imgPlayingMusic;
    private TextView tvSongName;
    private TextView tvSingerName;
    private SeekBar sbPlay;
    private TextView tvCountSongTime;
    private TextView tvSongTime;
    private LinearLayout lrlControlBottom;
    private TextView tvSongNameBottom;
    private TextView tvSingerNameBottom;
    private ImageButton imgPrevSongBottom;
    private ImageButton imgPlaySongBottom;
    private ImageButton imgNextSongBottom;
    private ItemClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        ResUtil.init(MainActivity.this);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mToolBar = findViewById(R.id.toolBar);
        mToolBar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(mToolBar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mLinearLayoutControlBottom = findViewById(R.id.lrl_control_bottom);
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
        mImgSearch = findViewById(R.id.icon_search);
        appBarLayout = findViewById(R.id.appBarLayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        fragmentSong = new FragmentSong();
        fragmentSong.setmItemClickPlaySong(this);
        mToolbarPlaying = findViewById(R.id.toolbarNowPlaying);
        setSupportActionBar(mToolbarPlaying);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(ResUtil.getInstance().getString(R.string.now_play_ing));


        mSongLoader = new SongLoader(this);
        mSongList = mSongLoader.getSongsFromCursor();
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                appBarLayout.setAlpha(1 - slideOffset);
                mLinearLayoutControlBottom.setAlpha(1 - slideOffset);
                mToolbarPlaying.setVisibility(View.VISIBLE);
                mToolbarPlaying.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.e(TAG, previousState + "/" + newState);
                if (MediaPlayerService.getMediaPlayerService() == null) {
                    lrlControlBottom.setVisibility(View.GONE);
                    //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                    //((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                } else if (newState.toString().equals("EXPANDED") && mSongList.size() > 0 && mSongLoader != null) {
                    // btn2.setVisibility(View.VISIBLE);
                    // tv2.setVisibility(View.VISIBLE);
                    lrlControlBottom.setVisibility(View.GONE);
                    //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                    //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mSongList.get(MediaPlayerService.getMediaPlayerService().getIndex()).getTitle());
                    tvSongName.setText(forMatSongName(mSongList.get(MediaPlayerService.getMediaPlayerService().getIndex()).getTitle()));
                    tvSingerName.setText(mSongList.get(MediaPlayerService.getMediaPlayerService().getIndex()).getArtist());
                    tvSongTime.setText(MediaPlayerService.getMediaPlayerService().getTimePlay());

                    //((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    // mRelativeLayoutControl.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                }
                if (newState.toString().equals("COLLAPSED") && mSongList.size() > 0 && mSongLoader != null) {
                    //btn2.setVisibility(View.VISIBLE);
                    //tv2.setVisibility(View.GONE);
                    lrlControlBottom.setVisibility(View.VISIBLE);
                    // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                    //((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    // mRelativeLayoutControl.setBackgroundColor(getResources().getColor(R.color.bgcontrol));
                }
            }
        });

        mImgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    if(MediaPlayerService.getMediaPlayerService() != null){
                        MediaPlayerService.getMediaPlayerService().seekToPos(i);
                    }


                }
                tvCountSongTime.setText((forMartime(i)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initView() {
        mHandler = new Handler();
        tvSongNameBottom = (TextView) findViewById(R.id.tv_song_name_bottom);
        tvSingerNameBottom = (TextView) findViewById(R.id.tv_singer_name_bottom);
        imgPrevSongBottom = (ImageButton) findViewById(R.id.imb_prev_song_bottom);
        imgPlaySongBottom = (ImageButton) findViewById(R.id.imb_play_song_bottom);
        imgNextSongBottom = (ImageButton) findViewById(R.id.imb_next_song_bottom);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mRelativeLayout = findViewById(R.id.rl_infor);
        mRelativeLayoutControl = findViewById(R.id.rl_control);
        rlControl = (RelativeLayout) findViewById(R.id.rl_control);
        lrlControl = (LinearLayout) findViewById(R.id.lrl_control);
        imbLoopSong = (ImageButton) findViewById(R.id.imb_loop_song);
        imbPrevSong = (ImageButton) findViewById(R.id.imb_prev_song);
        imbPlaySong = (ImageButton) findViewById(R.id.imb_play_song);
        imbNextSong = (ImageButton) findViewById(R.id.imb_next_song);
        imbFavorite = (ImageButton) findViewById(R.id.imb_favorite);
        rlInfor = (RelativeLayout) findViewById(R.id.rl_infor);
        imgPlayingMusic = (ImageView) findViewById(R.id.img_playing_music);
        tvSongName = (TextView) findViewById(R.id.tv_song_name);
        tvSingerName = (TextView) findViewById(R.id.tv_singer_name);
        sbPlay = (SeekBar) findViewById(R.id.sb_play);
        tvCountSongTime = (TextView) findViewById(R.id.tv_count_song_time);
        tvSongTime = (TextView) findViewById(R.id.tv_song_time);
        mRecyclerView = findViewById(R.id.rcv_list_music);
        lrlControlBottom = findViewById(R.id.lrl_control_bottom);
        imbNextSong.setOnClickListener(this);
        imbPlaySong.setOnClickListener(this);
        imbFavorite.setOnClickListener(this);
        imbPrevSong.setOnClickListener(this);
        imbLoopSong.setOnClickListener(this);
        imgNextSongBottom.setOnClickListener(this);
        imgPrevSongBottom.setOnClickListener(this);
        imgPlaySongBottom.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            this.doubleBackToExitPressedOnce = false;
            return;
        }
         else if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fragmentManager.popBackStack();
            this.doubleBackToExitPressedOnce = false;
            return;
        }
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            this.doubleBackToExitPressedOnce = false;
            return;
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
    public void playSycle(){
        if(MediaPlayerService.getMediaPlayerService() != null) {
            try {
                sbPlay.setProgress(MediaPlayerService.getMediaPlayerService().getCurrentPostion());
                if (MediaPlayerService.getMediaPlayerService().getStateMedia()) {
                    imbPlaySong.setImageResource(R.drawable.ic_pause_circle_outline_white);
                    mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            playSycle();
                        }
                    };
                    mHandler.postDelayed(mRunnable, 1000);
                }
            }catch (Exception e){

            }
        }

    }
    public String forMartime(int m) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(m);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    // functioncontrol play media
    private void playAudio(final int audioIndex, final ArrayList<Song> songList) {
        //Check is service is active
        if (MediaPlayerService.getMediaPlayerService() != null) {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(MainActivity.this);
            storage.storeAudioIndex(audioIndex);
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);

        } else {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(MainActivity.this);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(playerIntent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playAudio(audioIndex,songList);

                }
            },200);

        }
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        imbPlaySong.setImageResource(R.drawable.ic_pause_circle_outline_white);
        imgPlaySongBottom.setImageResource(R.drawable.ic_pause_circle_outline_yellow);
    }
    private void playAudio() {
        if (MediaPlayerService.getMediaPlayerService() != null) {

            if (MediaPlayerService.getMediaPlayerService().getStateMedia()) {
                MediaPlayerService.getMediaPlayerService().pauseMedia();
                tvSongName.setText(forMatSongName(mSongList.get(mPostion).getTitle()));
                tvSongNameBottom.setText(forMatSongName(mSongList.get(mPostion).getTitle()));
                tvSingerNameBottom.setText(forMatSongName(mSongList.get(mPostion).getArtist()));
                tvSongTime.setText(forMartime(MediaPlayerService.getMediaPlayerService().getDuaration()));
                sbPlay.setMax(MediaPlayerService.getMediaPlayerService().getDuaration());
                imbPlaySong.setImageResource(R.drawable.ic_play_circle_outline_white);
                imgPlaySongBottom.setImageResource(R.drawable.ic_play_circle_yellow);
                playSycle();
            } else {
                MediaPlayerService.getMediaPlayerService().resumeMedia();
                setTextSong();
                playSycle();

            }
            //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mSongList.get(MediaPlayerService.getMediaPlayerService().getIndex()).getTitle());
        } else {
            Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playAudio(0,mSongList);
                    setTextSong();
                    playSycle();
                }
            }, 200);
            playAudio(0,mSongList);
            playSycle();
            // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mSongList.get(0).getTitle());
        }


    }
    private void setTextSong(){
        playSycle();
        imbPlaySong.setImageResource(R.drawable.ic_pause_circle_outline_white);
        imgPlaySongBottom.setImageResource(R.drawable.ic_pause_circle_outline_yellow);
        tvSongName.setText(forMatSongName(mSongList.get(mPostion).getTitle()));
        tvSongNameBottom.setText(forMatSongName(mSongList.get(mPostion).getTitle()));
        tvSingerNameBottom.setText(forMatSongName(mSongList.get(mPostion).getArtist()));
        tvSingerName.setText(forMatSongName(mSongList.get(mPostion).getArtist()));
        tvSongTime.setText(forMartime(MediaPlayerService.getMediaPlayerService().getDuaration()));
        sbPlay.setMax(MediaPlayerService.getMediaPlayerService().getDuaration());
    }
    private void nextAudio() {
        if (MediaPlayerService.getMediaPlayerService() != null) {
            StorageUtil storage = new StorageUtil(MainActivity.this);

            storage.storeAudioIndex(mPostion);
            Log.e(TAG, "nextAudio:"+mPostion );
            MediaPlayerService.getMediaPlayerService().skipToNext();
            mPostion = storage.loadAudioIndex();
            Log.e(TAG, "nextAudio2:"+mPostion );
            setTextSong();
        } else {
            Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StorageUtil storage = new StorageUtil(MainActivity.this);
                    storage.storeAudioIndex(mPostion);
                    playAudio(0,mSongList);
                    setTextSong();
                }
            }, 200);
            playAudio(0,mSongList);
            playSycle();

        }
    }

    private void prevAudio() {
        if (MediaPlayerService.getMediaPlayerService() != null) {
            StorageUtil storage = new StorageUtil(MainActivity.this);
            storage.storeAudioIndex(mPostion);
            MediaPlayerService.getMediaPlayerService().skipToPrevious();
            setTextSong();
        } else {
            Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StorageUtil storage = new StorageUtil(MainActivity.this);
                    storage.storeAudioIndex(mPostion);
                    playAudio(0,mSongList);
                    setTextSong();
                }
            }, 200);
            StorageUtil storage = new StorageUtil(MainActivity.this);
            storage.storeAudioIndex(mPostion);
            playAudio(0,mSongList);
            playSycle();
        }

    }

    private void loopAudio() {
        if (MediaPlayerService.getMediaPlayerService().setLoopAudio()) {
            imbLoopSong.setImageResource(R.drawable.ic_loop_enable);
        } else {
            imbLoopSong.setImageResource(R.drawable.ic_loop_disable);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imb_play_song:
                playAudio();
                break;
            case R.id.imb_next_song:
                nextAudio();
                break;
            case R.id.imb_prev_song:
                prevAudio();
                break;
            case R.id.imb_favorite:
                //favoriteAudio();
                break;
            case R.id.imb_loop_song:
                loopAudio();
                break;
            case R.id.imb_play_song_bottom:
                playAudio();
                break;
            case R.id.imb_next_song_bottom:
                nextAudio();
                break;
            case R.id.imb_prev_song_bottom:
                prevAudio();
                break;
        }

    }
    private String forMatSongName(String name){
        String songName="";
        if(name.length()>30){
            songName = name.substring(0,30) +"...";
        }
        else {
            songName = name;
        }
        return songName;
    }
    @Override
    public void onResume() {
        super.onResume();
        if(MediaPlayerService.getMediaPlayerService() != null){
            setTextSong();
        }
    }

    @Override
    public void onItemClickListener(final ArrayList<Song> songList, final int postion) {
        Log.e(TAG, postion+"///"+songList.size());
        StorageUtil storage = new StorageUtil(MainActivity.this);
        storage.storeAudioIndex(postion);
        storage.storeAudio(songList);
        mSongList = songList;
        if (MediaPlayerService.getMediaPlayerService() == null) {
            Intent intent = new Intent(MainActivity.this, MediaPlayerService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MediaPlayerService.getMediaPlayerService().updateList();
                    playAudio(postion,songList);
                    setTextSong();
                    playSycle();
                }
            }, 200);
            playSycle();
            playAudio(postion,songList);
            mPostion = postion;
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            // new UpdateSeekBar().execute();
        } else {
            MediaPlayerService.getMediaPlayerService().updateList();
            playAudio(postion,songList);
            mPostion = postion;
            setTextSong();
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            playSycle();
            // new UpdateSeekBar().execute();
        }
    }
}
