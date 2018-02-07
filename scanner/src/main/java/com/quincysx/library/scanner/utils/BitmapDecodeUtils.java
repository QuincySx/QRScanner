package com.quincysx.library.scanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import static android.content.ContentValues.TAG;

/**
 * @author QuincySx
 * @date 2018/1/6 下午9:42
 */
public class BitmapDecodeUtils {
    private static String TAG = BitmapDecodeUtils.class.getSimpleName();

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int sqrt = (int) Math.sqrt(maxMemory / 4d * 1024);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, sqrt, sqrt);
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        Log.e("====最大内存=====", maxMemory + "     " + options.inSampleSize + "    " + sqrt);

        int[] intArray = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        Log.i(TAG, "数组生命完成 " + System.currentTimeMillis());
        //copy pixel data from the Bitmap into the 'intArray' array
        scanBitmap.getPixels(intArray, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        Log.i(TAG, "数组转换完成 " + System.currentTimeMillis());

        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        if (!scanBitmap.isRecycled()) {
            scanBitmap.recycle();
            System.gc();
        }
        return scanQRImage(intArray, width, height);
    }

    public static Result scanQRImage(int[] intArray, int width, int height) {
        Log.i(TAG, "扫码开始 " + System.currentTimeMillis());
        LuminanceSource source = new RGBLuminanceSource(width, height, intArray);
        Log.i(TAG, "资源转换完成 完成 " + System.currentTimeMillis());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Log.i(TAG, "导入加载器 完成 " + System.currentTimeMillis());

        Reader reader = new MultiFormatReader();
        try {
            Log.i(TAG, " 开始解码 " + System.currentTimeMillis());
            Result result = reader.decode(bitmap);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, " 解码完成 " + System.currentTimeMillis());
        return null;
    }

    public static Result scanningImage(Bitmap scanBitmap) {
        try {
            Log.i(TAG, "扫码开始 " + System.currentTimeMillis());
            byte[] data = getYUV420sp(scanBitmap.getWidth(), scanBitmap.getHeight(), scanBitmap);

            Log.i(TAG, "YUV 完成 " + System.currentTimeMillis());

            Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();
            decodeFormats.add(BarcodeFormat.CODE_128);
            decodeFormats.add(BarcodeFormat.QR_CODE);

            Hashtable<DecodeHintType, Object> hints = new Hashtable();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

            Log.i(TAG, "开始 YUV 差值化 " + System.currentTimeMillis());
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,
                    scanBitmap.getWidth(),
                    scanBitmap.getHeight(),
                    0, 0,
                    scanBitmap.getWidth(),
                    scanBitmap.getHeight(),
                    false);

            Log.i(TAG, " YUV 差值化 完成 " + System.currentTimeMillis());

            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));

            Log.i(TAG, " 导入加载器 完成 " + System.currentTimeMillis());

            QRCodeReader reader2 = new QRCodeReader();
            Result result = null;
            try {
                Log.i(TAG, " 开始解码 " + System.currentTimeMillis());
                result = reader2.decode(bitmap1, hints);
                Log.e("hxy", result.getText());
            } catch (NotFoundException e) {
                Log.e("hxy", "NotFoundException");
            } catch (ChecksumException e) {
                Log.e("hxy", "ChecksumException");
            } catch (FormatException e) {
                Log.e("hxy", "FormatException");
            }
            Log.i(TAG, " 解码完成 " + System.currentTimeMillis());
            return result;
        } catch (OutOfMemoryError s) {
            s.printStackTrace();
            return null;
        }
    }


    public static byte[] getYUV420sp(int inputWidth, int inputHeight,
                                     Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];

        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }


    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width,
                                       int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;

        // ---颜色数据---
//      int a, R, G, B;
        int R, G, B;
        //
        int argbIndex = 0;
        //

        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                // a is not used obviously
//              a = (argb[argbIndex] & 0xff000000) >> 24;
                R = (argb[argbIndex] & 0xff0000) >> 16;
                G = (argb[argbIndex] & 0xff00) >> 8;
                B = (argb[argbIndex] & 0xff);
                //
                argbIndex++;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                //
                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));

                // NV21 has a plane of Y and interleaved planes of VU each
                // sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the
                // sampling is every other
                // pixel AND every other scanline.
                // ---Y---
                yuv420sp[yIndex++] = (byte) Y;

                // ---UV---
//              if ((j % 2 == 0) && (i % 2 == 0)) {
//
//
//
//                  yuv420sp[uvIndex++] = (byte) V;
//
//                  yuv420sp[uvIndex++] = (byte) U;
//              }
            }
        }
    }
}
