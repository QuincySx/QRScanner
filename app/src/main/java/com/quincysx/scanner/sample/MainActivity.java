package com.quincysx.scanner.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;
import com.jph.takephoto.app.XTakePhoto;
import com.jph.takephoto.model.TResult;
import com.quincysx.library.scanner.utils.BitmapDecodeUtils;

public class MainActivity extends AppCompatActivity implements XTakePhoto.TakeResultListener {
    private XTakePhoto mXTakePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mXTakePhoto = XTakePhoto.with(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_select_album:
                mXTakePhoto.onPickFromGallery();
                break;
            default:
        }
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
