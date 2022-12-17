package com.example.crosswordapplication.CrosswordPackage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

@Entity(tableName = "last")
public class SingleWord2 implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int ID;

    @ColumnInfo(name = "parent_crossword")
    private int parent_crossword;


    @ColumnInfo(name = "x")
    private int startX;
    @ColumnInfo(name = "y")
    private int startY;

    @ColumnInfo(name = "orientation")
    private boolean orientation;

    @ColumnInfo(name = "isSolved")
    private boolean isSolved;

    public int wordLength;

    @ColumnInfo(name = "answer")
    public String answer;
    @ColumnInfo(name = "task")
    public String task;

    @ColumnInfo(name = "solved")
    private String crossesString;

    private ArrayList<int[]> crosses;

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public boolean isOrientation() {
        return orientation;
    }

    public void setOrientation(boolean orientation) {
        this.orientation = orientation;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public SingleWord2(int parent_crossword,String task, String l, int x, int y, boolean orientation, ArrayList<int[]> crosses) {
        this.task = task;
        this.orientation = orientation;
        startX = x;
        startY = y;
        this.parent_crossword = parent_crossword;
        this.wordLength = l.length();
        answer = l;
        this.crosses = crosses;

    }

    public SingleWord2(int parent_crossword, String task,String l, boolean[] opened, int x, int y, boolean orientation, ArrayList<int[]> crosses) {
        this.task = task;
        this.orientation = orientation;
        startX = x;
        startY = y;
        this.parent_crossword = parent_crossword;
        this.wordLength = l.length();
        this.answer = l;
        this.crosses = crosses;
    }
    public static class Converters {
        @TypeConverter
        public static ArrayList<String> fromString(String value) {
            Type listType = new TypeToken<ArrayList<int[]>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(ArrayList<int[]> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }
    }

}
