package com.nosorogstudio.animations;

import android.animation.TimeInterpolator;
import android.support.annotation.NonNull;
import android.view.Choreographer;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nosorogstudio.animations.util.Lambda;

public class AnimatableTimer {

    public AnimatableTimer() {
    }

    public AnimatableTimer configInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public AnimatableTimer configPeriod(long period) {
        this.period = period;
        return this;
    }

    public AnimatableTimer configInvalidate(View view) {
        this.viewForInvalidate = view;
        configOwner(this.viewForInvalidate);
        return this;
    }

    public AnimatableTimer configRequestLayout(View view) {
        this.viewForRequestLayout = view;
        configOwner(this.viewForRequestLayout);
        return this;
    }

    public AnimatableTimer configOwner(AnimationOwner owner) {
        owner.addOnDetachListener(this::stop);
        return this;
    }

    public AnimatableTimer configOwner(View view) {
        AnimationOwner owner = AnimationOwners.forView(view);
        return configOwner(owner);
    }

    public AnimatableTimer configInvalidate(@NonNull Runnable action) {
        this.actionForInvalidate = action;
        return this;
    }

    public AnimatableTimer configTick(@NonNull Runnable action) {
        this.actionForTick = action;
        return this;
    }

    private TimeInterpolator interpolator = new LinearInterpolator();
    private long period = 300;
    private View viewForInvalidate;
    private View viewForRequestLayout;
    private @NonNull
    Runnable actionForInvalidate = Lambda.none;
    private @NonNull
    Runnable actionForTick = Lambda.none;

    private float progress = 0.0f;
    private float value = 0.0f;

    public float getValue() {
        return value;
    }

    private boolean active = false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean newActive) {
        if (newActive) {
            start();
        } else {
            stop();
        }
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;
        lastFrameNanos = 0;
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    public void stop() {
        if (!active) {
            return;
        }
        active = false;
        Choreographer.getInstance().removeFrameCallback(frameCallback);
    }

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long nanos) {
            update(nanos);
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private long lastFrameNanos = 0;
    private long elapsedNanos = 0;

    private void update(long nanos) {
        long deltaNanos = 0;
        if (lastFrameNanos == 0) {
            elapsedNanos = 0;
        } else {
            deltaNanos = nanos - lastFrameNanos;
        }
        lastFrameNanos = nanos;

        long periodNanos = this.period * 1000000;
        elapsedNanos += deltaNanos;
        while (elapsedNanos > periodNanos) {
            elapsedNanos -= periodNanos;
            tick();
        }
        this.progress = (float) ((double) elapsedNanos / (double) periodNanos);
        interpolate();
    }

    private void interpolate() {
        value = interpolator.getInterpolation(progress);
        invalidate();
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

    protected void tick() {
        actionForTick.run();
    }
}
