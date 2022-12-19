package com.example.crosswordapplication.CrosswordPackage;

import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crosswordapplication.DrawingPackage.RoomDB;
import com.example.crosswordapplication.DrawingPackage.SingleWord;
import com.example.crosswordapplication.databinding.ActivityCroswordBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CrosswordActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private TextToSpeech mTTS;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizeIntent;

    boolean firstVolumeChange = true;
    private boolean speechRunning = false;
    private boolean isCurrentMine = false;
    private boolean currentOrientation = true;
    private boolean otgadalPred = false;
    private String currentAnswer = "";

    ActivityCroswordBinding binding;
    RoomDB roomDB;


    ImageButton nextHorizontal;
    ImageButton nextVertical;
    ImageButton previousHorizontal;
    ImageButton previousVertical;
    ImageButton mute;

    int currentH = 0;
    int currentV = 0;
    List<SingleWord> horizontalWords = new ArrayList<>();
    List<SingleWord> verticalWords = new ArrayList<>();
    private int attempt = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCroswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        roomDB = RoomDB.getInstance(this);
        Intent intent = getIntent();

        int number = intent.getIntExtra("number", 0);

        List<SingleWord> words = roomDB.mainDao().getFromCrossword(number);

        for (SingleWord s : words) {
            if (s.isOrientation()) {
                horizontalWords.add(s);
            } else {
                verticalWords.add(s);
            }
        }
        currentAnswer = horizontalWords.get(0).answer;


        nextHorizontal = binding.nextHorizontalWordBtn;
        nextVertical = binding.nextVerticalWordBtn;
        previousHorizontal = binding.previousHorizontalWordBtn;
        previousVertical = binding.previousVerticalWordBtn;
        mute = binding.stopSoundBtn;
//
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

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
                    doRecognizedCommand(data.get(0));
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
//
    mTTS = new TextToSpeech(this, i -> {

            if(i == TextToSpeech.SUCCESS){
                ttsInitialized();
            }
        });
