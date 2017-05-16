package com.nosorogstudio.animations;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

import com.nosorogstudio.animations.util.Lambda;
import com.nosorogstudio.animations.util.Math2;

public class AnimatableDegrees {

    public AnimatableDegrees() {
        this(0.0f);
    }

    public AnimatableDegrees(float value) {
        this.value = value;
    }

    public AnimatableDegrees configInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public AnimatableDegrees configDuration(long duration) {
        this.duration = duration;
        this.useSpeed = false;
        return this;
    }

    public AnimatableDegrees configSpeed(float degreesPerSecond) {
        this.speed = degreesPerSecond;
        this.useSpeed = true;
        return this;
    }

    public AnimatableDegrees configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableDegrees configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableDegrees configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableDegrees configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableDegrees configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableDegrees configEnd(@NonNull Runnable action) {
        this.actionForEnd = action;
        return this;
    }

    private TimeInterpolator interpolator = Interpolators.changeInterpolator;
    private long duration = Interpolators.changeDuration;
    private float speed = 0.0f;
    private boolean useSpeed = false;
    private View viewForInvalidate;
    private View viewForRequestLayout;
    private @NonNull
    Runnable actionForInvalidate = Lambda.none;
    private @NonNull
    Runnable actionForEnd = Lambda.none;

    private float value = 0.0f;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        stop();
        this.value = value;
        invalidate();
    }

    public void setValueAnimated(float value, boolean animated) {
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

    public void animate(float target) {
        stop();

        float from = this.value;
        float to = target;
        from = Math2.normalizeAngle360(from);
        to = Math2.normalizeAngle360(to);

        float difference = Math.abs(from - to);
        if (difference > 180) {
            if (to > from) {
                from += 360;
            } else {
                to += 360;
            }
        }

        animator = ValueAnimator.ofFloat(from, to);
        animator.addListener(animatorListener);
        animator.addUpdateListener(onAnimationUpdate);
        animator.setInterpolator(interpolator);

        long duration = this.duration;
        if (useSpeed) {
            float distance = Math.abs(to - from);
            float seconds = distance / this.speed;
            duration = (long) (seconds * 1000);
        }
        animator.setDuration(duration);

        animator.start();
    }

    private ValueAnimator animator = null;

    private final ValueAnimator.AnimatorUpdateListener onAnimationUpdate = animation -> {
        float degrees = (Float) animation.getAnimatedValue();
        degrees = Math2.normalizeAngle360(degrees);
        AnimatableDegrees.this.value = degrees;
        AnimatableDegrees.this.invalidate();
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
