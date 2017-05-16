package com.nosorogstudio.animations;

import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class Interpolators {
    public static final Interpolator linear = new LinearInterpolator();

    public static final Interpolator enterInterpolator = new LinearOutSlowInInterpolator();
    public static final long enterDuration = 225;

    public static final Interpolator exitInterpolator = new FastOutLinearInInterpolator();
    public static final long exitDuration = 195;

    public static final Interpolator changeInterpolator = new FastOutSlowInInterpolator();
    public static final long changeDuration = 300;

    public static final Interpolator accelerate = new AccelerateInterpolator();
    public static final Interpolator decelerate = new DecelerateInterpolator();
    public static final Interpolator accelerateDecelerate = new AccelerateDecelerateInterpolator();

    public static Interpolator combine(Interpolator interpolator1, Interpolator interpolator2, float firstDurationFactor) {
        return new Combined(interpolator1, interpolator2, firstDurationFactor);
    }

    public static Interpolator combine(Interpolator interpolator1, long duration1, Interpolator interpolator2, long duration2) {
        long totalDuration = duration1 + duration2;
        float firstDurationFactor = (float) duration1 / (float) totalDuration;
        return combine(interpolator1, interpolator2, firstDurationFactor);
    }

    public static final Interpolator just0 = new Just0();
    public static final Interpolator just1 = new Just1();

    public static Interpolator delayed(Interpolator interpolator, long delayDuration, long interpolatorDuration) {
        return combine(just0, delayDuration, interpolator, interpolatorDuration);
    }

    private static class Combined implements Interpolator {
        private final Interpolator interpolator1;
        private final Interpolator interpolator2;
        private final float firstDurationFactor;

        public Combined(Interpolator interpolator1, Interpolator interpolator2, float firstDurationFactor) {
            this.interpolator1 = interpolator1;
            this.interpolator2 = interpolator2;
            this.firstDurationFactor = firstDurationFactor;
        }

        @Override
        public float getInterpolation(float v) {
            if (v < firstDurationFactor) {
                float v1 = v / firstDurationFactor;
                return interpolator1.getInterpolation(v1);
            } else {
                float v2 = (v - firstDurationFactor) / (1.0f - firstDurationFactor);
                return interpolator2.getInterpolation(v2);
            }
        }
    }

    private static class Just0 implements Interpolator {
        @Override
        public float getInterpolation(float v) {
            return 0;
        }
    }

    private static class Just1 implements Interpolator {

        @Override
        public float getInterpolation(float v) {
            return 1;
        }
    }
}
