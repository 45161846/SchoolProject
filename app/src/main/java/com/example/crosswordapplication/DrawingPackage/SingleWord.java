package com.example.crosswordapplication.DrawingPackage;

public class SingleWord {
    public int startX;
    public int startY;
    Orientations orientation;

    public int wordLength;
    public Checker[] letters;
    public String answer;
    public String task;

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

    public Orientations isOrientation() {
        return orientation;
    }

    public void setOrientation(Orientations orientation) {
        this.orientation = orientation;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public Checker[] getLetters() {
        return letters;
    }

    public void setLetters(Checker[] letters) {
        this.letters = letters;
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

    public SingleWord(String task, String l, int x, int y, Orientations orientation) {
        this.task = task;
        this.orientation = orientation;
        startX = x;
        startY = y;
        this.wordLength = l.length();
        answer = l;

        letters = new Checker[wordLength];
        for (int i = 0; i < wordLength; i++) {
            letters[i] = new Checker(answer.charAt(i));
        }
    }

    public SingleWord(String task,String l, boolean[] opened, int x, int y, Orientations orientation) {
        this.task = task;
        this.orientation = orientation;
        startX = x;
        startY = y;
        this.wordLength = l.length();
        this.answer = l;
        for (int i = 0; i < wordLength; i++) {
            letters[i] = new Checker(answer.charAt(i));
            if (opened[i]) {
                letters[i].setAnswered(true);
            }
        }
    }


}
