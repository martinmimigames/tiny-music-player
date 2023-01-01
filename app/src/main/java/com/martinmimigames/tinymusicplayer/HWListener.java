package com.martinmimigames.tinymusicplayer;


import static android.content.Intent.EXTRA_KEY_EVENT;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.view.KeyEvent;

/**
 * Hardware Listener for button controls
 */
public class HWListener extends BroadcastReceiver {

  private Service service;
  private MediaSession mediaSession;
  private PlaybackState.Builder playbackStateBuilder;
  private ComponentName cn;

  /**
   * Required for older android versions,
   * initialized by the system
   */
  public HWListener() {
    super();
  }

  /**
   * Returns an instance, only useful when SDK_INT >= LOLLIPOP
   *
   * @param service the music service
   */
  public HWListener(Service service) {
    this.service = service;
  }

  /**
   * Initializer, only useful when SDK_INT >= LOLLIPOP
   */
  void create() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mediaSession = new MediaSession(service, HWListener.class.toString());

      mediaSession.setCallback(new MediaSession.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
          onReceive(service, mediaButtonIntent);
          return super.onMediaButtonEvent(mediaButtonIntent);
        }
      });

      playbackStateBuilder = new PlaybackState.Builder();
      playbackStateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE);
      mediaSession.setPlaybackState(playbackStateBuilder.build());

      mediaSession.setActive(true);
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        cn = new ComponentName(service, HWListener.class);
        ((AudioManager) service.getSystemService(Context.AUDIO_SERVICE)).registerMediaButtonEventReceiver(cn);
      }
      service.registerReceiver(this, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
    }
  }

  /**
   * Switch to play state, only useful when SDK_INT >= LOLLIPOP
   */
  void play() {
    if (Build.VERSION.SDK_INT >= 21) {
      playbackStateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1);
      mediaSession.setPlaybackState(playbackStateBuilder.build());
    }
  }

  /**
   * Switch to play state, only useful when SDK_INT >= LOLLIPOP
   */
  void pause() {
    if (Build.VERSION.SDK_INT >= 21) {
      playbackStateBuilder.setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
      mediaSession.setPlaybackState(playbackStateBuilder.build());
    }
  }

  /**
   * Get ready to be destroyed, only useful when SDK_INT >= LOLLIPOP
   */
  void destroy() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mediaSession.setActive(false);
      mediaSession.release();
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        ((AudioManager) service.getSystemService(Context.AUDIO_SERVICE)).unregisterMediaButtonEventReceiver(cn);
      }
      service.unregisterReceiver(this);
    }
  }

  /**
   * Responds to media keycodes (ie. from bluetooth ear phones, etc.).
   * Does not connect directly to service variable because service may not be initialized.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    var event = (KeyEvent) intent.getParcelableExtra(EXTRA_KEY_EVENT);
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      intent = new Intent(context, Service.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          intent.putExtra(Launcher.TYPE, Launcher.PLAY);
          break;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          intent.putExtra(Launcher.TYPE, Launcher.PAUSE);
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
          intent.putExtra(Launcher.TYPE, Launcher.PLAY_PAUSE);
          break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
          intent.putExtra(Launcher.TYPE, Launcher.KILL);
          break;
        default:
          return;
      }
      context.startService(intent);
    }
  }
}