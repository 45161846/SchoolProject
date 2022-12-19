package com.example.crosswordapplication.DrawingPackage;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SingleWord.class}, version = 2, exportSchema = false)
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
