/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cardinfolink.qrscanner.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.cardinfolink.qrscanner.R;
import com.cardinfolink.qrscanner.base.BaseActivity;
import com.cardinfolink.qrscanner.utils.ToastUtils;
import com.cardinfolink.qrscanner.view.ScanBoxView;
import com.cardinfolink.qrscanner.zxing.camera.CameraManager;
import com.cardinfolink.qrscanner.zxing.decode.DecodeThread;
import com.cardinfolink.qrscanner.zxing.utils.BeepManager;
import com.cardinfolink.qrscanner.zxing.utils.ScanFragmentHandler;
import com.cardinfolink.qrscanner.zxing.utils.InactivityTimer;
import com.google.zxing.Result;

import java.io.IOException;


/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends BaseActivity {
    private ScanFragment mScanFragment;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        ScanFragment scanFragment = ScanFragment.newInstance(new ResultCallback() {
            @Override
            public void onSuccess(Result result) {
                Log.e("====", "++++++" + result.getText());
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_scan, scanFragment)
                .commitNowAllowingStateLoss();
    }

}