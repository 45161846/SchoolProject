package com.example.crosswordapplication.CrosswordPackage;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.crosswordapplication.DrawingPackage.SingleWord;

import java.util.List;


@Dao
public interface LittleDao {

    @Insert(onConflict = REPLACE)
    void insert(SingleWord2 singleWord);

    @Delete
    void delete(SingleWord2 singleWord);

    @Delete
    void reset(List<SingleWord2> singleWords);

    @Query("UPDATE last SET solved = :sText WHERE ID = :sID")
    void updateSolvedCharacters(int sID, String sText);

    @Query("SELECT * FROM last")
    List<SingleWord2> getAll();

    @Query("SELECT * FROM last WHERE ID = :sID ")
    SingleWord2 getOne(int sID);

    @Query("SELECT * FROM last WHERE parent_crossword = :sParent")
    List<SingleWord2> getFromCrossword(int sParent);

    @Query("UPDATE last SET isSolved = :S WHERE ID = :sID")
    void update(int sID, boolean S);

}
