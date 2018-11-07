package com.gaswa.calculatrice.donnee;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {ItemHistorique.class}, version = 1)
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
