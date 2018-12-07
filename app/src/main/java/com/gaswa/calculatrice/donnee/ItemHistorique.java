package com.gaswa.calculatrice.donnee;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = "calcul", unique = true)})
public class ItemHistorique {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo
    public String calcul;

    @ColumnInfo
    public String resultat;

    public ItemHistorique()
    {
        id = UUID.randomUUID().toString() + "-" + Long.toString(System.currentTimeMillis());
    }
}
