package com.martinmimigames.tinymusicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

/**
 * activity for controlling the playback by invoking different logics based on incoming intents
 */
public class Launcher extends Activity {


  static final String TYPE = "type";
  static final byte NULL = 0;
  static final byte PLAY_PAUSE = 1;
  static final byte KILL = 2;
  static final byte PLAY = 3;
  static final byte PAUSE = 4;

  private static final int REQUEST_CODE = 3216487;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!Intent.ACTION_VIEW.equals(getIntent().getAction())
      && !Intent.ACTION_SEND.equals(getIntent().getAction())) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        this.getPackageManager()
          .checkPermission(
            Manifest.permission.POST_NOTIFICATIONS, this.getPackageName())
          != PackageManager.PERMISSION_GRANTED) {
        final var intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
        this.startActivity(intent);
        finish();
        return;
      }

      /* request a file from the system */
      var intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("audio/*"); // intent type to filter application based on your requirement
      startActivityForResult(intent, REQUEST_CODE);
      return;
    }
    onIntent(getIntent());
  }

  /**
   * redirect call to actual logic
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    onIntent(intent);
  }

  /**
   * restarts service
   */
  private void onIntent(Intent intent) {
    intent.setClass(this, Service.class);
    stopService(intent);
    startService(intent);
    /* does not need to keep this activity */
    finish();
  }

  /**
   * call service control on receiving file
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    /* if result unusable, discard */
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      /* redirect to service */
      intent.setAction(Intent.ACTION_VIEW);
      onIntent(intent);
      return;
    }
    finish();
  }
}