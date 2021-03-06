package com.quincysx.library.scanner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;
import com.quincysx.library.scanner.zxing.camera.CameraManager;
import com.quincysx.library.scanner.zxing.decode.DecodeThread;
import com.quincysx.library.scanner.zxing.utils.BeepManager;
import com.quincysx.library.scanner.zxing.utils.InactivityTimer;

import java.io.IOException;

/**
 * @author QuincySx
 * @date 2017/12/26 下午2:21
 */
public class ScanFragment extends Fragment implements TextureView.SurfaceTextureListener {
    public static ScanFragment newInstance(IScanBoxView scanBoxView, ResultCallback resultCallback) {
        return newInstance(DefVerifyScan, scanBoxView, resultCallback);
    }

    public static ScanFragment newInstance(VerifyScan verifyScan, IScanBoxView scanBoxView, ResultCallback resultCallback) {
        ScanFragment scanFragment = new ScanFragment();
        scanFragment.setResultCallback(resultCallback);
        scanFragment.mVerifyScan = verifyScan;
        scanFragment.mIScanBoxView = scanBoxView;
        return scanFragment;
    }

    private static VerifyScan DefVerifyScan = new VerifyScan() {
        @Override
        public boolean isVerify(String text) {
            return true;
        }
    };

    private static final String TAG = "CaptureFragment";
    private static final int REQUEST_PERMISSION_CAMERA = 0x2;

    private ResultCallback mResultCallback;
    private VerifyScan mVerifyScan;
    private CameraManager cameraManager;
    private ScanFragmentHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private TextureView scanPreview = null;
    private IScanBoxView mIScanBoxView = null;
    private boolean isHasSurface = false;
    private Rect mCropRect = null;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public ResultCallback getResultCallback() {
        return mResultCallback;
    }

    public VerifyScan getVerifyScan() {
        return mVerifyScan;
    }

    public void setVerifyScan(VerifyScan verifyScan) {
        mVerifyScan = verifyScan;
    }

    public void setResultCallback(ResultCallback resultCallback) {
        mResultCallback = resultCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_scan_box, container, false);
        scanPreview = inflate.findViewById(R.id.capture_preview);
        scanPreview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        scanPreview.setAlpha(0f);

        inactivityTimer = new InactivityTimer(getActivity());
        beepManager = new BeepManager(getActivity());
        return inflate;
    }

    @Override
    public void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getContext());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getSurfaceTexture());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.setSurfaceTextureListener(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.setSurfaceTextureListener(null);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     */
    public void handleDecode(Result rawResult) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        if (mVerifyScan.isVerify(rawResult.getText())) {
            mResultCallback.onSuccess(rawResult);
        } else {
            Toast.makeText(getActivity(), R.string.hint_qr_scanner_verify_error, Toast.LENGTH_SHORT).show();
        }
        restartPreviewAfterDelay(1000);
    }

    private void initCamera(SurfaceTexture surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        scanPreview.setAlpha(1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            return;
        }

        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new ScanFragmentHandler(this, cameraManager, DecodeThread.QRCODE_MODE);
            }
            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().finish();
            }
        });
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().height;
        int cameraHeight = cameraManager.getCameraResolution().width;
        mCropRect = mIScanBoxView.getScanBoxAreaRect(cameraWidth, cameraHeight);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera(scanPreview.getSurfaceTexture());
            } else {
                Toast.makeText(getContext(), R.string.hint_qrscan_no_camera_permission, Toast.LENGTH_SHORT).show();
                // Permission Denied
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (surface == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isHasSurface = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
