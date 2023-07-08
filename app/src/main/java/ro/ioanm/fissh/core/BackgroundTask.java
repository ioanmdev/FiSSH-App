package ro.ioanm.fissh.core;

import android.content.Context;

public abstract class BackgroundTask {

    public void execute(){
        new Thread(() -> doInBackground()).start();
    }

    public abstract void doInBackground();
}