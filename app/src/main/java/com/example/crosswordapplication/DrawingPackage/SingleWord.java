package com.example.crosswordapplication.DrawingPackage;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "words")
public class SingleWord implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int ID;

    @ColumnInfo(name = "parent_crossword")
    private int parent_crossword;

    @ColumnInfo(name = "numberOfWord")
    public int numberOfWord;

    @ColumnInfo(name = "x")
    public int startX;

    @ColumnInfo(name = "y")
    public int startY;

    @ColumnInfo(name = "crosses")
    public ArrayList<int[]> crosses;

    @ColumnInfo(name = "orientation")
    public boolean orientation;

    @ColumnInfo(name = "isSolved")
    public boolean isSolved;

    @ColumnInfo(name = "answer")
    public String answer;

    public SingleWord() {

    }

    @ColumnInfo(name = "task")
    public String task;

    @ColumnInfo(name = "solvedLetters")
    private String solvedLetters;


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getParent_crossword() {
        return parent_crossword;
    }

    public void setParent_crossword(int parent_crossword) {
        this.parent_crossword = parent_crossword;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public String getCrossesString() {
        return solvedLetters;
    }

    public void setCrossesString(String crossesString) {
        this.solvedLetters = crossesString;
    }


    public String getSolvedLetters() {
        return solvedLetters;
    }
    public int getNumberOfWord() {
        return numberOfWord;
    }

    public void setNumberOfWord(int numberOfWord) {
        this.numberOfWord = numberOfWord;
    }
    public void setSolvedLetters(String solvedLetters) {
        this.solvedLetters = solvedLetters;
    }

    public ArrayList<int[]> getCrosses() {
        return crosses;
    }

    public void setCrosses(ArrayList<int[]> crosses) {
        this.crosses = crosses;
    }

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

    public SingleWord(int parent_crossword, int numberOfWord, String task, String answer, int startX, int startY, boolean orientation, ArrayList<int[]> crosses,String solvedLetters,boolean isSolved) {
        this.task = task;
        this.orientation = orientation;
        this.startX = startX;
        this.startY = startY;
        this.parent_crossword = parent_crossword;

        this.answer = answer;
        this.crosses = crosses;
        this.numberOfWord = numberOfWord;
        this.isSolved = isSolved;
        this.solvedLetters = solvedLetters;
    }

    public static class Converters {
        @TypeConverter
        public static ArrayList<int[]> fromString(String value) {
            Type listType = new TypeToken<ArrayList<int[]>>() {
            }.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(ArrayList<int[]> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }
        @TypeConverter
        public static String fromArray(String list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }
        @TypeConverter
        public static List<Boolean> arrayFromString(String value) {
            Type listType = new TypeToken<List<Boolean>>() {
            }.getType();
            return new Gson().fromJson(value, listType);
        }
    }

}
