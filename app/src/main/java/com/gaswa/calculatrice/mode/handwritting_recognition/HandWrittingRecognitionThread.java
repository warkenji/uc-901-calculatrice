package com.gaswa.calculatrice.mode.handwritting_recognition;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class HandWrittingRecognitionThread extends Thread {
    private boolean continuer;
    private HandWrittingRecognition surface;
    private SurfaceHolder surfaceHolder;

    HandWrittingRecognitionThread(HandWrittingRecognition surface, SurfaceHolder surfaceHolder)  {
        this.surface = surface;
        this.surfaceHolder= surfaceHolder;
    }

    @Override
    public void run()  {
        while(continuer)  {
            Canvas canvas = null;
            try {
                // Get Canvas from Holder and lock it.
                canvas = this.surfaceHolder.lockCanvas();

                // Synchronized
                synchronized (canvas)  {
                    this.surface.update();
                    this.surface.draw(canvas);
                }
            }catch(Exception ignore)
            {

            }

            if(canvas!= null)
            {
                this.surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void setContinuer(boolean continuer)  {
        this.continuer = continuer;
    }
}
