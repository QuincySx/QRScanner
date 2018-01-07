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

package com.quincysx.scanner.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.Result;
import com.quincysx.library.scanner.ResultCallback;
import com.quincysx.library.scanner.ScanBoxView;
import com.quincysx.library.scanner.ScanFragment;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        ScanBoxView scanBoxView = findViewById(R.id.capture_crop_view_v);

        ScanFragment scanFragment = ScanFragment.newInstance(scanBoxView, new ResultCallback() {
            @Override
            public void onSuccess(Result result) {
                Toast.makeText(CaptureActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_scan, scanFragment)
                .commitAllowingStateLoss();
    }

}