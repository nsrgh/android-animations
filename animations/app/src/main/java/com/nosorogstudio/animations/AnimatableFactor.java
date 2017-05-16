package com.nosorogstudio.animations;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;

import com.nosorogstudio.animations.util.Lambda;


public class AnimatableFactor {

    public AnimatableFactor() {
        this(false);
    }

    public AnimatableFactor(boolean one) {
        this.one = one;
        this.value = one ? 1.0f : 0.0f;
    }

    public AnimatableFactor configToOne(TimeInterpolator interpolator, long duration) {
        this.interpolatorToOne = interpolator;
        this.durationToOne = duration;
        return this;
    }

    public AnimatableFactor configToZero(TimeInterpolator interpolator, long duration) {
        this.interpolatorToZero = interpolator;
        this.durationToZero = duration;
        return this;
    }

    public AnimatableFactor configAsEnterExit() {
        configToOne(Interpolators.enterInterpolator, Interpolators.enterDuration);
        configToZero(Interpolators.exitInterpolator, Interpolators.exitDuration);
        return this;
    }

    public AnimatableFactor configAsExitEnter() {
        configToOne(Interpolators.exitInterpolator, Interpolators.exitDuration);
        configToZero(Interpolators.enterInterpolator, Interpolators.enterDuration);
        return this;
    }

    public AnimatableFactor configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableFactor configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableFactor configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableFactor configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableFactor configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableFactor configEnd(@NonNull Runnable action) {
        this.actionForEnd = action;
        return this;
    }

    private TimeInterpolator interpolatorToOne = Interpolators.enterInterpolator;
    private long durationToOne = Interpolators.enterDuration;
    private TimeInterpolator interpolatorToZero = Interpolators.exitInterpolator;
    private long durationToZero = Interpolators.exitDuration;
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
    private boolean one = false;

    public float getValue() {
        return value;
    }

    public void setOne() {
        setOne(Lambda.none);
    }

    public void setOne(@NonNull Runnable completion) {
        if (one) {
            completion.run();
            return;
        }
        one = true;

        stop();
        this.value = 1.0f;
        invalidate();
        completion.run();
    }

    public void setZero() {
        setZero(Lambda.none);
    }

    public void setZero(@NonNull Runnable completion) {
        if (!one) {
            completion.run();
            return;
        }
        one = false;

        stop();
        this.value = 0.0f;
        invalidate();
        completion.run();
    }

    public boolean isOne() {
        return one;
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
            completion = Lambda.none;
            changing = false;
        }
    }

    public void animateToOne() {
        animateToOne(Lambda.none);
    }

    public void animateToOne(@NonNull Runnable completion) {
        if (one) {
            completion.run();
            return;
        }
        one = true;

        stop();

        animator = ValueAnimator.ofFloat(this.value, 1.0f);
        animator.addListener(animatorListener);
        animator.addUpdateListener(onAnimationUpdate);
        animator.setInterpolator(interpolatorToOne);
        animator.setDuration(durationToOne);
        this.completion = completion;
        animator.start();
    }

    public void animateToZero() {
        animateToZero(Lambda.none);
    }

    public void animateToZero(@NonNull Runnable completion) {
        if (!one) {
            completion.run();
            return;
        }
        one = false;

        stop();

        animator = ValueAnimator.ofFloat(this.value, 0.0f);
        animator.addListener(animatorListener);
        animator.addUpdateListener(onAnimationUpdate);
        animator.setInterpolator(interpolatorToZero);
        animator.setDuration(durationToZero);
        this.completion = completion;
        this.changing = true;
        animator.start();
    }

    public void setOneAnimated(boolean animated) {
        setOneAnimated(animated, Lambda.none);
    }

    public void setOneAnimated(boolean animated, @NonNull Runnable completion) {
        if (animated) {
            animateToOne(completion);
        } else {
            setOne(completion);
        }
    }

    public void setZeroAnimated(boolean animated) {
        setZeroAnimated(animated, Lambda.none);
    }

    public void setZeroAnimated(boolean animated, @NonNull Runnable completion) {
        if (animated) {
            animateToZero(completion);
        } else {
            setZero(completion);
        }
    }

    public void setValueAnimated(boolean one, boolean animated) {
        setValueAnimated(one, animated, Lambda.none);
    }

    public void setValueAnimated(boolean one, boolean animated, @NonNull Runnable completion) {
        if (one) {
            setOneAnimated(animated, completion);
        } else {
            setZeroAnimated(animated, completion);
        }
    }

    public boolean isChanging() {
        return changing;
    }

    private ValueAnimator animator = null;

    private final ValueAnimator.AnimatorUpdateListener onAnimationUpdate = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            AnimatableFactor.this.value = (Float) animation.getAnimatedValue();
            AnimatableFactor.this.invalidate();
        }
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
        callInvalidate(viewForInvalidate);
        callRequestLayout(viewForRequestLayout);
        actionForInvalidate.run();
    }

    protected void callEnd() {
        changing = false;
        actionForEnd.run();
        completion.run();
        completion = Lambda.none;
    }

    private void callInvalidate(View view) {
        if (view != null) {
            view.invalidate();
        }
    }

    private void callRequestLayout(View view) {
        if (view != null) {
            view.requestLayout();
        }
    }
}
