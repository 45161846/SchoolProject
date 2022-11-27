package com.example.crosswordapplication;

import static java.lang.Math.abs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.crosswordapplication.DrawingPackege.Orientations;
import com.example.crosswordapplication.DrawingPackege.SingleWord;

public class MainActivity extends AppCompatActivity {
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private float oldDist = 1f;

    private float x;
    private float y;

    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 4f;

    float startX;
    float startY;
    int startRight;
    int startBottom;
    int startLeft;
    int startTop;
    float scale;


    RelativeLayout.LayoutParams parms;
    int startwidth;
    int startheight;


    RelativeLayout relativeLayout;

    Bitmap picture;
    ImageView imageView;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.surfaceButton);
        TextView textView = findViewById(R.id.text_view);

        Crossword crossword = new Crossword(this, 10, 10, new SingleWord[]{new SingleWord("aQWEDqaeswf", "aaeswf", 2, 2, Orientations.HORIZONTAL)});

        button.setOnClickListener(view -> {
            crossword.drawBackground();
            crossword.drawWord(crossword.words[0]);
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> crossword.drawBackground());
        //zoomageView = findViewById(R.id.myZoomageView);
        relativeLayout = findViewById(R.id.relative_layout);

        imageView = findViewById(R.id.image_view);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final ImageView view = (ImageView) v;
                
//                if (event.getAction() == MotionEvent.ACTION_DOWN){
//                    startX = event.getRawX();
//                    startY = event.getRawY();
//                }
                
                
                ((BitmapDrawable) view.getDrawable()).setAntiAlias(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        parms = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        startwidth = parms.width;
                        startheight = parms.height;

                        startRight = parms.rightMargin;
                        startBottom = parms.bottomMargin;
                        startLeft = parms.leftMargin;
                        startTop = parms.topMargin;

                        startX = event.getRawX();
                        startY = event.getRawY();
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (event.getPointerCount()==2) {
                            mode = ZOOM;
                        }

                        break;
                    case MotionEvent.ACTION_UP:

                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {

                            x = event.getRawX();
                            y = event.getRawY();

                            parms.leftMargin = startLeft + (int) (x - startX);
                            parms.topMargin = startTop + (int) (y - startY);
                            parms.rightMargin = startRight - (int) (x - startX);
                            parms.bottomMargin = startBottom -(int) (y - startY);

                            if(abs(parms.leftMargin)>imageView.getWidth()* (imageView.getScaleX()-1)/2){
                                if(parms.leftMargin>0){
                                    parms.leftMargin = (int)(imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                }else{
                                    parms.leftMargin = (int)((-1)*imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                }
                            }
                            if(abs(parms.rightMargin)>imageView.getWidth()* (imageView.getScaleX()-1)/2){
                                if(parms.rightMargin>0){
                                    parms.rightMargin = (int)(imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                }else{
                                    parms.rightMargin = (int)((-1)*imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                }

                            }
                            if(abs(parms.topMargin)>imageView.getHeight()* (imageView.getScaleX()-1)/2){
                                if(parms.topMargin>0){
                                    parms.topMargin = (int)(imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                }else{
                                    parms.topMargin = (int)((-1)*imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                }
                            }
                            if(abs(parms.bottomMargin)>imageView.getHeight()* (imageView.getScaleX()-1)/2){
                                if(parms.bottomMargin>0){
                                    parms.bottomMargin = (int)(imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                }else{
                                    parms.bottomMargin = (int)((-1)*imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                }
                            }

                            textView.setText(parms.leftMargin +" "+parms.topMargin +"    "+imageView.getScaleX() + "   "+
                                    imageView.getWidth());
                            view.setLayoutParams(parms);

                        } else if (mode == ZOOM) {

                            if (event.getPointerCount() == 2) {

                                x = event.getRawX();
                                y = event.getRawY();

                                float newDist = spacing(event);
                                if (newDist > 10f) {
                                    scale = newDist / oldDist * view.getScaleX();
                                    if(scale>MAX_SCALE){
                                        scale = MAX_SCALE;
                                    }
                                    if(scale<MIN_SCALE){
                                        scale = MIN_SCALE;
                                    }

                                    view.setScaleX(scale);
                                    view.setScaleY(scale);

                                }

                                if(abs(parms.leftMargin)>imageView.getWidth()* (imageView.getScaleX()-1)/2){
                                    if(parms.leftMargin>0){
                                        parms.leftMargin = (int)(imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                    }else{
                                        parms.leftMargin = (int)((-1)*imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                    }
                                }
                                if(abs(parms.rightMargin)>imageView.getWidth()* (imageView.getScaleX()-1)/2){
                                    if(parms.rightMargin>0){
                                        parms.rightMargin = (int)(imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                    }else{
                                        parms.rightMargin = (int)((-1)*imageView.getWidth()* (imageView.getScaleX()-1)/2);
                                    }

                                }
                                if(abs(parms.topMargin)>imageView.getHeight()* (imageView.getScaleX()-1)/2){
                                    if(parms.topMargin>0){
                                        parms.topMargin = (int)(imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                    }else{
                                        parms.topMargin = (int)((-1)*imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                    }
                                }
                                if(abs(parms.bottomMargin)>imageView.getHeight()* (imageView.getScaleX()-1)/2){
                                    if(parms.bottomMargin>0){
                                        parms.bottomMargin = (int)(imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                    }else{
                                        parms.bottomMargin = (int)((-1)*imageView.getHeight()* (imageView.getScaleX()-1)/2);
                                    }
                                }

                                view.setLayoutParams(parms);


                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + (event.getAction() & MotionEvent.ACTION_MASK));
                }

                return true;
            }
        });

    }

    public class Crossword {


        int sizeY;
        int sizeX;
        SingleWord[] words;




        public void drawBackground() {

            Paint paint = new Paint();

            int x = relativeLayout.getWidth();
            int y = relativeLayout.getHeight();
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

            imageView.setImageBitmap(picture);
        }


        @SuppressLint("ResourceAsColor")
        public void drawWord(SingleWord word) {
            //settings canvas, bitmap, paint for text drawing
            //ImageView.ScaleType scaleType = zoomageView.getScaleType();
            Paint paint = new Paint();

            int x = relativeLayout.getWidth();
            int y = relativeLayout.getHeight();
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
            imageView.setImageBitmap(picture);
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

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
