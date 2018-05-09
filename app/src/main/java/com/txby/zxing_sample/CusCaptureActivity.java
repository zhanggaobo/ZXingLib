package com.txby.zxing_sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.txby.zxing.activity.CaptureFragment;
import com.txby.zxing.utils.CodeUtils;
import com.txby.zxing_sample.utils.ImageUtil;

/**
 * @author wangyd
 * @date 2018/5/8
 * @description description
 */
public class CusCaptureActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RC_IMAGE_PICK = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_capture);

        initView();
    }

    private void initView() {
        CaptureFragment captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, R.layout.fragment_cus_capture);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        captureFragment.setCameraInitCallBack(cameraInitCallBack);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_my_container, captureFragment).commit();

        findViewById(R.id.capture_flashlight).setOnClickListener(this);
        findViewById(R.id.capture_scan_photo).setOnClickListener(this);
    }

    /**
     * 二维码解析回调
     */
    private CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {

        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            CusCaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CusCaptureActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            CusCaptureActivity.this.setResult(RESULT_OK, resultIntent);
            CusCaptureActivity.this.finish();
        }
    };

    /**
     * 相机初始化回调
     */
    private CaptureFragment.CameraInitCallBack cameraInitCallBack = new CaptureFragment.CameraInitCallBack() {

        @Override
        public void callBack(Exception e) {
            if (e != null) {
                Log.e("TAG", "callBack: ", e);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture_flashlight:

                break;
            // 相册选取
            case R.id.capture_scan_photo:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, RC_IMAGE_PICK);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 选择系统图片并解析
        if (requestCode == RC_IMAGE_PICK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = ImageUtil.getImageAbsolutePath(this, uri);
                CodeUtils.analyzeBitmap(path, new CodeUtils.AnalyzeCallback() {
                    @Override
                    public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                        Toast.makeText(CusCaptureActivity.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAnalyzeFailed() {
                        Toast.makeText(CusCaptureActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
