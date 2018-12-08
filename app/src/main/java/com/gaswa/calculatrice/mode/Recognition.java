package com.gaswa.calculatrice.mode;

import android.content.Intent;

import androidx.annotation.NonNull;

public abstract class Recognition {
    public abstract void pick();
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
    public abstract void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults);
}
