package com.gaswa.calculatrice.mode.handwritting_recognition;

import android.graphics.Path;

import java.io.Serializable;

public class FingerPath implements Serializable {

    public int color;
    public boolean emboss;
    public boolean blur;
    public int strokeWidth;
    public Path path;

    public FingerPath(int color, boolean emboss, boolean blur, int strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}