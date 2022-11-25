package com.example.crosswordapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.crosswordapplication.DrawingPackege.Orientations;
import com.example.crosswordapplication.DrawingPackege.SingleWord;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    int charLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.drawerPlace);
        surfaceHolder = surfaceView.getHolder();

        Button button = findViewById(R.id.surfaceButton);

        Crossword crossword = new Crossword(this,100,100, new SingleWord[]{new SingleWord("a", "a", 1, 1, Orientations.HORIZONTAL)});

        button.setOnClickListener(v -> crossword.drawBackground());
    }

    public class Crossword{

        int sizeY;
        int sizeX;
        SingleWord[] words;
        Paint paint = new Paint();


        public void drawBackground() {
            int y = surfaceView.getHeight();
            int x = surfaceView.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(x ,y, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas = surfaceHolder.lockCanvas();
            paint.setColor(Color.BLUE);
            for(int i=0;i<x;i+=10){
                canvas.drawLine((float)i,0f,(float)i,y,paint);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

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
