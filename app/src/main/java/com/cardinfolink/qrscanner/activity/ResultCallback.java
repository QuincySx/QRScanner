package com.cardinfolink.qrscanner.activity;

import com.google.zxing.Result;

/**
 * @author QuincySx
 * @date 2017/12/26 下午2:46
 */
public interface ResultCallback {
    void onSuccess(Result result);
}
