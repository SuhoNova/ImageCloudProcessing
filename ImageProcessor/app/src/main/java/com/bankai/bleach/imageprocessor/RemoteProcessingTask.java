package com.bankai.bleach.imageprocessor;

import android.os.AsyncTask;

public class RemoteProcessingTask  extends AsyncTask<Void, Void, Void> {

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