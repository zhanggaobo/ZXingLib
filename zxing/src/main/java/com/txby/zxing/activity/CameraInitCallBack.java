package com.txby.zxing.activity;

/**
 * @author wangyd
 * @date 2018/5/8
 * @description 相机初始化回调
 */
public interface CameraInitCallBack {
    /**
     * Callback for Camera init result.
     *
     * @param e If is's null,means success.otherwise Camera init failed with the Exception.
     */
    void callBack(Exception e);
}
