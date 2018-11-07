package com.gaswa.calculatrice.donnee;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;

@Entity
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