//
        nextHorizontal.setOnClickListener(v -> {
            currentH += 1;
            if(currentH>horizontalWords.size()-1){
                currentH-=1;
                speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
            }else {
                readHorizontalWordPlus(false);
            }
        });
        nextVertical.setOnClickListener(v -> {
            currentV += 1;
            if(currentV>verticalWords.size()-1){
                currentV-=1;
                speakSMTH("Больше нет слов по вертикали, может попробуете предыдущие?");
            }else {

                readVerticalWordPlus(false);
            }
        });
        previousVertical.setOnClickListener(v -> {
            currentV -= 1;
            if(0>currentV){
                currentV+=1;
                speakSMTH("Больше нет слов по вертикали, может попробуете другие?");
            }else {
                readVerticalWordMinus(false);
            }
        });
        previousHorizontal.setOnClickListener(v -> {
            currentH -= 1;
            if(0>currentH){
                currentH+=1;
                speakSMTH("Больше нет слов по горизонтали, может попробуете другие?");
            }else {
                readHorizontalWordMinus(false);
            }
        });
        mute.setOnClickListener(v -> {
            mTTS.stop();
            speechRunning = false;
        });
        readHorizontalWordPlus(false);
    }

    private void doRecognizedCommand(String s) {
        s=s.toLowerCase().replaceAll(" ","");

        if(s.equals(currentAnswer)){
            readCorrect();
            otgadalPred = true;
            SingleWord singleWord;
            if(currentOrientation) {
                singleWord = horizontalWords.get(currentH);
            }else{
                singleWord = verticalWords.get(currentV);
            }
            singleWord.setSolved(true);
            Log.d("myTag",singleWord.getAnswer()+" "+singleWord.getNumberOfWord());
            roomDB.mainDao().solved(singleWord.getID(),true);
            ArrayList<int[]> array = singleWord.getCrosses();
            for(int[] a:array){
                List<String> letters = roomDB.mainDao().getParticularSolvedLettersFromDB(a[0],!singleWord.isOrientation());
                if(letters.size()>0) {
                    String letter = letters.get(0);
                    SingleWord sw = roomDB.mainDao().getByNumber(a[0], !singleWord.isOrientation());
                    letter = letter.substring(0, a[1]) + sw.answer.charAt(a[1]) + letter.substring(a[1] + 1);
                    roomDB.mainDao().updateSolvedLettersByNumber(a[0], !singleWord.isOrientation(), letter);
                    sw.setSolvedLetters(letter);
                    Log.e("myTag", letter + sw.answer);
                }else{
                    Log.d("myTag","smth go wrong: no such word crossed");
                }
            }

            attempt = 0;
            if(currentOrientation){
                currentH += 1;
                if(currentH>horizontalWords.size()-1){
                    currentH-=1;
                    speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
                }else {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> readHorizontalWordPlus(false), 1000);
                }


            }else {
                currentV += 1;
                if(currentV>verticalWords.size()-1){
                    currentV-=1;
                    speakSMTH("Больше нет слов по вертикали, может попробуете следующие?");
                }else {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> readVerticalWordPlus(false), 1000);
                }

            }
        }else{
            readNo(s);

            attempt+=1;
            if(attempt>=3){
                Handler handler = new Handler();
                handler.postDelayed(() -> speakSMTH("Давайте попробуем что-то другое, а если я не понимаю ваше слово, произнесите его по буквам"), 1000);
                attempt = 0;
            }
        }
    }

    private String sayLetter(char charAt) {

        switch (charAt){
            case 'а':
                return "аа";
            case 'б':
                return "бэ";
            case 'в':
                return "вэ";
            case 'г':
                return "гэ";
            case 'д':
                return "дэ";
            case 'е':
                return "е";
            case 'ё':
                return "ё";
            case 'ж':
                return "жэ";
            case 'з':
                return "зэ";
            case 'и':
                return "и";
            case 'й':
                return "и краткая";
            case 'к':
                return "ка";
            case 'л':
                return "эль";
            case 'м':
                return "эм";
            case 'н':
                return "эн";
            case 'о':
                return "о";
            case 'п':
                return "пэ";
            case 'р':
                return "эр";
            case 'с':
                return "эс";
            case 'т':
                return "тэ";
            case 'у':
                return "у";
            case 'ф':
                return "фэ";
            case 'х':
                return "ха";
            case 'ц':
                return "цэ";
            case 'ч':
                return "чэ";
            case 'ш':
                return "ша";
            case 'щ':
                return "ща";
            case 'ъ':
                return "твёрдый знак";
            case 'ы':
                return "ыы";
            case 'ь':
                return "мягкий знак";
            case 'э':
                return "ээ";
            case 'ю':
                return "Ю";
            case 'я':
                return "Я";
            case '1':
                return "первая";
            case '2':
                return "вторая";
            case '3':
                return "третья";
            case '4':
                return "четвёртая";
            case '5':
                return "пятая";
            case '6':
                return "шестая";
            case '7':
                return "седьмая";
            case '8':
                return "восьмая";
            case '9':
                return "девятая";

        }
        return String.valueOf(charAt);
    }

    private void readNo(String s) {
        speakSMTH("Нет, не "+s);
    }

    private void readCorrect() {
        speakSMTH("верно!");
    }

    private void ttsInitialized() {
        Locale locale = new Locale("ru");

        int result = mTTS.setLanguage(locale);
        mTTS.setSpeechRate(0.7f);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Извините, этот язык не поддерживается");
            Toast.makeText(getApplicationContext(), "Извините, этот язык не поддерживается", Toast.LENGTH_SHORT).show();
        } else {
            nextVertical.setEnabled(true);
            nextHorizontal.setEnabled(true);
            previousVertical.setEnabled(true);
            previousHorizontal.setEnabled(true);
            mute.setEnabled(true);
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
        speechRunning = true;
        if(firstVolumeChange) {
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
            }
            firstVolumeChange = false;
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> mTTS.speak(s, TextToSpeech.QUEUE_FLUSH,null,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID),30);


    }

    private void readVerticalWordPlus(boolean f2) {
        currentOrientation = false;
        SingleWord s = verticalWords.get(currentV);
        boolean f1= false;
        if(s.isSolved()){
            s = verticalWords.get(currentV);
            currentV+=1;
            if(!otgadalPred) {
                speakSMTH("Вы уже отгадали это слово");
                otgadalPred = true;
            }
            if(currentV>verticalWords.size()-1){
                currentV = 0;
                f1=true;
            }
            if(f1){
                if(f2){
                    speakSMTH("Вы уже отгадали все слова в кроссворде");

                }else {
                    speakSMTH("Вы уже отгадали все слова по горизонтали");
                    readHorizontalWordPlus(true);

                }
            }

        }
        if(!f1) {
            currentAnswer = s.answer;
            String text = "";
            text += s.getNumberOfWord() + "-ое по вертикали. " + s.task + " " + s.answer.length() + " буквы.";
            List<String> strings = roomDB.mainDao().getParticularSolvedLettersFromDB(s.getID());
            String str = strings.get(0);
            Log.d("myTag", str);
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '*') {
                    text += " " + sayLetter(Integer.toString(i + 1).charAt(0)) + " " + s.getAnswer().charAt(i);
                }
            }
            speakSMTH(text);
        }
    }
    private void readVerticalWordMinus(boolean f2) {
        currentOrientation = false;
        SingleWord s = verticalWords.get(currentV);
        boolean f1 = false;
        if (s.isSolved()){

            s = verticalWords.get(currentV);
            currentV-=1;
            if(!otgadalPred) {
                speakSMTH("Вы уже отгадали это слово");
                otgadalPred = true;
            }
            if(currentV<0){
                currentV = verticalWords.size()-1;
                f1=true;
            }

            if(f1){
                if(f2){
                    speakSMTH("Вы уже отгадали все слова в кроссворде");

                }else {
                    speakSMTH("Вы уже отгадали все слова по вертикали");
                    readHorizontalWordMinus(true);

                }
            }

        }
        if(!f1) {
            otgadalPred = false;
            currentAnswer = s.answer;
            String text = "";
            text += s.getNumberOfWord() + "-ое по вертикали. " + s.task + " " + s.answer.length() + " буквы.";
            List<String> strings = roomDB.mainDao().getParticularSolvedLettersFromDB(s.getID());
            String str = strings.get(0);
            Log.d("myTag", str);
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '*') {
                    text += " " + (i + 1) + "-ая " + s.getAnswer().charAt(i);
                }
            }

            speakSMTH(text);
        }
    }

    private void readHorizontalWordPlus(boolean f2) {
        currentOrientation = true;
        SingleWord s = horizontalWords.get(currentH);
        boolean f1 = false;
        if(s.isSolved()){

            s = horizontalWords.get(currentH);
            currentH+=1;
            if(!otgadalPred) {
                speakSMTH("Вы уже отгадали это слово");
                otgadalPred = true;
            }
            if(currentH>horizontalWords.size()-1){
                currentH = 0;
                f1=true;
            }
            if(f1){
                if(f2){
                    speakSMTH("Вы уже отгадали все слова в кроссворде");

                }else {
                    speakSMTH("Вы уже отгадали все слова по горизонтали");
                    readVerticalWordPlus(true);

                }
            }
        }
        if(!f1) {
            otgadalPred = false;
            currentAnswer = s.answer;
            String text = "";
            text += s.getNumberOfWord() + "-ое по горизонтали. " + s.task + " " + s.answer.length() + " буквы.";
            List<String> strings = roomDB.mainDao().getParticularSolvedLettersFromDB(s.getID());
            String str = strings.get(0);
            Log.d("myTag", str);
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '*') {
                    text += " " + (i + 1) + "-ая " + s.getAnswer().charAt(i);
                }
            }
            speakSMTH(text);
        }

    }
    private void readHorizontalWordMinus(boolean f2) {
        currentOrientation = true;
        SingleWord s = horizontalWords.get(currentH);
        boolean f1 = false;
        if(s.isSolved()){
            s = horizontalWords.get(currentH);
            currentH-=1;
            if(!otgadalPred) {
                speakSMTH("Вы уже отгадали это слово");
                otgadalPred = true;
            }
            if(currentH<0){
                currentH = horizontalWords.size()-1;
                f1=true;
            }

            if(f1){
                if(f2){
                    speakSMTH("Вы уже отгадали все слова в кроссворде");

                }else {
                    speakSMTH("Вы уже отгадали все слова по горизонтали");
                    readVerticalWordMinus(true);

                }
            }
        }
        if(!f1) {
            otgadalPred = false;
            currentAnswer = s.answer;
            String text = "";
            text += s.getNumberOfWord() + "-ое по горизонтали. " + s.task + " " + s.answer.length() + " буквы.";
            List<String> strings = roomDB.mainDao().getParticularSolvedLettersFromDB(s.getID());
            String str = strings.get(0);
            Log.d("myTag", str);
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '*') {
                    text += " " + (i + 1) + "-ая " + s.getAnswer().charAt(i);
                }
            }
            speakSMTH(text);
        }

    }



}
