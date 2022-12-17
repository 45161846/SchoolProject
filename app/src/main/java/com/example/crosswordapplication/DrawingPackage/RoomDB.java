package com.example.crosswordapplication.DrawingPackage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.crosswordapplication.CrosswordPackage.SingleWord2;

@Database(entities = {SingleWord.class}, version = 1, exportSchema = false)
@TypeConverters({SingleWord.Converters.class})
public abstract class RoomDB extends RoomDatabase {

    private static RoomDB database;
    private static String DATABASE_NAME = "words";


    public synchronized static RoomDB getInstance(Context context){

        if (database == null){
            database = Room.databaseBuilder(context.getApplicationContext()
                            ,RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract MainDao mainDao();

}
