package com.example.crosswordapplication.CrosswordPackage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


import com.example.crosswordapplication.DrawingPackage.SingleWord;

@Database(entities = {SingleWord2.class}, version = 1, exportSchema = false)
@TypeConverters({SingleWord2.Converters.class})
public abstract class CrossDB extends RoomDatabase {

    private static CrossDB database;
    private static String DATABASE_NAME = "last";


    public synchronized static CrossDB getInstance(Context context){

        if (database == null){
            database = Room.databaseBuilder(context.getApplicationContext()
                            , CrossDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract LittleDao littleDao();

}
