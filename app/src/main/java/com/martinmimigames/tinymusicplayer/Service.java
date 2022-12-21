package com.martinmimigames.tinymusicplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

/**
 * service for playing music
 */
public class Service extends android.app.Service {

  final SessionBroadcastControl sbc;
  final Notifications nm;
  /**
   * audio playing logic class
   */
  private AudioPlayer audioPlayer;

  public Service() {
    sbc = new SessionBroadcastControl(this);
    nm = new Notifications(this);
  }

  /**
   * unused
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * setup
   */
  @Override
  public void onCreate() {
    sbc.create();
    nm.create();

    super.onCreate();
  }

  /**
   * startup logic
   */
  @Override
  public void onStart(final Intent intent, final int startId) {
    /* check if called from self */
    if (intent.getByteExtra(ServiceControl.SELF_IDENTIFIER, ServiceControl.NULL) == ServiceControl.SELF_IDENTIFIER_ID) {
      switch (intent.getByteExtra(ServiceControl.TYPE, ServiceControl.NULL)) {
        /* start or pause audio playback */
        case ServiceControl.PLAY_PAUSE -> playPause();
        case ServiceControl.PLAY -> play();
        case ServiceControl.PAUSE -> pause();

        /* cancel audio playback and kill service */
        case ServiceControl.KILL -> stopSelf();
      }
      return;
    } else {
      switch (intent.getAction()) {
        case Intent.ACTION_VIEW -> setAudio(intent.getData());
        case Intent.ACTION_SEND -> setAudio(intent.getParcelableExtra(Intent.EXTRA_STREAM));
      }
    }
  }

  void setAudio(final Uri audioLocation) {
    /* create notification for playback control */
    nm.getNotification(audioLocation);

    /* start service as foreground */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
      startForeground(nm.NOTIFICATION, nm.notification);

    /* get audio playback logic and start async */
    audioPlayer = new AudioPlayer(this, audioLocation);
    audioPlayer.start();
  }

  /**
   * Switch to play or pause state, depending on current state
   */
  void playPause() {
    if (audioPlayer.isPlaying())
      pause();
    else
      play();
  }

  /**
   * Switch to play state
   */
  void play() {
    audioPlayer.play();
    sbc.play();
    nm.startPlayback();
  }

  /**
   * Switch to pause state
   */
  void pause() {
    audioPlayer.pause();
    sbc.pause();
    nm.pausePlayback();
  }

  /**
   * forward to startup logic for newer androids
   */
  @TargetApi(Build.VERSION_CODES.ECLAIR)
  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  /**
   * service killing logic
   */
  @Override
  public void onDestroy() {
    nm.destroy();
    sbc.destroy();
    /* interrupt audio playback logic */
    if (!audioPlayer.isInterrupted()) audioPlayer.interrupt();

    super.onDestroy();
  }
}
