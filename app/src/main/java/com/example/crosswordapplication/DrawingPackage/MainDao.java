package com.example.crosswordapplication.DrawingPackage;



import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;



import java.util.ArrayList;
import java.util.List;


@Dao
public interface MainDao {

    @Insert(onConflict = REPLACE)
    void insert(SingleWord singleWord);

    @Delete
    void delete(SingleWord singleWord);

    @Delete
    void reset(List<SingleWord> singleWords2);

    @Query("UPDATE words SET solvedLetters = :sText WHERE ID = :sID")
    void updateSolvedCharacters(int sID, String sText);

    @Query("SELECT * FROM words")
    List<SingleWord> getAll();

    @Query("SELECT * FROM words WHERE ID = :sID ")
    SingleWord getOne(int sID);

    @Query("SELECT * FROM words WHERE numberOfWord = :number AND orientation = :ori ")
    SingleWord getByNumber(int number, boolean ori);

    @Query("SELECT * FROM words WHERE parent_crossword = :sParent")
    List<SingleWord> getFromCrossword(int sParent);


    @Query("UPDATE words SET isSolved = :S WHERE ID = :sID")
    void solved(int sID, boolean S);

    @Query("UPDATE words SET solvedLetters = :a WHERE numberOfWord = :number AND orientation = :ori")
    void updateSolvedLettersByNumber(int number,boolean ori, String a);

    @Query("SELECT solvedLetters FROM words")
    List<String> getAllSolvedLettersFromDB();

    @Query("SELECT solvedLetters FROM words WHERE ID = :sID")
    List<String> getParticularSolvedLettersFromDB(int sID);

    @Query("SELECT solvedLetters FROM words WHERE numberOfWord = :sNum AND orientation = :ori")
    List<String> getParticularSolvedLettersFromDB(int sNum, boolean ori);


    @Query("UPDATE words SET solvedLetters = :a WHERE ID = :sID")
    void updateSolvedLetters(int sID, String a);
}
