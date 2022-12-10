package com.example.crosswordapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.crosswordapplication.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private ActivityMainBinding binding;
    private TextToSpeech mTTS;

    ImageButton button;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if(i == TextToSpeech.SUCCESS){
                    Locale locale = new Locale("ru");

                    int result = mTTS.setLanguage(locale);
                    //int result = mTTS.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Извините, этот язык не поддерживается");
                        Toast.makeText(getApplicationContext(), "Извините, этот язык не поддерживается", Toast.LENGTH_SHORT).show();
                    } else {
                        button.setEnabled(true);
                    }
                }
            }
        });

        button = binding.button;

        button.setOnClickListener(v -> {
            speakSMTH("Привет Мир");
        });



        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)<audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
        }


    }
    private void speakSMTH(String s){
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)<audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);
        }
        mTTS.speak(s, TextToSpeech.QUEUE_FLUSH,null,null);
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