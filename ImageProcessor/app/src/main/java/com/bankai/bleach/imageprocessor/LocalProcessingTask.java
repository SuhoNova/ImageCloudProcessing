package com.bankai.bleach.imageprocessor;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LocalProcessingTask  extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Simulate processing.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

    }
}
