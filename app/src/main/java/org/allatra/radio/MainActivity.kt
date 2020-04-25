package org.allatra.radio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener, OnAudioFocusChangeListener {
    private var uri: Uri = Uri.parse(RADIO_STREAM)
    private var audioPlayerService: AudioPlayerService? = null
    var serviceBound = false
    private var playWhenReady = true


    //Handle incoming phone calls
    private val ongoingCall = false
    private var permissionCheck = 0
    private var dialogFragment: AlertDialog.Builder? = null
    private var player: SimpleExoPlayer? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeUIElements()
        if (permissionCheck == 1) callStateListener() else permission()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this, "exoplayer-allatraradio")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player!!.release()
    }

    private fun releasePlayer() {
        playWhenReady = player!!.playWhenReady
        player!!.release()
        player = null
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceAllatRaRadioState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceAllatRaRadioState")
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    callStateListener()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    dialogFragment!!.create()
                }
            }
        }
    }

    private fun permission() {
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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private fun initializeUIElements() {
        buttonPlay.setOnClickListener(this)
        buttonPlay.isEnabled = true
        buttonStopPlay.isEnabled = false
        buttonStopPlay.visibility = View.INVISIBLE
        buttonStopPlay.setOnClickListener(this)
        chb_radio.isChecked = true

        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)

        dialogFragment = AlertDialog.Builder(this)
                .setTitle(R.string.attantion)
                .setMessage(R.string.message_text)
                .setNeutralButton(R.string.yes) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait Mode
            iv_portrait.background = ContextCompat.getDrawable(this, R.drawable.back)
        } else {
            // Landscape Mode
            iv_land.background = ContextCompat.getDrawable(this, R.drawable.back_land)
        }
    }

    override fun onClick(v: View) {
//        if (serviceBound) {
        if (v === buttonPlay) {
            if (chb_radio!!.isChecked) {
//                    audioPlayerService!!.startPlayRadio()
                uri = Uri.parse(RADIO_STREAM)
            } else {
                uri = Uri.parse(CLASSIC_STREAM)
//                    audioPlayerService!!.startPlayClassic()
            }
            buttonPlay!!.visibility = View.INVISIBLE
            buttonStopPlay!!.visibility = View.VISIBLE
            buttonStopPlay!!.isEnabled = true
            buttonPlay!!.isEnabled = false
            chb_radio!!.isEnabled = false
            chb_classic!!.isEnabled = false
            startPlaying()
        } else if (v === buttonStopPlay) {
//                audioPlayerService!!.stopMedia()
            buttonStopPlay!!.visibility = View.INVISIBLE
            buttonPlay!!.isEnabled = true
            buttonPlay!!.visibility = View.VISIBLE
            buttonStopPlay!!.isEnabled = false
            chb_classic!!.isEnabled = true
            chb_radio!!.isEnabled = true
            stopPlaying()
        }
//        }
    }

    private fun startPlaying() {
        player.run {
            val mediaSource = buildMediaSource(uri)
            mediaSource?.let { player!!.prepare(it, false, false) }
            player!!.playWhenReady = playWhenReady
        }

        buttonPlay!!.visibility = View.INVISIBLE
        buttonStopPlay!!.visibility = View.VISIBLE
        buttonStopPlay!!.isEnabled = true
        buttonPlay!!.isEnabled = false
        chb_radio!!.isEnabled = false
        chb_classic!!.isEnabled = false
    }

    private fun stopPlaying() {
        playWhenReady = player!!.playWhenReady
        player!!.stop()

        buttonStopPlay!!.visibility = View.INVISIBLE
        buttonPlay!!.isEnabled = true
        buttonPlay!!.visibility = View.VISIBLE
        buttonStopPlay!!.isEnabled = false
        chb_classic!!.isEnabled = true
        chb_radio!!.isEnabled = true
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                startPlaying()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                stopPlaying()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                stopPlaying()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                stopPlaying()
            }
        }
    }

    fun onCheckBoxClicked(view: View) {
        // Is the view now checked?
        val checked = (view as CheckBox).isChecked
        when (view.getId()) {
            R.id.chb_radio -> if (checked) {
                chb_classic!!.isChecked = false
            }
            R.id.chb_classic -> if (checked) {
                chb_radio!!.isChecked = false
            }
        }
    }

    private fun callStateListener() {
        // Get the telephony manager
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        val phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                        stopPlaying()
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        startPlaying()
                    }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1
        const val RADIO_STREAM = "http://116.202.18.122/radio/8010/radio.mp3"
        const val CLASSIC_STREAM = "http://116.202.18.122/radio/8000/radio.mp3?1584817375"
        const val Broadcast_PLAY_NEW_AUDIO = "org.allatra.radio.audioplayer.PlayNewAudio"
    }
}