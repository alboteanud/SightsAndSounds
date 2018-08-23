package com.craiovadata.sightsandsounds;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;


public class IntentServicePlayer extends IntentService {
    private static final String ACTION_PLAY_STOP_PLAYER = "com.craiovadata.sightsandsounds.action.PLAY_OR_STOP";
    private static final String ACTION_RESET_PLAYER = "com.craiovadata.sightsandsounds.action.RESET";
    private static final String ACTION_PAUSE_PLAYER = "com.craiovadata.sightsandsounds.action.PAUSE";
    private static final String EXTRA_PARAM_ID = "com.craiovadata.sightsandsounds.extra.ID";
    static MediaPlayer mediaPlayer;

    public IntentServicePlayer() {
        super("IntentServicePlayer");
    }

    public static void startActionPlayOrStopPlayer(Context context, String id) {
        Intent intent = new Intent(context, IntentServicePlayer.class);
        intent.setAction(ACTION_PLAY_STOP_PLAYER);
        intent.putExtra(EXTRA_PARAM_ID, id);
        context.startService(intent);
    }

    public static void startActionResetPlayer(Context context) {
        Intent intent = new Intent(context, IntentServicePlayer.class);
        intent.setAction(ACTION_RESET_PLAYER);
        context.startService(intent);
    }

    public static void startActionPausePlayer(Context context) {
        Intent intent = new Intent(context, IntentServicePlayer.class);
        intent.setAction(ACTION_PAUSE_PLAYER);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY_STOP_PLAYER.equals(action)) {
                final String id = intent.getStringExtra(EXTRA_PARAM_ID);
                handleActionPlayStop(id);
            } else if (ACTION_RESET_PLAYER.equals(action)) {
                handleActionResetPlayer();
            } else if (ACTION_PAUSE_PLAYER.equals(action)) {
                handleActionPausePlayer();
            }
        }
    }

    private void handleActionPausePlayer() {
        pausePlayer();
    }

    private void handleActionResetPlayer() {
        resetPlayer();
    }

    private void handleActionPlayStop(String id) {
        if (mediaPlayer == null) {
            checkSoundAvailabilityThenPlay(id);
        } else if (mediaPlayer.isPlaying()) {
            pausePlayer();
        } else {
            try {
//                setVolumeControlStream(AudioManager.STREAM_MUSIC);
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mediaPlayer.release();
                mediaPlayer = null;
            }

        }
    }

    private void prepareAndPlay(Uri uri) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    findViewById(R.id.play_sound).setVisibility(View.VISIBLE);
                    mp.start();
                }
            });
        } else
            mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  // nu e neaparat
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
//                MainActivity.loadOrShowInterstitial(context);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mediaPlayer.release();
                mediaPlayer = null;
                return true;
            }
        });
        mediaPlayer.prepareAsync();
    }

    private void checkSoundAvailabilityThenPlay(String id) {
        StorageReference soundRef = FirebaseStorage.getInstance().getReference().child("sounds/" + id + ".mp3");
        Task<Uri> downloadUrl = soundRef.getDownloadUrl();
        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                prepareAndPlay(uri);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        findViewById(R.id.play_sound).setVisibility(View.GONE);
                    }
                });
    }

    private void pausePlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    private void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


   
}
