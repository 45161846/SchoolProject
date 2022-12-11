package com.example.crosswordapplication.DrawingPackage;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crosswordapplication.R;
import com.example.crosswordapplication.databinding.VoiceActivityBinding;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceActivity extends AppCompatActivity {
    private VoiceActivityBinding binding;

    ImageButton next;
    ImageButton previous;
    ImageButton stopSound;
    TextView textRecognised;


    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizeIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = VoiceActivityBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());


        next = binding.nextWordBtn;
        previous = binding.previousWordBtn;
        stopSound = binding.stopSoundBtn;
        textRecognised = binding.textRecognised;


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizeIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechRecognizer.startListening(speechRecognizeIntent);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizer.startListening(speechRecognizeIntent);
            }

            @Override
            public void onError(int i) {
                speechRecognizer.startListening(speechRecognizeIntent);
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                textRecognised.setText(data.get(0));
                Log.d("speechRecognizer", "started");
                speechRecognizer.startListening(speechRecognizeIntent);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }


    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
