package com.kupangstudio.shoufangbao.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;

/**
 * Created by long1 on 16/1/25.
 * Copyright 16/1/25 android_xiaobai.
 */
public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mMs;
    private File mFile;
    private Context mContext;

    public SingleMediaScanner(Context context, File f) {
        mContext = context;
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Looper.prepare();
        Toast.makeText(mContext, "图片保存成功", Toast.LENGTH_SHORT).show();
        Looper.loop();
        mMs.disconnect();
    }
}
