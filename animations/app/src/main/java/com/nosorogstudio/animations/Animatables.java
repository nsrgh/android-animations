package com.nosorogstudio.animations;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.nosorogstudio.animations.util.Math2;

public class Animatables {

    public static AnimatableFactor actionAccept(View view) {
        AnimatableFactor acceptFactor = new AnimatableFactor(false)
                .configToOne(new AccelerateDecelerateInterpolator(), 500);

        acceptFactor
                .configInvalidate(view)
                .configInvalidate(() -> {
                    float factor = acceptFactor.getValue();
                    if (factor > 0.5f) {
                        factor = 1.0f - Math2.subProgressClamp(factor, 0.5f, 1.0f);
                    } else {
                        factor = Math2.subProgressClamp(factor, 0.0f, 0.5f);
                    }
                    float scale = Math2.interpolate(1.0f, 1.1f, factor);
                    view.setScaleX(scale);
                    view.setScaleY(scale);
                });

        return acceptFactor;
    }


    public static AnimatableFactor actionDismiss(View view) {
        AnimatableFactor dismissFactor = new AnimatableFactor(false);

        dismissFactor
                .configInvalidate(view)
                .configInvalidate(() -> {
                    view.setAlpha(Math2.interpolate(1.0f, 0.333f, dismissFactor.getValue()));
                });

        return dismissFactor;
    }

}
