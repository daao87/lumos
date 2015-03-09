package com.h4ackademy.hp.lumos;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera.Parameters;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private Camera cam;
    private Parameters p;
    private boolean isFlashOn;

    private SpeechRecognizer sr;
    private Intent intent;
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new Listener());

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

    }

    public void turnOnFlash(){
        if(!isFlashOn) {
            cam = Camera.open();
            p = cam.getParameters();
            p.setFlashMode(Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
            isFlashOn = true;
        }
    }

    public void turnOffFlash(){
        if(isFlashOn) {
            if (cam == null) {
                cam = Camera.open();
            }
            cam.stopPreview();
            cam.release();
            isFlashOn = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppStatus.activityResumed();
        sr.startListening(intent);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    @Override
    protected void onPause() {
        AppStatus.activityPaused();
        sr.stopListening();
        super.onPause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
            }
        }, 300);

    }

    @Override
    public void onDestroy() {
        if(sr != null) {
            sr.cancel();
            sr.destroy();
            sr = null;
        }
        turnOffFlash();
        super.onDestroy();
    }

    class Listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
        }
        public void onBeginningOfSpeech() {
        }
        public void onRmsChanged(float rmsdB) {
        }
        public void onBufferReceived(byte[] buffer) {
        }
        public void onEndOfSpeech() {
        }
        public void onError(int error) {
            if(AppStatus.isActivityVisible()) {
                sr.startListening(intent);
            }
        }
        public void onResults(Bundle results) {
            String str = new String();
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                str += data.get(i);
            }
            String strings = str.toLowerCase();
            if(strings.contains("lumos") || strings.contains("loomis") || strings.contains("luminous")
                    || strings.contains("numerous") || strings.contains("animals") || strings.contains("louis")
                    || strings.contains("nomas") || strings.contains("numbers") || strings.contains("lomas")) {
                turnOnFlash();
            }
            if(strings.contains("knox") || strings.contains("nox") || strings.contains("knocks")) {
                turnOffFlash();
            }
            if(AppStatus.isActivityVisible()) {
                sr.startListening(intent);
            }
        }
        public void onPartialResults(Bundle partialResults) {
        }
        public void onEvent(int eventType, Bundle params) {
        }
    }

    static class AppStatus {
        private static boolean activityVisible;

        public static boolean isActivityVisible() {
            return activityVisible;
        }

        public static void activityResumed() {
            activityVisible = true;
        }

        public static void activityPaused() {
            activityVisible = false;
        }
    }
}
