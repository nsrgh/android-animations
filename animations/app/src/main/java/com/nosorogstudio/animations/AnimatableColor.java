package com.nosorogstudio.animations;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

import com.nosorogstudio.animations.util.Colors;
import com.nosorogstudio.animations.util.Lambda;

public class AnimatableColor {

    public enum Mode {
        Rgb,
        Hsv
    }

    public AnimatableColor() {
        this(0);
    }

    public AnimatableColor(int value) {
        this.value = value;
    }

    public AnimatableColor configInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public AnimatableColor configDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public AnimatableColor configMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public AnimatableColor configRgb() {
        return configMode(Mode.Rgb);
    }

    public AnimatableColor configHsv() {
        return configMode(Mode.Hsv);
    }

    public AnimatableColor configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableColor configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableColor configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableColor configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableColor configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableColor configEnd(@NonNull Runnable action) {
        this.actionForEnd = action;
        return this;
    }

    private TimeInterpolator interpolator = Interpolators.changeInterpolator;
    private long duration = Interpolators.changeDuration;
    private Mode mode = Mode.Rgb;
    private View viewForInvalidate;
    private View viewForRequestLayout;
    private @NonNull
    Runnable actionForInvalidate = Lambda.none;
    private @NonNull
    Runnable actionForEnd = Lambda.none;

    private int value = 0;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        stop();
        this.value = value;
        invalidate();
    }

    public void setValueAnimated(int value, boolean animated) {
        if (animated) {
            animate(value);
        } else {
            setValue(value);
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    public void animate(int target) {
        if (this.value == target) {
            return;
        }
        if (this.animationColorTo == target) {
            return;
        }

        stop();

        this.animationColorFrom = this.value;
        this.animationColorTo = target;

        animator = ValueAnimator.ofFloat(0, 1);
        animator.addListener(animatorListener);
        animator.addUpdateListener(onAnimationUpdate);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);

        animator.start();
    }

    private ValueAnimator animator = null;
    private int animationColorFrom = 0;
    private int animationColorTo = 0;

    private final ValueAnimator.AnimatorUpdateListener onAnimationUpdate = animation -> {
        float progress = (Float) animation.getAnimatedValue();
        switch (mode) {
            case Rgb:
                AnimatableColor.this.value = Colors.interpolateRgb(animationColorFrom, animationColorTo, progress);
                break;
            case Hsv:
                AnimatableColor.this.value = Colors.interpolateHsv(animationColorFrom, animationColorTo, progress);
                break;
        }

        AnimatableColor.this.invalidate();
    };

    private final ValueAnimator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            actionForEnd.run();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    protected void invalidate() {
        if (viewForInvalidate != null) {
            viewForInvalidate.invalidate();
        }
        if (viewForRequestLayout != null) {
            viewForRequestLayout.requestLayout();
        }
        actionForInvalidate.run();
    }
}
