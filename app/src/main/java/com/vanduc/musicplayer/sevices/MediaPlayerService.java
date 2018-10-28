package com.vanduc.musicplayer.sevices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.fragments.FragmentSong;
import com.vanduc.musicplayer.interFace.OnNotifiClickListner;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.screens.HomeActivity;
import com.vanduc.musicplayer.util.ControlUtils;
import com.vanduc.musicplayer.util.PlaybackStatus;
import com.vanduc.musicplayer.util.StorageUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {
    public static MediaPlayerService mediaPlayerService = null;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    //path to the audio file
    private String mediaFile;
    //Used to pause/resume MediaPlayer
    private int resumePosition;
    //List of available Song files
    private ArrayList<Song> songList;
    private int audioIndex = -1;
    private Song activeSong; //an object of the currently playing audio
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    // User Interactions
    public static final String ACTION_PLAY = "com.vanduc.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.vanduc.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.vanduc.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.vanduc.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.vanduc.musicplayer.ACTION_STOP";

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private OnNotifiClickListner onNotifiClickListner;
    private NotificationManagerCompat mNotificationManager;
    private Context mContext;
    public void setOnNotifiClickListner(OnNotifiClickListner onNotifiClickListner) {
        this.onNotifiClickListner = onNotifiClickListner;
    }

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    public static MediaPlayerService getMediaPlayerService() {
        return mediaPlayerService;
    }

    public void updateList() {
        if (songList != null && songList.size() > 0) {
            songList.clear();
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());
        songList = storage.loadAudio();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = NotificationManagerCompat.from(this);
        mediaPlayerService = this;
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Song to play -- BroadcastReceiver
        register_playNewAudio();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mContext= getApplicationContext();
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(this);
            songList = storage.loadAudio();
            Log.e("onStartCommand: ", songList.size() + "");
            audioIndex = storage.loadAudioIndex();
            mediaPlayerService = this;
            if (audioIndex != -1 && audioIndex < songList.size()) {
                //index is in a valid range
                activeSong = songList.get(audioIndex);
            } else {
                //stopSelf();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            //stopSelf();
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            //stopSelf();
        }

        if (mediaSession == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (Exception e) {
                e.printStackTrace();
                //stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return START_NOT_STICKY;
    }
    // initMediaPlayer
    public void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeSong.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            //stopSelf();
        }
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Không thể phát bài hát !", Toast.LENGTH_SHORT).show();
        }
    }
    // function control media

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
             buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            buildNotification(PlaybackStatus.STOP);
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            buildNotification(PlaybackStatus.PAUSED);
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public boolean getStateMedia() {
        if (mediaPlayer.isPlaying()) {
            return true;
        } else return false;
    }

    public boolean setLoopAudio() {
        if (!mediaPlayer.isLooping() && mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            return true;
        } else {
            mediaPlayer.setLooping(false);
            return false;
        }
    }

    // end funMeida
    // regsiter broadcatservice
    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    // end resigter broadcastservice

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy: " );
        mediaPlayerService = null;
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        //clear cached playlist
        if(HomeActivity.getHomeActivity() == null){
           new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
        }

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    // resigter broadcast
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < songList.size()) {
                //index is in a valid range
                activeSong = songList.get(audioIndex);
            } else {
                //stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Song
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(FragmentSong.BROADCAST_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    // end resigter
    // user background
    private void initMediaSession() throws RemoteException {
        if (mediaSession != null) return; //mediaSessionManager exists

        //mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                if(HomeActivity.getHomeActivity() != null){
                    onNotifiClickListner.onNotifiCationClick();
                }
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                if(HomeActivity.getHomeActivity() != null){
                    onNotifiClickListner.onNotifiCationClick();
                }

                //buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                if(HomeActivity.getHomeActivity() != null){
                    onNotifiClickListner.onNotifiCationClick();
                }
                //updateMetaData();
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                if(HomeActivity.getHomeActivity() != null){
                    onNotifiClickListner.onNotifiCationClick();
                }
                //updateMetaData();
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                //Stop the service
                stopMedia();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt;
        Bitmap bitmap = activeSong.getSmallCover(this);
        if (bitmap != null) {
            albumArt = bitmap;
        } else {
            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.iconmusic);
        } //replace with medias albumArt
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeSong.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getTitle())
                .build());
    }

    // end user background
    // control audio
    public void skipToNext() {
        if (mediaPlayer != null && songList != null && songList.size() > 0) {
            if (audioIndex == songList.size() - 1) {
                //if last in playlist
                audioIndex = 0;
                activeSong = songList.get(audioIndex);
            } else {
                //get next in playlist
                activeSong = songList.get(++audioIndex);
            }

            //Update stored index
            new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

            stopMedia();
            //reset mediaPlayer
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public int getIndex() {
        return audioIndex;
    }

    public void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of songList
            audioIndex = songList.size() - 1;
            activeSong = songList.get(audioIndex);
        } else {
            //get previous in playlist
            activeSong = songList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(mContext).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
    }
    // end control audio
    // creat notifycationManager

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = R.drawable.ic_pause;//needs to be initialized
        int notificationActionStop = R.drawable.ic_skip_previous;//needs to be initialized
        PendingIntent play_pauseAction = null;
        PendingIntent prev_close = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause;
            notificationActionStop = R.drawable.ic_skip_previous;
            //create the pause action
            play_pauseAction = playbackAction(1);
            prev_close = playbackAction(11);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play;
            notificationActionStop = R.drawable.ic_close;
            prev_close = playbackAction(10);
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon;
        Bitmap bitmap = activeSong.getSmallCover(mContext);
        if (bitmap != null) {
            largeIcon = bitmap;
        } else {
            largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.music_image);
        } //replace with medias albumArt

        // Create a new Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)

                .setShowWhen(false)
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeSong.getArtist())
                .setContentTitle(activeSong.getDisplay_name())
                .setContentInfo(activeSong.getTitle())
                // Add playback actions
                .addAction(notificationActionStop, "previous", prev_close)
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_skip_next, "next", playbackAction(2));
        if (ControlUtils.isJellyBeanMR1()) {
            builder.setShowWhen(false);
        }

        if (ControlUtils.isLollipop()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2, 3);
            builder.setStyle(style);
        }
        if (ControlUtils.isOreo()) {
            builder.setColorized(true);
        }
        Notification notification = builder.build();
        builder.setPublicVersion(notification);

       startForeground(NOTIFICATION_ID, notification);

    }

    private void removeNotification() {
        stopForeground(false);
        getNotificationManager().cancel(NOTIFICATION_ID);
    }

    private NotificationManager getNotificationManager(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    // end notifycationmanager

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 10:
                // stop track
                playbackAction.setAction(ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 11:
                // stop track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    // handleIncomming
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skipToNext();
        if(HomeActivity.getHomeActivity() != null){
            onNotifiClickListner.onNotifiCationClick();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:

                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:

                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // wwhn music already to play
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    public String getTimePlay() {
        int durationTime = mediaPlayer.getDuration();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(durationTime);
    }

    public int getDuaration() {
        return mediaPlayer.getDuration();

    }

    public int getCurrentPostion() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekToPos(int pos) {
        mediaPlayer.seekTo(pos);
    }
}
