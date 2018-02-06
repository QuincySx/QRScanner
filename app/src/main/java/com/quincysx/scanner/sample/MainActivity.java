package com.quincysx.scanner.sample;

import android.content.Intent;
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
// innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT); 这个方法报 图片地址 空指针；使用下面的方法
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
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Uri fileUri = data.getData();
                    photo_path = uriToFilename(fileUri);

                    final String finalPhoto_path = photo_path;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Result result = BitmapDecodeUtils.scanningImage(finalPhoto_path);
                            // String result = decode(photo_path);
                            if (result == null) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "图片格式有误", Toast.LENGTH_SHORT)
                                        .show();
                                Looper.loop();
                            } else {
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }).start();
                    break;
            }
        }
    }

    private String uriToFilename(Uri uri) {
        String path = null;
        if (Build.VERSION.SDK_INT < 11) {
            path = RealPathUtil.getRealPathFromURI_BelowAPI11(this, uri);
        } else if (Build.VERSION.SDK_INT < 19) {
            path = RealPathUtil.getRealPathFromURI_API11to18(this, uri);
        } else {
            path = RealPathUtil.getRealPathFromURI_API19(this, uri);
        }

        return path;
    }

}
