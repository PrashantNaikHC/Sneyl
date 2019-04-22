package com.hyperclock.prashant.sliderapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView;

    SensorManager mySensorManager;
    Sensor myProximitySensor;

    public Vibrator vibration;

    Runnable runnableCode = null;
    final Handler handler = new Handler();

    private int time = 0;
    private boolean PROXIMITY = false;

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.help:
                ToastMessage(getString(R.string.instructions));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnableCode);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);



        releaseMediaPlayer();

        //initialize vibration
        vibration = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        mySensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);
        if (myProximitySensor == null) {
            ToastMessage("No Proximity sensor setup.");
        } else {
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if(PROXIMITY){
            runnableCode = new Runnable() {
                @Override
                public void run() {
                    vibration.vibrate(1000);
                    //time++;
                    handler.postDelayed(this,2000);
                }
            };
            handler.post(runnableCode);
        }
    }

    private void ToastMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    SensorEventListener proximitySensorEventListener
            = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
        @Override
        public void onSensorChanged(SensorEvent event) {

            //mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            //mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.sneyl_audio);
            //mMediaPlayer.setOnCompletionListener(mCompletionListener);

            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    //close
                    textView.setText("Runnin");
                    PROXIMITY=true;
                    runnableCode = new Runnable() {
                        @Override
                        public void run() {
                            vibration.vibrate(200);
                            //time++;
                            handler.postDelayed(this,300);
                        }
                    };
                    handler.post(runnableCode);
                    // Start the audio file
                    //mMediaPlayer.reset();
                    //mMediaPlayer.start();
                    imageView.setImageResource(R.drawable.snail_running);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    //far away
                    textView.setText("Chillin");
                    imageView.setImageResource(R.drawable.snail_standing);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    PROXIMITY=false;
                    handler.removeCallbacks(runnableCode);
                    //mMediaPlayer.reset();

                }
            }
        }
    };
}
