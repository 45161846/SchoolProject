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
import com.example.crosswordapplication.MainActivity;
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
    private int direction = 1; //0 - up, 1 - right, 2 - down, 3 - left
    private int solvedHorizontalCount = 0;
    private int solvedVerticalCount = 0;
    private float speechRate = 0.7f;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCroswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        roomDB = RoomDB.getInstance(this);
        Intent intent = getIntent();

        int number = intent.getIntExtra("number", 0);
        speechRate = intent.getFloatExtra("speechRate", 0.7f);

        List<SingleWord> words = roomDB.mainDao().getFromCrossword(number);

        for (SingleWord s : words) {
            if (s.isOrientation()) {
                horizontalWords.add(s);
                if (s.isSolved) {
                    solvedHorizontalCount += 1;
                }
            } else {
                verticalWords.add(s);
                if (s.isSolved) {
                    solvedVerticalCount += 1;
                }
            }
            Log.d("myTag", s.getAnswer() + " " + s.isSolved);
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
                if (speechRunning) {
                    isCurrentMine = true;
                }
            }

            @Override
            public void onRmsChanged(float v) {}

            @Override
            public void onBufferReceived(byte[] bytes) {}

            @Override
            public void onEndOfSpeech() {}


            @Override
            public void onError(int i) {
                speechRecognizer.startListening(speechRecognizeIntent);
            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d("commanda", "onResults(Bundle bundle)");

                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (!isCurrentMine) {
                    doRecognizedCommand(data.get(0));
                } else {
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

            if (i == TextToSpeech.SUCCESS) {
                ttsInitialized();
            }
        });
