package com.example.crosswordapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

//import com.example.crosswordapplication.DrawingPackege.Checker;
import com.example.crosswordapplication.DrawingPackege.Checker;
import com.example.crosswordapplication.DrawingPackege.Orientations;
import com.example.crosswordapplication.DrawingPackege.SingleWord;
import com.jsibbold.zoomage.ZoomageView;

public class MainActivity extends AppCompatActivity {

    int charLength;
    ZoomageView zoomageView;
    Bitmap picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.surfaceButton);

        Crossword crossword = new Crossword(this, 10, 10, new SingleWord[]{new SingleWord("aQWEDqaeswf", "aaeswf", 2, 2, Orientations.HORIZONTAL)});

        button.setOnClickListener(view -> {
            crossword.drawBackground();
            crossword.drawWord(crossword.words[0]);
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> crossword.drawBackground());
        zoomageView = findViewById(R.id.myZoomageView);

    }

    public class Crossword {
        boolean zoomFlag = true;

        int sizeY;
        int sizeX;
        SingleWord[] words;

        public void flashBackground() {
            Paint paint = new Paint();

            int x = zoomageView.getWidth();
            int y = zoomageView.getHeight();
            int oneChackLength = x / sizeX;
            picture = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(picture);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            canvas.drawRect(0, y, x, 0, paint);



            zoomageView.setImageBitmap(picture);


        }


        public void drawBackground() {
            ZoomageView.ScaleType scaleType = zoomageView.getScaleType();
            float sX = zoomageView.getScaleX();
            float sY = zoomageView.getScaleY();
            Paint paint = new Paint();
            float fx =zoomageView.getScaleX();
            float fy =zoomageView.getScaleY();


            int x = zoomageView.getWidth();
            int y = zoomageView.getHeight();
            int oneChackLength = x / sizeX;
            picture = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(picture);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            canvas.drawRect(0, y, x, 0, paint);

            paint.setColor(Color.BLUE);
            for (int i = 0; i < x; i += oneChackLength) {
                canvas.drawRect(i, y, i + 5, 0, paint);
            }
            for (int i = 5; i < y; i += oneChackLength) {
                canvas.drawRect(0, i, x, i - 5, paint);
            }

            zoomageView.setImageBitmap(picture);
            zoomageView.setScaleX(sX);
            zoomageView.setScaleY(sY);
            //zoomageView.setScaleType(scaleType);


        }


        @SuppressLint("ResourceAsColor")
        public void drawWord(SingleWord word) {
            //settings canvas, bitmap, paint for text drawing
            //ImageView.ScaleType scaleType = zoomageView.getScaleType();
            Paint paint = new Paint();

            int x = zoomageView.getWidth();
            int y = zoomageView.getHeight();
            int oneChackLength = x / sizeX;

            Canvas canvas = new Canvas();
            canvas.setBitmap(picture);

            //paint.setColor(R.color.task_text_1);
            paint.setColor(Color.argb(255, 255, 239, 102));
            paint.setTextSize((int) (((float) oneChackLength) * 0.95));
            //paint.setStyle(Paint.Style.FILL);
            //draw each Checker
            for (int i = 0; i < word.letters.length; i++) {
                //draw Checker = word.letters[i]
                canvas.drawText(word.letters[i].letter.toString(), ((float) word.startX + 0.3f + i) * oneChackLength * 0.985f, ((float) word.startY + 0.8f) * oneChackLength, paint);

            }
            zoomageView.setImageBitmap(picture);
            //zoomageView.setScaleType(scaleType);
        }

//        @SuppressLint("ResourceAsColor")
//        public void drawChecker(Checker c) {
//
//        }

        public Crossword(Context context, int sizeY, int sizeX, SingleWord[] words) {

            this.sizeY = sizeY;
            this.sizeX = sizeX;
            this.words = words;
        }

        public int getSizeY() {
            return sizeY;
        }

        public int getSizeX() {
            return sizeX;
        }

        public void setSize(int sizeY, int sizeX) {
            this.sizeY = sizeY;
            this.sizeX = sizeX;
        }

        public SingleWord[] getWords() {
            return words;
        }

        public void setWords(SingleWord[] words) {
            this.words = words;
        }
    }

}
