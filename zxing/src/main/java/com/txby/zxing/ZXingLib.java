package com.txby.zxing;

import android.content.Context;
import android.util.DisplayMetrics;

import com.txby.zxing.utils.DisplayUtil;


/**
 * @author wangyd
 * @date 2018/5/8
 * @description zxing初始化
 */
public class ZXingLib {

    public static void initDisplayOpinion(Context context) {
        if (context == null) {
            return;
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(context, dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(context, dm.heightPixels);
    }
}