//
        nextHorizontal.setOnClickListener(v -> {
            currentH += 1;
            direction = 1;
            if (currentH > horizontalWords.size() - 1) {
                currentH -= 1;
                speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
            } else {

                readWord();
            }
        });
        nextVertical.setOnClickListener(v -> {
            currentV += 1;
            direction = 2;
            if (currentV > verticalWords.size() - 1) {
                currentV -= 1;
                speakSMTH("Больше нет слов по вертикали, может попробуете предыдущие?");
            } else {

                readWord();
            }
        });
        previousVertical.setOnClickListener(v -> {
            currentV -= 1;
            direction = 0;
            if (0 > currentV) {
                currentV += 1;
                speakSMTH("Больше нет слов по вертикали, может попробуете другие?");
            } else {
                readWord();
            }
        });
        previousHorizontal.setOnClickListener(v -> {
            currentH -= 1;
            direction = 3;
            if (0 > currentH) {
                currentH += 1;
                speakSMTH("Больше нет слов по горизонтали, может попробуете другие?");
            } else {
                readWord();
            }
        });
        mute.setOnClickListener(v -> {
            mTTS.stop();
            speechRunning = false;
            isCurrentMine = false;
        });
        readWord();
    }

    private void doRecognizedCommand(String s) {
        Log.d("commanda", "doRecognizedCommand(String s)");
        s = s.toLowerCase().replaceAll(" ", "");

        if (s.equals(currentAnswer)) {
            readCorrect();

            SingleWord singleWord;
            if (currentOrientation) {
                singleWord = horizontalWords.get(currentH);
                solvedHorizontalCount += 1;
            } else {
                singleWord = verticalWords.get(currentV);
                solvedVerticalCount += 1;
            }
            singleWord.setSolved(true);
            Log.d("myTag", singleWord.getAnswer() + " " + singleWord.getNumberOfWord());
            roomDB.mainDao().solved(singleWord.getID(), true);
            ArrayList<int[]> array = singleWord.getCrosses();
            for (int[] a : array) {
                List<String> letters = roomDB.mainDao().getParticularSolvedLettersFromDB(a[0], !singleWord.isOrientation());
                if (letters.size() > 0) {
                    String letter = letters.get(0);
                    SingleWord sw = roomDB.mainDao().getByNumber(a[0], !singleWord.isOrientation());
                    letter = letter.substring(0, a[1]) + sw.answer.charAt(a[1]) + letter.substring(a[1] + 1);
                    roomDB.mainDao().updateSolvedLettersByNumber(a[0], !singleWord.isOrientation(), letter);
                    sw.setSolvedLetters(letter);
                    Log.e("myTag", letter + sw.answer);
                } else {
                    Log.d("myTag", "smth go wrong: no such word crossed");
                }
            }

            attempt = 0;
            if (currentOrientation) {
                currentH += 1;
                if (currentH > horizontalWords.size() - 1) {
                    currentH -= 1;
                    speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(this::readWord, 1000);
                }


            } else {
                currentV += 1;
                if (currentV > verticalWords.size() - 1) {
                    currentV -= 1;
                    speakSMTH("Больше нет слов по вертикали, может попробуете следующие?");
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(this::readWord, 1000);
                }

            }
        }
        else if(s.contains("ответ")){
            speakSMTH(currentAnswer);
            SingleWord singleWord;
            if (currentOrientation) {
                singleWord = horizontalWords.get(currentH);
                solvedHorizontalCount += 1;
            } else {
                singleWord = verticalWords.get(currentV);
                solvedVerticalCount += 1;
            }
            singleWord.setSolved(true);
            Log.d("myTag", singleWord.getAnswer() + " " + singleWord.getNumberOfWord());
            roomDB.mainDao().solved(singleWord.getID(), true);
            ArrayList<int[]> array = singleWord.getCrosses();
            for (int[] a : array) {
                List<String> letters = roomDB.mainDao().getParticularSolvedLettersFromDB(a[0], !singleWord.isOrientation());
                if (letters.size() > 0) {
                    String letter = letters.get(0);
                    SingleWord sw = roomDB.mainDao().getByNumber(a[0], !singleWord.isOrientation());
                    letter = letter.substring(0, a[1]) + sw.answer.charAt(a[1]) + letter.substring(a[1] + 1);
                    roomDB.mainDao().updateSolvedLettersByNumber(a[0], !singleWord.isOrientation(), letter);
                    sw.setSolvedLetters(letter);
                    Log.e("myTag", letter + sw.answer);
                } else {
                    Log.d("myTag", "smth go wrong: no such word crossed");
                }
            }
            attempt = 0;
            if (currentOrientation) {
                currentH += 1;
                if (currentH > horizontalWords.size() - 1) {
                    currentH -= 1;
                    speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(this::readWord, 1000);
                }


            } else {
                currentV += 1;
                if (currentV > verticalWords.size() - 1) {
                    currentV -= 1;
                    speakSMTH("Больше нет слов по вертикали, может попробуете следующие?");
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(this::readWord, 1000);
                }

            }
        }else if(s.contains("повтор")){
            readWord();
        }else if(s.contains("вый") && s.contains("игр")){
            Intent myIntent = new Intent(CrosswordActivity.this, MainActivity.class);
            CrosswordActivity.this.startActivity(myIntent);
        }else if(s.contains("команд")){
            String listCommands = "Для того чтобы узнать ответ, скажите ответ. Чтобы закончить игру, " +
                    "скажите (выйти из игры). Для того чтобы перейти к следующему или предыдущему" +
                    " слову, " +
                    "скажите (следующее) или (предыдущее) с указанием направления: по вертикали или горизонтали. " +
                    "  Ещё можно менять мой голос. Скажите (быстрее) или (медленнее), чтобы изменить скорость речи. Скажите (громче) или (тише)" +
                    " чтобы поменять громкость.";
            speakSMTH(listCommands);
        }else if(s.contains("горизонт") || s.contains("вертика")){
            if(s.contains("горизонт") && s.contains("следующ")){
                currentH += 1;
                direction = 1;
                if (currentH > horizontalWords.size() - 1) {
                    currentH -= 1;
                    speakSMTH("Больше нет слов по горизонтали, может попробуете предыдущие?");
                } else {

                    readWord();
                }
            }else if(s.contains("горизонт") && s.contains("предыдущ")){
                currentH -= 1;
                direction = 3;
                if (0 > currentH) {
                    currentH += 1;
                    speakSMTH("Больше нет слов по горизонтали, может попробуете другие?");
                } else {
                    readWord();
                }
            }else if(s.contains("вертика") && s.contains("следующ")){
                currentV += 1;
                direction = 2;
                if (currentV > verticalWords.size() - 1) {
                    currentV -= 1;
                    speakSMTH("Больше нет слов по вертикали, может попробуете предыдущие?");
                } else {

                    readWord();
                }
            }else if(s.contains("вертика") && s.contains("предыдущ")){
                currentV -= 1 ;
                direction = 0;
                if (0 > currentV) {
                    currentV += 1;
                    speakSMTH("Больше нет слов по вертикали, может попробуете другие?");
                } else {
                    readWord();
                }
            }

        }else if(s.contains("медлен")){
            if(speechRate>0.5){
                speechRate-=0.1;
                mTTS.setSpeechRate(speechRate);
            }else{
                speakSMTH("Я не могу медленее");
            }
        }else if(s.contains("быстре")){
            if(speechRate<1.3){
                speechRate+=0.1;
                mTTS.setSpeechRate(speechRate);
            }else{
                speakSMTH("Я не могу быстрее");
            }
        }else if(s.contains("громч")){
            //todo
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int change = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10;

            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)<max-change){
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+change, 0);
            }else {
                speakSMTH("И так уже связки болят, куда еще громче?");
            }
        }else if(s.contains("тише")){
            //todo
            int change = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10;

            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)>change*3){
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)-change, 0);
            }else{
                speakSMTH("Боюсь, что тише меня не будет слышно, но если очень хотите потише, сделайте это с помощью кнопок на телефоне");
            }
        } else{
            readNo(s);

            attempt += 1;
            if (attempt >= 3) {
                Handler handler = new Handler();
                handler.postDelayed(() -> speakSMTH("Давайте попробуем что-то другое, а если я не понимаю ваше слово, произнесите его по буквам"), 1000);
                attempt = 0;
            }
        }
    }

    private String sayLetter(char charAt) {

        switch (charAt) {
            case 'а':
                return " (аа) ";
            case 'б':
                return " (бэ) ";
            case 'в':
                return " (вэ) ";
            case 'г':
                return " (гэ) ";
            case 'д':
                return " (дэ) ";
            case 'е':
                return " (е) ";
            case 'ё':
                return " (ё) ";
            case 'ж':
                return " (жэ) ";
            case 'з':
                return " (зэ) ";
            case 'и':
                return " (и) ";
            case 'й':
                return " (и краткая) ";
            case 'к':
                return " (ка) ";
            case 'л':
                return " (эль) ";
            case 'м':
                return " (эм) ";
            case 'н':
                return " (эн) ";
            case 'о':
                return " (о) ";
            case 'п':
                return " (пэ) ";
            case 'р':
                return " (эр) ";
            case 'с':
                return " (эс) ";
            case 'т':
                return " (тэ) ";
            case 'у':
                return " (у) ";
            case 'ф':
                return " (фэ) ";
            case 'х':
                return " (ха) ";
            case 'ц':
                return " (цэ) ";
            case 'ч':
                return " (чэ) ";
            case 'ш':
                return " (ша) ";
            case 'щ':
                return " (ща) ";
            case 'ъ':
                return " (твёрдый знак) ";
            case 'ы':
                return " (ыы) ";
            case 'ь':
                return " (мягкий знак) ";
            case 'э':
                return " (ээ) ";
            case 'ю':
                return " (Ю) ";
            case 'я':
                return " (Я) ";
            case '1':
                return " (первая) ";
            case '2':
                return " (вторая) ";
            case '3':
                return " (третья) ";
            case '4':
                return " (четвёртая) ";
            case '5':
                return " (пятая) ";
            case '6':
                return " (шестая) ";
            case '7':
                return " (седьмая) ";
            case '8':
                return " (восьмая) ";
            case '9':
                return " (девятая) ";

        }
        return String.valueOf(charAt);
    }

    private void readNo(String s) {
        speakSMTH("Нет, не " + s);
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
            public void onDone(String utteranceId) {
                Handler handler = new Handler();
                handler.postDelayed(() ->{
                    speechRunning = false;
                    isCurrentMine = false;},70);

            }

            @Override
            public void onError(String utteranceId) {
                Handler handler = new Handler();
                handler.postDelayed(() ->{
                    isCurrentMine = false;},70);
                isCurrentMine = false;
            }
        });

    }

    private void speakSMTH(String s) {
        Log.d("commanda", "speakSMTH(String s)");
        speechRunning = true;
        if (firstVolumeChange) {
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
            }
            firstVolumeChange = false;
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> mTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID), 30);


    }

    private void readWord() {
        Log.d("commanda", "readWord()");
        direction %= 4;
        SingleWord s;
        int size = 0;
        int currentNumber = 0;
        if (direction % 2 == 1) { // horizontal direction
            s = horizontalWords.get(currentH);
            currentNumber = currentH;
            size = horizontalWords.size();
            currentOrientation = true;
        } else {
            s = verticalWords.get(currentV);
            currentNumber = currentV;
            size = verticalWords.size();
            currentOrientation = false;
        }

        if (s.isSolved || s.getSolvedLetters().equals(s.answer)) {
            if(s.getSolvedLetters().equals(s.answer)){
                s.setSolved(true);
                roomDB.mainDao().solved(s.getID(), true);
                if (currentOrientation) {
                    solvedHorizontalCount += 1;
                } else {
                    solvedVerticalCount += 1;
                }
            }

            if (solvedHorizontalCount == horizontalWords.size() && solvedVerticalCount == verticalWords.size()) {
                speakSMTH("Поздравляю, вы отгадали весь кроссворд");
                Handler handler = new Handler();
                handler.postDelayed(this::onBackPressed, 1500);
            } else if (direction % 2 == 1 && solvedHorizontalCount == horizontalWords.size()) { // horizontal direction
                speakSMTH("Вы отгадали все слова по горизонтали. Давайте перейдем к вертикальным");
                direction = 2;
                Handler handler = new Handler();
                handler.postDelayed(this::readWord, 1500);
            } else if (direction % 2 == 0 && solvedVerticalCount == verticalWords.size()) {
                speakSMTH("Вы отгадали все слова по вертикали. Давайте перейдем к горизонтальным");
                direction = 1;
                Handler handler = new Handler();
                handler.postDelayed(this::readWord, 1500);
            } else {
                //check whether there is next word
                if (direction == 1 || direction == 2) { //moving forward(right/down)
                    if (currentNumber < size - 1) { //there is next
                        if (direction == 1) {
                            currentH += 1;
                        } else {
                            currentV += 1;
                        }
                    } else {
                        if (direction == 1) {
                            currentH -= 1;
                        } else {
                            currentV -= 1;
                        }
                        direction += 2; // turn
                    }
                } else {//moving backward(left/up)
                    if (currentNumber > 0) { //there is next
                        if (direction == 3) {
                            currentH -= 1;
                        } else {
                            currentV -= 1;
                        }
                    } else {
                        if (direction == 3) {
                            currentH += 1;
                        } else {
                            currentV += 1;
                        }
                        direction += 2; // turn
                    }
                }
                readWord();

            }
        } else{
            currentAnswer = s.answer;
            String text = "";
            if (direction % 2 == 1) {
                text += s.getNumberOfWord() + "-ое по горизонтали. ";
            } else {
                text += s.getNumberOfWord() + "-ое по вертикали. ";
            }
            text += s.task + " " + s.answer.length() + " буквы.";
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

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        binding = null;
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        speechRecognizer.destroy();
        speechRecognizer = null;
        super.onStop();
    }
}
