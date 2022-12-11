package com.example.crosswordapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.crosswordapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private ActivityMainBinding binding;
    private TextToSpeech mTTS;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizeIntent;


    boolean firstVolumeChange = true;

    private boolean speechRunning = false;
    private boolean isCurrentMine = false;

    ImageButton button;
    ImageButton stopSound;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "AUDIO GRANTED", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);   }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTTS = new TextToSpeech(this, i -> {

            if(i == TextToSpeech.SUCCESS){
                ttsInitialized();
            }
        });

        button = binding.button;

        stopSound = binding.stopSoundBtn;

        button.setOnClickListener(v -> {
            speechRecognizer.stopListening();
            speechRunning = true;
            Handler handler = new Handler();
            handler.postDelayed(() -> speakSMTH(binding.editText.getText().toString()), 50);


        });

        stopSound.setOnClickListener(v -> {
            mTTS.stop();
            speechRunning = false;

        });


        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)<audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizeIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizer.startListening(speechRecognizeIntent);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            }

            @Override
            public void onBeginningOfSpeech() {
                if(speechRunning) {
                    isCurrentMine = true;
                }
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
            }


            @Override
            public void onError(int i) {
                speechRecognizer.startListening(speechRecognizeIntent);
                }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(!isCurrentMine) {
                    binding.recognizedTextView.setText(data.get(0));
                }else{
                    isCurrentMine = false;
                    speechRunning = false;
                }
                Log.d("speechRecognizer", "started");
                speechRecognizer.startListening(speechRecognizeIntent);
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    private void ttsInitialized() {
        Locale locale = new Locale("ru");

        int result = mTTS.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Извините, этот язык не поддерживается");
            Toast.makeText(getApplicationContext(), "Извините, этот язык не поддерживается", Toast.LENGTH_SHORT).show();
        } else {
            button.setEnabled(true);
        }

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                speechRunning = false;
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

    }

    private void speakSMTH(String s){
        if(firstVolumeChange) {
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
            }
            firstVolumeChange = false;
        }

        mTTS.speak(s, TextToSpeech.QUEUE_FLUSH,null,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);

    }
    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        binding = null;
        super.onDestroy();
    }
}