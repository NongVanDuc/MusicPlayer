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
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vanduc.musicplayer.R;
import com.vanduc.musicplayer.fragments.FragmentSong;
import com.vanduc.musicplayer.interFace.OnNotifiClickListner;
import com.vanduc.musicplayer.model.Song;
import com.vanduc.musicplayer.until.ControlUtils;
import com.vanduc.musicplayer.until.PlaybackStatus;
import com.vanduc.musicplayer.until.StorageUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener{
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

    public void setOnNotifiClickListner(OnNotifiClickListner onNotifiClickListner) {
        this.onNotifiClickListner = onNotifiClickListner;
    }

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    public static MediaPlayerService getMediaPlayerService(){
        return mediaPlayerService;
    }
public void updateList(){
        if(songList != null && songList.size()>0){
            songList.clear();
        }
    StorageUtil storage = new StorageUtil(getApplicationContext());
    songList = storage.loadAudio();
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
            stopSelf();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
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
            buildNotification(PlaybackStatus.PAUSED);
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
    public boolean getStateMedia(){
            if(mediaPlayer.isPlaying()){
                return true;
            }
            else return false;
    }
    public boolean setLoopAudio(){
        if(!mediaPlayer.isLooping() && mediaPlayer != null){
            mediaPlayer.setLooping(true);
            return true;
        }
        else
        {
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
    public void onCreate() {
        super.onCreate();
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
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            songList = storage.loadAudio();
            Log.e("onStartCommand: ", songList.size()+"" );
            audioIndex = storage.loadAudioIndex();
            mediaPlayerService = this;
            if (audioIndex != -1 && audioIndex < songList.size()) {
                //index is in a valid range
                activeSong = songList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
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
                stopSelf();
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
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
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
                onNotifiClickListner.onNotifiCationClick();
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                onNotifiClickListner.onNotifiCationClick();
                //buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                onNotifiClickListner.onNotifiCationClick();
                //updateMetaData();
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                onNotifiClickListner.onNotifiCationClick();
                //updateMetaData();
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt ;
        Bitmap bitmap = activeSong.getSmallCover(this);
        if(bitmap != null){
            albumArt = bitmap;
        }
        else {albumArt = BitmapFactory.decodeResource(getResources(),R.drawable.iconmusic);} //replace with medias albumArt
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
        if(mediaPlayer != null && songList != null && songList.size()>0){
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
    public int getIndex(){
        return audioIndex;
    }
    public void playAudioFromList(int index){
        stopMedia();
        activeSong = songList.get(index);
        initMediaPlayer();
        mediaPlayer.start();
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
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
    }
    // end control audio
    // creat notifycationManager

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = R.drawable.ic_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon ;
        Bitmap bitmap = activeSong.getSmallCover(this);
        if(bitmap != null){
            largeIcon = bitmap;
        }
        else {largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.bg_now_play_ing);} //replace with medias albumArt

        // Create a new Notification
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)

                .setShowWhen(false)
                // Set the Notification style
//                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
//                        // Attach our MediaSession token
//                        .setMediaSession(mediaSession.getSessionToken())
//                        // Show our playback controls in the compact notification view.
//                        .setShowActionsInCompactView(0, 1, 2))
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
                .addAction(R.drawable.ic_skip_previous, "previous", playbackAction(3))
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



        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
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
        onNotifiClickListner.onNotifiCationClick();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
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
        Log.e( "getDuaration: ", mediaPlayer.getDuration()+"");
        return mediaPlayer.getDuration();

    }

    public int getCurrentPostion() {
        return mediaPlayer.getCurrentPosition();
    }
    public void seekToPos (int pos){
        mediaPlayer.seekTo(pos);
    }
}
