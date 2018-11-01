package org.allatra.radio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;

import wseemann.media.FFmpegMediaPlayer;

public class MainActivity extends AppCompatActivity
        implements
        View.OnClickListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private ImageButton buttonPlay;
    private ImageButton buttonStopPlay;
    private CheckBox checkRadio;
    private CheckBox checkClassic;
    private static final String RADIO_STREAM = "http://94.23.36.117:5551/stream";
    private static final String CLASSIC_STREAM = "http://eu6.fastcast4u.com:5551/classic";
    private FFmpegMediaPlayer mediaPlayer;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int permissionCheck;
    private AlertDialog.Builder dialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();
        if (permissionCheck == 1) callStateListener();
        else permission();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    callStateListener();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    dialogFragment.create();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void permission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @SuppressLint("ShowToast")
    private void initializeUIElements() {
        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);
        buttonPlay.setEnabled(true);

        buttonStopPlay = (ImageButton) findViewById(R.id.buttonStopPlay);
        buttonStopPlay.setEnabled(false);
        buttonStopPlay.setVisibility(View.INVISIBLE);
        buttonStopPlay.setOnClickListener(this);

        checkRadio = (CheckBox) findViewById(R.id.chb_radio);
        checkClassic = (CheckBox) findViewById(R.id.chb_classic);
        checkRadio.setChecked(true);

        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);

        dialogFragment = new AlertDialog.Builder(this)
                .setTitle(R.string.attantion)
                .setMessage(R.string.message_text)
                .setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait Mode
            ImageView background = (ImageView) findViewById(R.id.iv_portrait);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background.setBackground(getResources().getDrawable(R.drawable.back_land));
            } else {
                background.setImageDrawable(getResources().getDrawable(R.drawable.back_land));
            }
        } else {
            // Landscape Mode
            ImageView background = (ImageView) findViewById(R.id.iv_land);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background.setBackground(getResources().getDrawable(R.drawable.back_land));
            } else {
                background.setImageDrawable(getResources().getDrawable(R.drawable.back_land));
            }
        }

    }

    public void onClick(View v) {
        if (v == buttonPlay) {
            startPlaying();
        } else if (v == buttonStopPlay) {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mediaPlayer = new FFmpegMediaPlayer();
        mediaPlayer.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(FFmpegMediaPlayer mp) {
                mp.start();
            }
        });

        mediaPlayer.setOnErrorListener(new FFmpegMediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(FFmpegMediaPlayer mp, int what, int extra) {
                mp.release();
                return false;
            }
        });

        try {
            if (checkRadio.isChecked()) {
                mediaPlayer.setDataSource(RADIO_STREAM);
            } else {
                mediaPlayer.setDataSource(CLASSIC_STREAM);
            }
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonPlay.setVisibility(View.INVISIBLE);
        buttonStopPlay.setVisibility(View.VISIBLE);
        buttonStopPlay.setEnabled(true);
        buttonPlay.setEnabled(false);

        checkRadio.setEnabled(false);
        checkClassic.setEnabled(false);
    }

    private void stopPlaying() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();

        buttonStopPlay.setVisibility(View.INVISIBLE);
        buttonPlay.setEnabled(true);
        buttonPlay.setVisibility(View.VISIBLE);
        buttonStopPlay.setEnabled(false);
        checkClassic.setEnabled(true);
        checkRadio.setEnabled(true);

    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) startPlaying();
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
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
        }
    }

    public void onCheckBoxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.chb_radio:
                if (checked) {
                    checkClassic.setChecked(false);
                }
                break;
            case R.id.chb_classic:
                if (checked) {
                    checkRadio.setChecked(false);
                }
                break;
        }
    }

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
                            mediaPlayer.pause();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                mediaPlayer.start();
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
}
