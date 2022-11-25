package com.example.crosswordapplication.DrawingPackege;

public class Checker {

    public boolean isAnswered = false;
    public Character letter;

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public Character getLetter() {
        return letter;
    }

    public void setLetter(Character letter) {
        this.letter = letter;
    }

    public Checker(Character letter,boolean isAnswered) {
        this.isAnswered = isAnswered;
        this.letter = letter;
    }

    public Checker(Character letter) {
        this.letter = letter;
    }

}
