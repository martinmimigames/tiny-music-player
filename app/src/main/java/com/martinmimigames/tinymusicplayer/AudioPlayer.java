package com.martinmimigames.tinymusicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

import mg.utils.notify.ToastHelper;

public class AudioPlayer extends Thread implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

  private final Service service;
  private final MediaPlayer mediaPlayer;

  public AudioPlayer(Service service, Uri audioLocation) {
    this.service = service;
    /* initiate new audio player */
    mediaPlayer = new MediaPlayer();

    /* setup player variables */
    try {
      mediaPlayer.setDataSource(service, audioLocation);
    } catch (IllegalArgumentException e) {
      Exceptions.throwError(service, Exceptions.IllegalArgument);
      return;
    } catch (SecurityException e) {
      Exceptions.throwError(service, Exceptions.Security);
      return;
    } catch (IllegalStateException e) {
      Exceptions.throwError(service, Exceptions.IllegalState);
      return;
    } catch (IOException e) {
      Exceptions.throwError(service, Exceptions.IO);
      return;
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    } else {
      mediaPlayer.setAudioAttributes(
        new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
      );
    }

    mediaPlayer.setLooping(false);

    /* setup listeners for further logics */
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnCompletionListener(this);
  }

  @Override
  public void run() {
    try {
      /* get ready for playback */
      mediaPlayer.prepareAsync();
    } catch (IllegalStateException e) {
      Exceptions.throwError(service, Exceptions.IllegalState);
    }
  }

  /**
   * check if audio is playing
   */
  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  /**
   * Switch to play state
   */
  public void play() {
    mediaPlayer.start();
  }

  /**
   * Switch to pause state
   */
  public void pause() {
    mediaPlayer.pause();
  }

  /**
   * playback when ready
   */
  @Override
  public void onPrepared(MediaPlayer mp) {
    mediaPlayer.start();
  }

  /**
   * release resource when playback finished
   */
  @Override
  public void onCompletion(MediaPlayer mp) {
    service.stopSelf();
  }

  /**
   * release and kill service
   */
  @Override
  public void interrupt() {
    mediaPlayer.release();
    super.interrupt();
  }
}
