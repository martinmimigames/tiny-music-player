package com.martinmimigames.tinymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.io.File;

import mg.utils.notify.NotificationHelper;

class Notifications {

  /**
   * notification channel id
   */
  public static final String NOTIFICATION_CHANNEL = "nc";

  private static final String TAP_TO_CLOSE = "Tap to close";

  /**
   * notification id
   */
  public static final int NOTIFICATION = 1;
  private final Service service;
  /**
   * notification for playback control
   */
  Notification notification;
  Notification.Builder builder;

  public Notifications(Service service) {
    this.service = service;
  }

  public void create() {
    if (Build.VERSION.SDK_INT >= 26) {
      /* create a notification channel */
      var name = "playback control";
      var description = "Allows for control over audio playback.";
      var importance = NotificationManager.IMPORTANCE_LOW;
      var notificationChannel = NotificationHelper.setupNotificationChannel(service, NOTIFICATION_CHANNEL, name, description, importance);
      notificationChannel.setSound(null, null);
      notificationChannel.setVibrationPattern(null);
    }
  }

  /**
   * setup notification properties
   *
   * @param title           title of notification (title of file)
   * @param playPauseIntent pending intent for pause/play audio
   * @param killIntent      pending intent for closing the service
   */
  void setupNotificationBuilder(String title, PendingIntent playPauseIntent, PendingIntent killIntent) {
    if (Build.VERSION.SDK_INT < 11) return;

    // create builder instance
    if (Build.VERSION.SDK_INT >= 26) {
      builder = new Notification.Builder(service, NOTIFICATION_CHANNEL);
    } else {
      builder = new Notification.Builder(service);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.setCategory(Notification.CATEGORY_SERVICE);
    }

    builder.setSmallIcon(R.drawable.ic_notif);
    builder.setContentTitle(title);
    builder.setSound(null);
    builder.setVibrate(null);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      builder.setContentIntent(playPauseIntent);
      builder.addAction(0, TAP_TO_CLOSE, killIntent);
    } else {
      builder.setContentText(TAP_TO_CLOSE);
      builder.setContentIntent(killIntent);
    }
  }

  /**
   * Switch to pause state
   */
  void pausePlayback() {
    // no notification controls < Jelly bean
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      builder.setContentText("Tap to start");
      buildNotification();
      update();
    }
  }

  /**
   * Switch to play state
   */
  void startPlayback() {
    // no notification controls < Jelly bean
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      builder.setContentText("Tap to stop");
      buildNotification();
      update();
    }
  }

  /**
   * Generate pending intents for service control
   *
   * @param id     the id for the intent
   * @param action the control action
   * @return the pending intent generated
   */
  PendingIntent genIntent(int id, byte action) {
    /* flags for control logics on notification */
    var pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE)
      pendingIntentFlag |= PendingIntent.FLAG_UPDATE_CURRENT;

    var intentFlag = Intent.FLAG_ACTIVITY_NO_HISTORY;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
      intentFlag |= Intent.FLAG_ACTIVITY_NO_ANIMATION;

    return PendingIntent
      .getService(service, id, new Intent(service, Service.class)
          .addFlags(intentFlag)
          .putExtra(Launcher.TYPE, action)
          .putExtra(Launcher.SELF_IDENTIFIER, Launcher.SELF_IDENTIFIER_ID)
        , pendingIntentFlag);
  }

  /**
   * generate new notification
   */
  void genNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      buildNotification();
    } else {
      notification = new Notification();
    }
  }

  /**
   * build notification from notification builder
   */
  void buildNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      notification = builder.build();
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      notification = builder.getNotification();
    }
  }

  /**
   * setup notification properties
   *
   * @param title      title of notification (title of file)
   * @param killIntent pending intent for closing the service
   */
  void setupNotification(String title, PendingIntent killIntent) {
    if (Build.VERSION.SDK_INT < 11) {
      notification.contentView = new RemoteViews("com.martinmimigames.tinymusicplayer", R.layout.notif);
      notification.icon = R.drawable.ic_notif; // icon display
      notification.audioStreamType = AudioManager.STREAM_MUSIC;
      notification.sound = null;
      notification.contentIntent = killIntent;
      notification.contentView.setTextViewText(R.id.notif_title, title);
      notification.vibrate = null;
    }
  }

  /**
   * create and start playback control notification
   */
  void getNotification(final Uri uri) {

    /* setup notification variable */
    var title = new File(uri.getPath()).getName();

    /* calls for control logic by starting activity with flags */
    var killIntent = genIntent(1, Launcher.KILL);
    var playPauseIntent = genIntent(2, Launcher.PLAY_PAUSE);

    setupNotificationBuilder(title, playPauseIntent, killIntent);
    genNotification();
    setupNotification(title, killIntent);

    update();
  }

  /**
   * update notification content and place on stack
   */
  private void update() {
    NotificationHelper.send(service, NOTIFICATION, notification);
  }

  void destroy() {
    /* remove notification from stack */
    NotificationHelper.unsend(service, NOTIFICATION);
  }
}
