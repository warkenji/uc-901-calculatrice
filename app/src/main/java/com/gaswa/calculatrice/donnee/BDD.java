package com.gaswa.calculatrice.donnee;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ItemHistorique.class}, version = 1, exportSchema = false)
public abstract class BDD extends RoomDatabase {
    private static BDD instance = null;
    public abstract ItemHistoriqueDao itemHistoriqueDao();

    public static BDD getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder(context, BDD.class, "bdd-historique").build();
        }

        return instance;
    }
}
