package org.allatra.radio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

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
    public static final String RADIO_STREAM = "http://94.23.36.117:5551/stream";
    public static final String CLASSIC_STREAM = "http://eu6.fastcast4u.com:5551/classic";
    public static final String Broadcast_PLAY_NEW_AUDIO = "org.allatra.radio.audioplayer.PlayNewAudio";
    private FFmpegMediaPlayer fFmpegMediaPlayer;

    private AudioPlayerService audioPlayerService;
    boolean serviceBound = false;
    private ArrayList<Audio> audioList;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private int permissionCheck;
    private AlertDialog.Builder dialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();
        if (permissionCheck == 1) callStateListener();
        else permission();

//        loadAudio();
        //play the first audio in the ArrayList
//        playAudio(audioList.get(0).getData());

    }


    @Override
    protected void onStart() {
        super.onStart();
        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            audioPlayerService.stopSelf();
        }
//        releaseMP();
    }

    @Override
    protected void onPause() {
//        fFmpegMediaPlayer.stop();
//        fFmpegMediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
//        if (checkRadio.isChecked())
//            startPlaying();
//            fFmpegMediaPlayer = new FFmpegMediaPlayer();
//        fFmpegMediaPlayer.setLooping(false);
//        fFmpegMediaPlayer.start();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);
        buttonPlay.setEnabled(true);

        buttonStopPlay = findViewById(R.id.buttonStopPlay);
        buttonStopPlay.setEnabled(false);
        buttonStopPlay.setVisibility(View.INVISIBLE);
        buttonStopPlay.setOnClickListener(this);

        checkRadio = findViewById(R.id.chb_radio);
        checkClassic = findViewById(R.id.chb_classic);
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
            ImageView background = findViewById(R.id.iv_portrait);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background.setBackground(getResources().getDrawable(R.drawable.back));
            } else {
                background.setImageDrawable(getResources().getDrawable(R.drawable.back));
            }
        } else {
            // Landscape Mode
            ImageView background = findViewById(R.id.iv_land);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background.setBackground(getResources().getDrawable(R.drawable.back_land));
            } else {
                background.setImageDrawable(getResources().getDrawable(R.drawable.back_land));
            }
        }

    }

    public void onClick(View v) {
        if (serviceBound)
            if (v == buttonPlay) {
                if (checkRadio.isChecked()) {
                    audioPlayerService.startPlayRadio();
                } else {
                    audioPlayerService.startPlayClassic();
                }
                buttonPlay.setVisibility(View.INVISIBLE);
                buttonStopPlay.setVisibility(View.VISIBLE);
                buttonStopPlay.setEnabled(true);
                buttonPlay.setEnabled(false);

                checkRadio.setEnabled(false);
                checkClassic.setEnabled(false);
//            startPlaying();
            } else if (v == buttonStopPlay) {
                audioPlayerService.stopMedia();

                buttonStopPlay.setVisibility(View.INVISIBLE);
                buttonPlay.setEnabled(true);
                buttonPlay.setVisibility(View.VISIBLE);
                buttonStopPlay.setEnabled(false);
                checkClassic.setEnabled(true);
                checkRadio.setEnabled(true);
//            stopPlaying();
            }
    }

    private void startPlaying() {
        fFmpegMediaPlayer = new FFmpegMediaPlayer();
        fFmpegMediaPlayer.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(FFmpegMediaPlayer mp) {
                mp.start();
            }
        });

        fFmpegMediaPlayer.setOnErrorListener(new FFmpegMediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(FFmpegMediaPlayer mp, int what, int extra) {
                mp.release();
                return false;
            }
        });

        try {
            if (checkRadio.isChecked()) {
                fFmpegMediaPlayer.setDataSource(RADIO_STREAM);
            } else {
                fFmpegMediaPlayer.setDataSource(CLASSIC_STREAM);
            }
            fFmpegMediaPlayer.prepareAsync();
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
        if (fFmpegMediaPlayer.isPlaying())
            fFmpegMediaPlayer.stop();

        buttonStopPlay.setVisibility(View.INVISIBLE);
        buttonPlay.setEnabled(true);
        buttonPlay.setVisibility(View.VISIBLE);
        buttonStopPlay.setEnabled(false);
        checkClassic.setEnabled(true);
        checkRadio.setEnabled(true);

    }

    private void releaseMP() {
        if (fFmpegMediaPlayer != null) {
            try {
                fFmpegMediaPlayer.release();
                fFmpegMediaPlayer = null;
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
                if (fFmpegMediaPlayer == null) startPlaying();
                else if (!fFmpegMediaPlayer.isPlaying()) fFmpegMediaPlayer.start();
                fFmpegMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (fFmpegMediaPlayer.isPlaying()) fFmpegMediaPlayer.stop();
                fFmpegMediaPlayer.release();
                fFmpegMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (fFmpegMediaPlayer.isPlaying()) fFmpegMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (fFmpegMediaPlayer.isPlaying()) fFmpegMediaPlayer.pause();
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
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (fFmpegMediaPlayer != null) {
                            fFmpegMediaPlayer.pause();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (fFmpegMediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                fFmpegMediaPlayer.start();
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

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, AudioPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void startService() {
        Intent playerIntent = new Intent(this, AudioPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
