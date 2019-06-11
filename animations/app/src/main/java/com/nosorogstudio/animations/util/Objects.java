package com.nosorogstudio.animations.util;

import android.os.Build;

import com.nosorogstudio.animations.BuildConfig;

/**
 * Created by N. Korobeinikov on 2019-06-11.
 */
public class Objects {

    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return java.util.Objects.equals(a, b);
        } else {
            //noinspection EqualsReplaceableByObjectsCall
            return (a == b) || (a != null && a.equals(b));
        }
    }

}
