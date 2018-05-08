package com.bankai.bleach.imageprocessor;

import android.os.AsyncTask;

// TODO to prevent the Warnings, these AsyncTasks can be used as an inner class of ResultActivity
public class RemoteProcessingTask  extends AsyncTask<Void, Void, Void> {
    private ResultActivity _resultActivity;

    public RemoteProcessingTask(ResultActivity resultActivity) {
        _resultActivity = resultActivity;
        // TODO can pass in other stuff you want to
    }


    @Override
    protected Void doInBackground(Void... voids) {
        try {
            publishProgress();
            // Simulate processing.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        _resultActivity.toBeChangedFunctionToPopulateImageArraysAfterGettingResults();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        _resultActivity.setProgressText("Processing Remotely ...");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        _resultActivity.showProgressView(false);
    }
}