package com.txby.zxing.activity;

import android.graphics.Bitmap;

/**

 * @author wangyd
 * @date 2018/5/8
 * @description 二维码解析回调
 */
public interface AnalyzeCallback {

    void onAnalyzeSuccess(Bitmap mBitmap, String result);

    void onAnalyzeFailed();
}

