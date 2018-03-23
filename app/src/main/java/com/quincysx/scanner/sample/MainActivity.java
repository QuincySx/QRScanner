package com.quincysx.scanner.sample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;
import com.quincysx.library.scanner.utils.BitmapDecodeUtils;
import com.quincysx.library.scanner.utils.RealPathUtil;
import com.quincysx.library.scanner.utils.UriUtils;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_select_album:
                startPick();
                break;
            default:
        }
    }

    private void startPick() {
        Intent innerIntent = new Intent();//"android.intent.action.GET_CONTENT"
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_PICK);
        }
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        startActivityForResult(wrapperIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    String photo_path = "";
                    Uri fileUri = data.getData();
                    photo_path = UriUtils.uriToFilename(this, fileUri);
                    final String finalPhoto_path = photo_path;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String result = BitmapDecodeUtils.scanningImageZbar(finalPhoto_path);
                                // String result = decode(photo_path);
                                if (result == null || result.equals("")) {
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), "没有扫描到二维码", Toast.LENGTH_SHORT)
                                            .show();
                                    Looper.loop();
                                } else {
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "图片太大，请扫码处理", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                    break;
            }
        }
    }

}
