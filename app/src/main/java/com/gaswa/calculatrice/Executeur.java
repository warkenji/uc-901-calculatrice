package com.gaswa.calculatrice;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class Executeur implements Executor {
    @Override
    public void execute(@NonNull Runnable command) {
        Thread thread = new Thread(command);
        thread.start();
    }
}
