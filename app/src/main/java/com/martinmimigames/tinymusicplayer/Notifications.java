package com.martinmimigames.tinymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.io.File;

import mg.utils.notify.NotificationHelper;

public class Notifications {

  /**
   * notification channel id
   */
  public static final String NOTIFICATION_CHANNEL = "LittleMusicPlayer notifications";
  /**
   * notification id
   */
  public final int NOTIFICATION = 1;
  private final Service service;
  /**
   * notification for playback control
   */
  Notification notification;

  public Notifications(Service service) {
    this.service = service;
  }

  public void create() {
    /* create a notification channel */
    final CharSequence name = "playback control";
    final String description = "Allows for control over audio playback.";
    final int importance = (Build.VERSION.SDK_INT > 24) ? NotificationManager.IMPORTANCE_LOW : 0;
    NotificationHelper.setupNotificationChannel(service, NOTIFICATION_CHANNEL, name, description, importance);
  }

  /**
   * Switch to pause state
   */
  void pausePlayback() {
    NotificationHelper.setText(notification, "Tap to start");
    update();
  }

  /**
   * Switch to play state
   */
  void startPlayback() {
    NotificationHelper.setText(notification, "Tap to stop");
    update();
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
    int pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE)
      pendingIntentFlag |= PendingIntent.FLAG_UPDATE_CURRENT;

    int intentFlag = Intent.FLAG_ACTIVITY_NO_HISTORY;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
      intentFlag |= Intent.FLAG_ACTIVITY_NO_ANIMATION;

    return PendingIntent
      .getService(service, id, new Intent(service, Service.class)
          .addFlags(intentFlag)
          .putExtra(ServiceControl.TYPE, action)
          .putExtra(ServiceControl.SELF_IDENTIFIER, ServiceControl.SELF_IDENTIFIER_ID)
        , pendingIntentFlag);
  }

  /**
   * create and start playback control notification
   */
  void getNotification(final Uri uri) {

    /* setup notification variable */
    final String title = new File(uri.getPath()).getName();

    notification = NotificationHelper.createNotification(service, NOTIFICATION_CHANNEL, (Build.VERSION.SDK_INT > 21) ? android.app.Notification.CATEGORY_SERVICE : null);

    notification.icon = R.drawable.ic_notif; // icon display
    notification.defaults = Notification.DEFAULT_ALL; // set defaults
    notification.when = System.currentTimeMillis(); // set time of notification
    notification.tickerText = title;// set popup text
    notification.flags = Notification.FLAG_AUTO_CANCEL; // automatically close popup
    notification.audioStreamType = Notification.STREAM_DEFAULT;
    notification.sound = null;


    /* calls for control logic by starting activity with flags */
    final PendingIntent killIntent = genIntent(1, ServiceControl.KILL);

    /* extra variables for notification setup */
    /* different depending on sdk version as they require different logic */
    if (Build.VERSION.SDK_INT >= 19) {

      notification.extras.putCharSequence(Notification.EXTRA_TITLE, title);
      notification.extras.putCharSequence(Notification.EXTRA_TEXT, "Tap to stop");

      notification.contentIntent = genIntent(2, ServiceControl.PLAY_PAUSE);

      notification.actions = new Notification.Action[]{
        new Notification.Action(R.drawable.ic_launcher, "close", killIntent)
      };
    } else {
      notification.contentView = new RemoteViews("com.martinmimigames.tinymusicplayer", R.layout.notif);
      NotificationHelper.setText(notification, R.id.notif_title, title);
      notification.contentIntent = killIntent;
    }

    /* set to not notify again when update */
    notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
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
