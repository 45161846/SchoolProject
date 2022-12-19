package com.example.crosswordapplication;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.crosswordapplication.CrosswordPackage.CrosswordActivity;
import com.example.crosswordapplication.DrawingPackage.RoomDB;
import com.example.crosswordapplication.DrawingPackage.SingleWord;
import com.example.crosswordapplication.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private ActivityMainBinding binding;
    private TextToSpeech mTTS;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizeIntent;

    private RoomDB roomDB;

    private final int crosswordsNumber = 1;
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        roomDB = RoomDB.getInstance(this);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("main",MODE_PRIVATE);
        boolean firstStart = sharedPreferences.getBoolean("firstStart", true);
        if(firstStart){
            addAllCrosswords();
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            prefEditor.putBoolean("firstStart",false).apply();
            Toast.makeText(this,"first",Toast.LENGTH_SHORT).show();


        }else{
            Toast.makeText(this,"not first",Toast.LENGTH_SHORT).show();
        }

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

    }

    private void doRecognizedCommand(String s) {

        s=s.toLowerCase();
        String[] parts = s.split(" ");
        if(s.contains("открой") && s.contains("последний")){
            openLastCrossword();
        }else if(s.contains("открой") && s.contains("кроссворд")){
            boolean flag = true;
            for(String st:parts){
                try {
                    int a = Integer.parseInt(parts[parts.length - 1]);
                    break;
                }catch (NumberFormatException e){
                    flag = false;
                }
            }
            if(flag){
                openCrossword(Integer.parseInt(parts[parts.length - 1]));
            }else{
                speakSMTH("Укажите номер кроссворда");
            }

        }else if(s.contains("прочитай") && s.contains("список")){
            readListOfCrosswords();
        }



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
    private void openLastCrossword() {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("main",MODE_PRIVATE);
        int lastCrossword = sharedPreferences.getInt("last",0);
        if (lastCrossword == 0){
            speakSMTH("Похоже, что вы ещё не решали кроссворды");
        }else{
            openCrossword(lastCrossword);
        }

    }
    private void openCrossword(int parseInt) {
        Intent myIntent = new Intent(MainActivity.this, CrosswordActivity.class);
        myIntent.putExtra("number", parseInt);
        MainActivity.this.startActivity(myIntent);

    }

    private void addAllCrosswords() {
        addCrossword1();
    }
    private void addCrossword1() {
        ArrayList<int[]> arrayList1 = new ArrayList<>();
        arrayList1.add(new int[]{2, 0, 0});
        arrayList1.add(new int[]{3,0,2});

        SingleWord s1 = new SingleWord(1,2,"Хитрый зверь."
                , "лис", 3, 9, true, arrayList1,"***", false);


        ArrayList<int[]> arrayList2 = new ArrayList<>();
        arrayList2.add(new int[]{4,0,0});
        arrayList2.add(new int[]{5,0,1});
        SingleWord s2 = new SingleWord(1,4,"Поросль на лице.",
                "усы",7,9,true,arrayList2,"***", false);


        ArrayList<int[]> arrayList3 = new ArrayList<>();
        arrayList3.add(new int[]{1,1,0});
        arrayList3.add(new int[]{7,0,1});
        arrayList3.add(new int[]{2,1,2});
        SingleWord s3 = new SingleWord(1,6,"Торжественное стихотворение"
                , "ода", 0, 0, true, arrayList3,"***", false);


        ArrayList<int[]> arrayList4 = new ArrayList<>();
        arrayList4.add(new int[]{3,1,0});
        arrayList4.add(new int[]{9,0,1});
        arrayList4.add(new int[]{4,1,2});
        arrayList4.add(new int[]{5,1,3});
        SingleWord s4 = new SingleWord(1,8,"И рыбья, и овощная.",
                "икра",3,1,true,arrayList4,"****", false);


        ArrayList<int[]> arrayList5 = new ArrayList<>();
        arrayList5.add(new int[]{1,2,0});
        arrayList5.add(new int[]{7,1,1});
        arrayList5.add(new int[]{2,2,2});
        arrayList5.add(new int[]{11,0,3});
        SingleWord s5 = new SingleWord(1,10,"Верховный бог у древних греков."
                , "зевс", 3, 9, true, arrayList5, "****",false);


        ArrayList<int[]> arrayList6 = new ArrayList<>();
        arrayList6.add(new int[]{12,1,0});
        arrayList6.add(new int[]{4,2,1});
        arrayList6.add(new int[]{5,2,2});
        arrayList6.add(new int[]{13,0,3});
        SingleWord s6 = new SingleWord(1,12,"Парламент Украины.",
                "рада",7,9,true,arrayList6,"****", false);

        ArrayList<int[]> arrayList7 = new ArrayList<>();
        arrayList7.add(new int[]{7,2,0});
        arrayList7.add(new int[]{2,3,1});
        arrayList7.add(new int[]{11,1,2});
        arrayList7.add(new int[]{9,2,4});
        arrayList7.add(new int[]{4,3,5});
        SingleWord s7 = new SingleWord(1,14,"Взрывчатка."
                , "тротил", 0, 0, true, arrayList7, "******",false);


        ArrayList<int[]> arrayList8 = new ArrayList<>();
        arrayList8.add(new int[]{15,0,0});
        arrayList8.add(new int[]{7,3,1});
        SingleWord s8 = new SingleWord(1,15,"Суп с капустой.",
                "щи",3,1,true,arrayList8,"**",false);

        ArrayList<int[]> arrayList9 = new ArrayList<>();
        arrayList9.add(new int[]{16,0,0});
        arrayList9.add(new int[]{13,2,1});
        SingleWord s9 = new SingleWord(1,16,"Имя Мопассана."
                , "ги", 3, 9, true, arrayList9, "**",false);


        ArrayList<int[]> arrayList10 = new ArrayList<>();
        arrayList10.add(new int[]{17,0,0});
        arrayList10.add(new int[]{11,3,1});
        arrayList10.add(new int[]{9,4,3});
        arrayList10.add(new int[]{18,0,4});
        arrayList10.add(new int[]{16,1,5});
        SingleWord s10 = new SingleWord(1,17,"Вид художественной литературы.",
                "сатира",7,9,true,arrayList10, "******",false);

        ArrayList<int[]> arrayList11 = new ArrayList<>();
        arrayList11.add(new int[]{15,2,0});
        arrayList11.add(new int[]{20,0,1});
        arrayList11.add(new int[]{17,1,2});
        arrayList11.add(new int[]{11,4,3});
        SingleWord s11 = new SingleWord(1,19,"Звено гусеницы."
                , "трак", 0, 0, true, arrayList11, "****",false);


        ArrayList<int[]> arrayList12 = new ArrayList<>();
        arrayList12.add(new int[]{9,5,0});
        arrayList12.add(new int[]{18,1,1});
        arrayList12.add(new int[]{16,2,2});
        arrayList12.add(new int[]{22,0,3});
        SingleWord s12 = new SingleWord(1,21,"Летняя забота владельца гужевого транспорта.",
                "сани",3,1,true,arrayList12, "****",false);


        ArrayList<int[]> arrayList13 = new ArrayList<>();
        arrayList13.add(new int[]{20,1,0});
        arrayList13.add(new int[]{17,2,1});
        arrayList13.add(new int[]{11,5,2});
        arrayList13.add(new int[]{24,0,3});
        SingleWord s13 = new SingleWord(1,23,"Минерал, разновидность кварца."
                , "агат", 3, 9, true, arrayList13, "****",false);


        ArrayList<int[]> arrayList14 = new ArrayList<>();
        arrayList14.add(new int[]{18,2,0});
        arrayList14.add(new int[]{16,3,1});
        arrayList14.add(new int[]{22,1,2});
        SingleWord s14 = new SingleWord(1,25,"Великий вождь китайского народа.",
                "мао",7,9,true,arrayList14, "***",false);

        ArrayList<int[]> arrayList15 = new ArrayList<>();
        arrayList15.add(new int[]{20,2,1});
        arrayList15.add(new int[]{17,3,2});
        SingleWord s15 = new SingleWord(1,26,"Крупнейший приток Волги."
                , "ока", 0, 0, true, arrayList15, "***",false);


        ArrayList<int[]> arrayList16 = new ArrayList<>();
        arrayList16.add(new int[]{24,1,0});
        arrayList16.add(new int[]{18,3,2});
        SingleWord s16 = new SingleWord(1,27,"«Банзай» по-русски.",
                "ура",3,1,true,arrayList16, "***",false);

//        Колесная повозка или сани с кладью.
//        2. Дерево с листьями и для венка, и для борща.
//        3. Нота.
//        4. Горный массив в России.
//        5. Участок земли, засаженный деревьями, кустами, цветами.
//
        ArrayList<int[]> arrayList17 = new ArrayList<>();
        arrayList17.add(new int[]{6,0,1});
        arrayList17.add(new int[]{10,0,2});
        SingleWord s17 = new SingleWord(1,1,"Колесная повозка или сани с кладью."
                , "воз", 3, 9, false, arrayList17, "***",false);


        ArrayList<int[]> arrayList18 = new ArrayList<>();
        arrayList18.add(new int[]{2,0,0});
        arrayList18.add(new int[]{6,2,1});
        arrayList18.add(new int[]{10,2,2});
        arrayList18.add(new int[]{14,1,3});
        SingleWord s18 = new SingleWord(1,2,"Дерево с листьями и для венка, и для борща.",
                "лавр",7,9,false,arrayList18, "****",false);

        ArrayList<int[]> arrayList19 = new ArrayList<>();
        arrayList19.add(new int[]{2,2,0});
        arrayList19.add(new int[]{8,0,1});
        SingleWord s19 = new SingleWord(1,3,"Нота."
                , "си", 0, 0, false, arrayList19, "**",false);


        ArrayList<int[]> arrayList20 = new ArrayList<>();
        arrayList20.add(new int[]{4,0,0});
        arrayList20.add(new int[]{8,2,1});
        arrayList20.add(new int[]{12,1,2});
        arrayList20.add(new int[]{14,5,3});
        SingleWord s20= new SingleWord(1,4,"Горный массив в России.",
                "урал",3,1,false,arrayList20, "****",false);


        ArrayList<int[]> arrayList21 = new ArrayList<>();
        arrayList21.add(new int[]{4,1,0});
        arrayList21.add(new int[]{8,3,1});
        arrayList21.add(new int[]{12,2,2});
        SingleWord s21 = new SingleWord(1,5,"Участок земли, засаженный деревьями, кустами, цветами."
                , "сад", 3, 9, false, arrayList21, "***",false);


//        7. Цветы жизни.
//        9. Переломный момент в болезни.
//        11. Домашнее животное.
//        13. Мусульманское имя Кассиуса Клея.
//        15. Средство обороны от холодного оружия.
//        16. Государство в Западной Африке.
        ArrayList<int[]> arrayList22 = new ArrayList<>();
        arrayList22.add(new int[]{6,1,0});
        arrayList22.add(new int[]{10,1,1});
        arrayList22.add(new int[]{14,0,2});
        arrayList22.add(new int[]{15,1,3});
        SingleWord s22 = new SingleWord(1,7,"Цветы жизни.",
                "дети",7,9,false,arrayList22,"****",false);

        ArrayList<int[]> arrayList23 = new ArrayList<>();
        arrayList23.add(new int[]{8,1,0});
        arrayList23.add(new int[]{12,0,1});
        arrayList23.add(new int[]{14,4,2});
        arrayList23.add(new int[]{17,3,4});
        arrayList23.add(new int[]{21,0,5});
        SingleWord s23 = new SingleWord(1,9,"Переломный момент в болезни."
                , "кризис", 0, 0, false, arrayList23, "******",false);


        ArrayList<int[]> arrayList24 = new ArrayList<>();
        arrayList24.add(new int[]{10,3,0});
        arrayList24.add(new int[]{14,2,1});
        arrayList24.add(new int[]{17,1,3});
        arrayList24.add(new int[]{19,3,4});
        arrayList24.add(new int[]{23,2,5});
        SingleWord s24 = new SingleWord(1,11,"Домашнее животное.",
                "собака",3,1,false,arrayList24, "******",false);

        ArrayList<int[]> arrayList25 = new ArrayList<>();
        arrayList25.add(new int[]{12,3,0});
        arrayList25.add(new int[]{16,1,2});
        SingleWord s25 = new SingleWord(1,13,"Мусульманское имя Кассиуса Клея."
                , "али", 3, 9, false, arrayList25, "***",false);


        ArrayList<int[]> arrayList26 = new ArrayList<>();
        arrayList26.add(new int[]{15,0,0});
        arrayList26.add(new int[]{19,0,2});
        SingleWord s26 = new SingleWord(1,15,"Средство обороны от холодного оружия.",
                "щит",7,9,false,arrayList26, "***",false);

        ArrayList<int[]> arrayList27 = new ArrayList<>();
        arrayList27.add(new int[]{16,0,0});
        arrayList27.add(new int[]{17,4,1});
        arrayList27.add(new int[]{21,2,2});
        arrayList27.add(new int[]{25,1,3});
        SingleWord s27 = new SingleWord(1,16,"Государство в Западной Африке."
                , "гана", 0, 0, false, arrayList27, "****",false);


//        17. Древнескандинавское народное сказание.
//        18. Она украшает картину.
//        20. Зодиакальное созвездие.
//        22. Электрически заряженная частица.
//        24. Марка, тип российских самолетов.
        ArrayList<int[]> arrayList28 = new ArrayList<>();
        arrayList28.add(new int[]{17,0,0});
        arrayList28.add(new int[]{19,2,1});
        arrayList28.add(new int[]{23,1,2});
        arrayList28.add(new int[]{26,2,3});
        SingleWord s28 = new SingleWord(1,17,"Древнескандинавское народное сказание.",
                "сага",3,1,false,arrayList28,"****", false);


        ArrayList<int[]> arrayList29 = new ArrayList<>();
        arrayList29.add(new int[]{17,4,0});
        arrayList29.add(new int[]{21,1,1});
        arrayList29.add(new int[]{25,0,2});
        arrayList29.add(new int[]{27,2,3});
        SingleWord s29 = new SingleWord(1,29,"Она украшает картину."
                , "рама", 3, 9, false, arrayList29, "****",false);


        ArrayList<int[]> arrayList30 = new ArrayList<>();
        arrayList30.add(new int[]{19,1,0});
        arrayList30.add(new int[]{23,0,1});
        arrayList30.add(new int[]{26,1,2});
        SingleWord s30 = new SingleWord(1,20,"Зодиакальное созвездие.",
                "рак",7,9,false,arrayList30, "***",false);

        ArrayList<int[]> arrayList31 = new ArrayList<>();
        arrayList31.add(new int[]{21,3,0});
        arrayList31.add(new int[]{25,2,1});
        SingleWord s31 = new SingleWord(1,22,"Электрически заряженная частица."
                , "ион", 0, 0, false, arrayList31, "***",false);


        ArrayList<int[]> arrayList32 = new ArrayList<>();
        arrayList32.add(new int[]{23,3,0});
        arrayList32.add(new int[]{27,0,1});
        SingleWord s32 = new SingleWord(1,24,"Марка, тип российских самолетов.",
                "ту",3,1,false,arrayList32, "**",false);

        roomDB.mainDao().insert(s1);
        roomDB.mainDao().insert(s2);
        roomDB.mainDao().insert(s3);
        roomDB.mainDao().insert(s4);
        roomDB.mainDao().insert(s5);
        roomDB.mainDao().insert(s6);
        roomDB.mainDao().insert(s7);
        roomDB.mainDao().insert(s8);
        roomDB.mainDao().insert(s9);
        roomDB.mainDao().insert(s10);
        roomDB.mainDao().insert(s11);
        roomDB.mainDao().insert(s12);
        roomDB.mainDao().insert(s13);
        roomDB.mainDao().insert(s14);
        roomDB.mainDao().insert(s15);
        roomDB.mainDao().insert(s16);
        roomDB.mainDao().insert(s17);
        roomDB.mainDao().insert(s18);
        roomDB.mainDao().insert(s19);
        roomDB.mainDao().insert(s20);
        roomDB.mainDao().insert(s21);
        roomDB.mainDao().insert(s22);
        roomDB.mainDao().insert(s23);
        roomDB.mainDao().insert(s24);
        roomDB.mainDao().insert(s25);
        roomDB.mainDao().insert(s26);
        roomDB.mainDao().insert(s27);
        roomDB.mainDao().insert(s28);
        roomDB.mainDao().insert(s29);
        roomDB.mainDao().insert(s30);
        roomDB.mainDao().insert(s31);
        roomDB.mainDao().insert(s32);
    }

    public void readInstruction(){
        speakSMTH("я прочитала инструкцию");
    }
    public void readListOfCrosswords(){
        speakSMTH("я прочитала список кроссвордов");
    }

}