package org.nv95.openmanga.components.reader.webtoon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.concurrent.Executor;

/**
 * Created by admin on 02.08.17.
 */

public class AsyncBitmapDecoder implements Runnable {

    private final String mFilename;
    private final DecodeCallback mCallback;

    public AsyncBitmapDecoder(String filename, DecodeCallback callback) {
        mFilename = filename;
        mCallback = callback;
    }

    @Override
    public void run() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(mFilename);
            mCallback.onBitmapDecoded(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DecodeCallback {
        @WorkerThread
        void onBitmapDecoded(@Nullable Bitmap bitmap);
    }

    public static void decode(String filename, DecodeCallback callback, Executor executor) {
        executor.execute(new AsyncBitmapDecoder(filename, callback));
    }
}
