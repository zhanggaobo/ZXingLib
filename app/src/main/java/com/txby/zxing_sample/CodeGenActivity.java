package com.txby.zxing_sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.txby.zxing.utils.CodeUtils;

/**
 * @author wangyd
 * @date 2018/5/8
 * @description 生成二维码
 */
public class CodeGenActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtContent;
    private ImageView ivCode;
    private String content;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_gen);

        edtContent = findViewById(R.id.edt_content);
        ivCode = findViewById(R.id.iv_code);

        findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);

        edtContent.setText("测试数据");
    }

    private boolean checkInput() {
        content = edtContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "输入内容为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1:
                if (checkInput()) {
                    Bitmap bitmap = CodeUtils.createImage(content, 400, 400,
                            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                    ivCode.setImageBitmap(bitmap);
                }
                break;
            case R.id.btn_2:
                if (checkInput()) {
                    Bitmap bitmap = CodeUtils.createImage(content, 400, 400, null);
                    ivCode.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }
}
