package com.quincysx.library.scanner.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

/**
 * @author QuincySx
 * @date 2018/2/7 上午10:46
 */
public class UriUtils {
    public static String uriToFilename(Context context, Uri uri) {
        String path = getRealPathFromUri(context, uri);
        if (path == null) {
            if (Build.VERSION.SDK_INT < 11) {
                path = RealPathUtil.getRealPathFromURI_BelowAPI11(context, uri);
            } else if (Build.VERSION.SDK_INT < 19) {
                path = RealPathUtil.getRealPathFromURI_API11to18(context, uri);
            } else {
                path = RealPathUtil.getRealPathFromURI_API19(context, uri);
            }
        }
        return path;
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
