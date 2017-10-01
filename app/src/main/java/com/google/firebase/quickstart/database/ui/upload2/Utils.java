package com.google.firebase.quickstart.database.ui.upload2;

import android.content.Context;

/**
 * Created by korrio on 9/28/2017 AD.
 */

public class Utils {

    public static int dip2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
