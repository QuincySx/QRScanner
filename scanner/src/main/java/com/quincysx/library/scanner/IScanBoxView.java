package com.quincysx.library.scanner;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

/**
 * @author QuincySx
 * @date 2018/1/7 下午5:42
 */
public interface IScanBoxView {
    /**
     * 返回扫描框的扫描区域
     *
     * @param previewWidth  相机预览高度
     * @param previewHeight 相机预览宽度
     * @return 扫描框区域
     */
    Rect getScanBoxAreaRect(int previewWidth, int previewHeight);
}
