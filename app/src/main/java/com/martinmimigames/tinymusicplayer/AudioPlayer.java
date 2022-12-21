package com.martinmimigames.tinymusicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

class AudioPlayer extends Thread implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

  private final Service service;
  private final MediaPlayer mediaPlayer;

  /**
   * Initiate an audio player, throws exceptions if failed.
   *
   * @param service       the service initialising this.
   * @param audioLocation the Uri containing the location of the audio.
   * @throws IllegalArgumentException when the media player need cookies, but we do not supply it.
   * @throws IllegalStateException    when the media player is not in the correct state.
   * @throws SecurityException        when the audio file is protected and cannot be played.
   * @throws IOException              when the audio file cannot be read.
   */
  public AudioPlayer(Service service, Uri audioLocation) throws IllegalArgumentException, IllegalStateException, SecurityException, IOException {
    this.service = service;
    /* initiate new audio player */
    mediaPlayer = new MediaPlayer();

    /* setup player variables */
    mediaPlayer.setDataSource(service, audioLocation);

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
    /* get ready for playback */
    try {
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
