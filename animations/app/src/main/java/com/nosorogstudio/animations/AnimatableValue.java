package com.nosorogstudio.animations;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

import com.nosorogstudio.animations.util.Lambda;

public class AnimatableValue {

    public AnimatableValue() {
        this(0.0f);
    }

    public AnimatableValue(float value) {
        this.value = value;
    }

    public AnimatableValue configInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public AnimatableValue configDuration(long duration) {
        this.duration = duration;
        this.useSpeed = false;
        return this;
    }

    public AnimatableValue configSpeed(float unitsPerSecond) {
        this.speed = unitsPerSecond;
        this.useSpeed = true;
        return this;
    }

    public AnimatableValue configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableValue configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableValue configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableValue configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableValue configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableValue configEnd(@NonNull Runnable action) {
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
    private @NonNull
    Runnable completion = Lambda.none;
    private boolean changing = false;

    private float value = 0.0f;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        setValue(value, completion);
    }

    public void setValue(float value, @NonNull Runnable completion) {
        stop();
        this.value = value;
        invalidate();
        completion.run();
    }

    public void setValueAnimated(float value, boolean animated) {
        setValueAnimated(value, animated, Lambda.none);
    }

    public void setValueAnimated(float value, boolean animated, @NonNull Runnable completion) {
        if (animated) {
            animate(value, completion);
        } else {
            setValue(value, completion);
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
            completion = Lambda.none;
            changing = false;
        }
    }

    public void animate(float target) {
        animate(target, Lambda.none);
    }

    public void animate(float target, @NonNull Runnable completion) {
        stop();

        animator = ValueAnimator.ofFloat(this.value, target);
        animator.addListener(animatorListener);
        animator.addUpdateListener(onAnimationUpdate);
        animator.setInterpolator(interpolator);

        long duration = this.duration;
        if (useSpeed) {
            float distance = Math.abs(target - this.value);
            float seconds = distance / this.speed;
            duration = (long) (seconds * 1000);
        }
        animator.setDuration(duration);

        this.completion = completion;
        this.changing = true;

        animator.start();
    }

    public boolean isChanging() {
        return changing;
    }

    private ValueAnimator animator = null;

    private final ValueAnimator.AnimatorUpdateListener onAnimationUpdate = animation -> {
        AnimatableValue.this.value = (Float) animation.getAnimatedValue();
        AnimatableValue.this.invalidate();
    };

    private final ValueAnimator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            callEnd();
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

    protected void callEnd() {
        changing = false;
        actionForEnd.run();
        completion.run();
        completion = Lambda.none;
    }
}
