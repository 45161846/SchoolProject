package com.example.crosswordapplication.DrawingPackage;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;


@Dao
public interface MainDao {

    @Insert(onConflict = REPLACE)
    void insert(SingleWord singleWord);

    @Delete
    void delete(SingleWord singleWord);

    @Delete
    void reset(List<SingleWord> singleWords);

    @Query("UPDATE words SET solved = :sText WHERE ID = :sID")
    void updateSolvedCharacters(int sID, String sText);

    @Query("SELECT * FROM words")
    List<SingleWord> getAll();

    @Query("SELECT * FROM words WHERE parent_crossword = :sParent")
    List<SingleWord> getFromCrossword(int sParent);

    @Query("UPDATE words SET isSolved = :S WHERE ID = :sID")
    void update(int sID, boolean S);

}
