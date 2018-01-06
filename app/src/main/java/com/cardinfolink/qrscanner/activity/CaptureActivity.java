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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.cardinfolink.qrscanner.R;
import com.cardinfolink.qrscanner.base.BaseActivity;
import com.cardinfolink.qrscanner.utils.BitmapDecodeUtils;
import com.google.zxing.Result;
import com.jph.takephoto.app.XTakePhoto;
import com.jph.takephoto.model.TResult;


/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends BaseActivity implements XTakePhoto.TakeResultListener {
    private ScanFragment mScanFragment;
    private XTakePhoto mXTakePhoto;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        mXTakePhoto = XTakePhoto.with(this);

        mScanFragment = ScanFragment.newInstance(new ResultCallback() {
            @Override
            public void onSuccess(Result result) {
                Log.e("====", "++++++" + result.getText());
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_scan, mScanFragment)
                .commitNowAllowingStateLoss();
    }

    public void onClick(View view) {
        mXTakePhoto.onPickFromGallery();
    }

    @Override
    public void takeSuccess(TResult tResult) {
        Result result = BitmapDecodeUtils.scanningImage(tResult.getImage().getOriginalPath());
        if (result != null) {
            Toast.makeText(this, result.getText(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "图片识别失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void takeFail(TResult tResult, String s) {

    }

    @Override
    public void takeCancel() {

    }
}