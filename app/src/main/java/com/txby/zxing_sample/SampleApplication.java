package com.txby.zxing_sample;

import android.app.Application;

import com.txby.zxing.ZXingLib;

/**
 * ......┌─┐       ┌─┐ + +
 * ...┌──┘ ┴───────┘ ┴──┐++
 * ...│                 │
 * ...│       ───       │++ + + +
 * ...███████───███████ │+
 * ...│                 │+
 * ...│       ─┴─       │
 * ...└───┐         ┌───┘
 * .......│         │
 * .......│         │           神兽保佑 永无 Bug
 * .......│         │   + +
 * .......│         │
 * .......│         └───────────────┐
 * .......│                         ├─┐
 * .......│                         ┌─┘
 * .......└─┐  ┐  ┌───────┬──┐  ┌───┘  + + + +
 * .........│ ─┤ ─┤       │ ─┤ ─┤
 * .........└──┴──┘       └──┴──┘  + + + +
 *
 * @author wangyd
 * @date 2018/5/8
 * @description description
 */
public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化
        ZXingLib.initDisplayOpinion(this);
    }
}
