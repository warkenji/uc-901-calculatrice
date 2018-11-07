package com.gaswa.calculatrice.donnee;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ItemHistoriqueDao {
    @Query("SELECT * FROM ItemHistorique")
    List<ItemHistorique> getAll();

    @Insert
    void insert(ItemHistorique itemHistorique);

    @Query("DELETE FROM ItemHistorique")
    void deleteAll();
}
