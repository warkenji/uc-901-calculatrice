package com.gaswa.calculatrice.donnee;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

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
