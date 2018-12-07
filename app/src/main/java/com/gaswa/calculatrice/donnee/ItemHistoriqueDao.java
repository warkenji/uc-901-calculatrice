package com.gaswa.calculatrice.donnee;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ItemHistoriqueDao {
    @Query("SELECT * FROM ItemHistorique")
    LiveData<List<ItemHistorique>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemHistorique itemHistorique);

    @Query("DELETE FROM ItemHistorique")
    void deleteAll();
}
