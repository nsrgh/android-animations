package com.nosorogstudio.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Interpolator;

import com.nosorogstudio.animations.util.Lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnimatableState<T> {

    public AnimatableState() {
        this(null);
    }

    public AnimatableState(T value) {
        this.value = value;
    }

    private static class AnimationParams {
        public final Interpolator interpolator;
        public final long duration;

        public AnimationParams(Interpolator interpolator, long duration) {
            this.interpolator = interpolator;
            this.duration = duration;
        }
    }

    public AnimatableState<T> config(T from, T to, Interpolator interpolator, long duration) {
        animationParamsSelector.add(from, to, new AnimationParams(interpolator, duration));
        return this;
    }

    public AnimatableState<T> configAsEnter(T from, T to) {
        return config(from, to, Interpolators.enterInterpolator, Interpolators.enterDuration);
    }

    public AnimatableState<T> configAsExit(T from, T to) {
        return config(from, to, Interpolators.exitInterpolator, Interpolators.exitDuration);
    }

    public AnimatableState<T> configAsChange(T from, T to) {
        return config(from, to, Interpolators.changeInterpolator, Interpolators.changeDuration);
    }

    public AnimatableState<T> configDefault(Interpolator interpolator, long duration) {
        animationParamsSelector.addDefault(new AnimationParams(interpolator, duration));
        return this;
    }

    public AnimatableState<T> configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableState<T> configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableState<T> configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableState<T> configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableState<T> configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableState<T> configEnd(@NonNull Runnable action) {
        this.actionForEnd = action;
        return this;
    }

    private final Selector<T, AnimationParams> animationParamsSelector = new Selector<>();
    private View viewForInvalidate;
    private View viewForRequestLayout;
    private @NonNull
    Runnable actionForInvalidate = Lambda.none;
    private @NonNull
    Runnable actionForEnd = Lambda.none;
    private @NonNull
    Runnable completion = Lambda.none;

    private T prevValue = null;

    public T getPrevValue() {
        return prevValue;
    }

    private boolean changing = false;

    public boolean isChanging() {
        return changing;
    }

    private float progress = 1.0f;

    public float getProgress() {
        return progress;
    }

    private T value = null;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (Objects.equals(this.value, value)) {
            return;
        }

        stop();
        this.prevValue = this.value;
        this.value = value;
        this.progress = 1.0f;
        invalidate();
        callEnd();
    }

    public void setValueAnimated(T value, boolean animated) {
        if (animated) {
            animate(value);
        } else {
            setValue(value);
        }
    }

    public void setValueAnimated(T value, boolean animated, @NonNull Runnable completion) {
        if (animated) {
            animate(value, completion);
        } else {
            setValue(value);
            completion.run();
        }
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        this.progress = 1.0f;
        this.completion = Lambda.none;
    }

    public void animate(T target) {
        animate(target, Lambda.none);
    }

    public void animate(T target, @NonNull Runnable completion) {
        if (Objects.equals(this.value, target)) {
            completion.run();
            return;
        }

        AnimationParams animationParams = animationParamsSelector.select(this.value, target);
        if (animationParams == null) {
            setValue(target);
            completion.run();
        } else {
            stop();

            this.completion = completion;
            prevValue = value;
            value = target;
            progress = 0.0f;

            animator = ValueAnimator.ofFloat(0, 1);
            animator.addListener(animatorListener);
            animator.addUpdateListener(onAnimationUpdate);
            animator.setInterpolator(animationParams.interpolator);
            animator.setDuration(animationParams.duration);

            animator.start();
        }
    }

    private ValueAnimator animator = null;

    private final ValueAnimator.AnimatorUpdateListener onAnimationUpdate = animation -> {
        AnimatableState.this.progress = (Float) animation.getAnimatedValue();
        AnimatableState.this.invalidate();
    };

    private final ValueAnimator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            changing = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            changing = false;
            AnimatableState.this.callEnd();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    protected void callEnd() {
        actionForEnd.run();
        completion.run();
        completion = Lambda.none;
    }

    protected void invalidate() {
        if (viewForInvalidate != null) {
            viewForInvalidate.invalidate();
        }
        if (viewForRequestLayout != null) {
            viewForRequestLayout.requestLayout();
        }
        actionForInvalidate.run();
    }

    public static class Selector<T, TResult> {
        private static class Entry<T, TResult> {
            public T from;
            public T to;
            public TResult result;
        }

        private final List<Entry<T, TResult>> entries = new ArrayList<>();
        private TResult defaultValue = null;

        public void add(T from, T to, TResult result) {
            Entry<T, TResult> entry = new Entry<>();
            entry.from = from;
            entry.to = to;
            entry.result = result;
            entries.add(entry);
        }

        public void addDefault(TResult result) {
            defaultValue = result;
        }

        public TResult select(T from, T to) {
            for (Entry<T, TResult> entry : entries) {
                if (Objects.equals(entry.from, from) && Objects.equals(entry.to, to)) {
                    return entry.result;
                }
            }
            return defaultValue;
        }
    }

    public <TResult> TResult select(Selector<T, TResult> selector) {
        return selector.select(this.prevValue, this.value);
    }

    public void run(Selector<T, Runnable> selector) {
        Runnable runnable = selector.select(this.prevValue, this.value);
        if (runnable != null) {
            runnable.run();
        }
    }

    public class SelectorBuilder<TResult> {
        private Selector<T, TResult> selector = new Selector<>();

        public SelectorBuilder<TResult> add(T from, T to, TResult result) {
            selector.add(from, to, result);
            return this;
        }

        public SelectorBuilder<TResult> addDefault(TResult result) {
            selector.addDefault(result);
            return this;
        }

        public TResult select() {
            return AnimatableState.this.select(selector);
        }
    }

    public <TResult> SelectorBuilder<TResult> selector() {
        return new SelectorBuilder<>();
    }

    public class RunnableBuilder {
        private Selector<T, Runnable> selector = new Selector<>();

        public RunnableBuilder add(T from, T to, Runnable result) {
            selector.add(from, to, result);
            return this;
        }

        public RunnableBuilder addDefault(Runnable result) {
            selector.addDefault(result);
            return this;
        }

        public Runnable select() {
            return AnimatableState.this.select(selector);
        }

        public void run() {
            AnimatableState.this.run(selector);
        }
    }

    public RunnableBuilder runnable() {
        return new RunnableBuilder();
    }
}
