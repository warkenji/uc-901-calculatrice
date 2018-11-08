package com.gaswa.calculatrice.donnee;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ItemHistoriqueDao {
    @Query("SELECT * FROM ItemHistorique")
    LiveData<List<ItemHistorique>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemHistorique itemHistorique);

    @Query("DELETE FROM ItemHistorique")
    void deleteAll();
}
