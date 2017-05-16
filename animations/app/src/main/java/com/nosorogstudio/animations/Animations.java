package com.nosorogstudio.animations;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class Animations {

    public static void clear(View view) {
        ViewCompat.setAlpha(view, 1);
        ViewCompat.setScaleY(view, 1);
        ViewCompat.setScaleX(view, 1);
        ViewCompat.setTranslationY(view, 0);
        ViewCompat.setTranslationX(view, 0);
        ViewCompat.setRotation(view, 0);
        ViewCompat.setRotationY(view, 0);
        ViewCompat.setRotationX(view, 0);
        ViewCompat.setPivotY(view, view.getMeasuredHeight() / 2);
        ViewCompat.setPivotX(view, view.getMeasuredWidth() / 2);
        ViewCompat.animate(view).setInterpolator(null).setStartDelay(0)
                .setListener(null).withStartAction(null).withEndAction(null);
    }

    public static void clear(View ... views) {
        for (View view : views) {
            clear(view);
        }
    }

    public static ViewPropertyAnimatorCompat animateEntering(View view) {
        return ViewCompat.animate(view)
                .setDuration(225)
                .setInterpolator(new LinearOutSlowInInterpolator());
    }

    public static ViewPropertyAnimatorCompat animateLeaving(View view) {
        return ViewCompat.animate(view)
                .setDuration(195)
                .setInterpolator(new FastOutLinearInInterpolator());
    }

    public static ViewPropertyAnimatorCompat animateLeavingUp(View view) {
        return animateLeaving(view)
                .translationY(-view.getRootView().getHeight() / 2)
                .alpha(0);
    }

    public static ViewPropertyAnimatorCompat animateLeavingDown(View view) {
        return animateLeaving(view)
                .translationY(view.getRootView().getHeight() / 2)
                .alpha(0);
    }

    public static ViewPropertyAnimatorCompat animateEnteringDown(View view) {
        ViewCompat.setTranslationY(view, -view.getRootView().getHeight() / 2);
        ViewCompat.setAlpha(view, 0);
        return animateEntering(view)
                .translationY(0)
                .alpha(1);
    }

    public static ViewPropertyAnimatorCompat animateEnteringUp(View view) {
        ViewCompat.setTranslationY(view, view.getRootView().getHeight() / 2);
        ViewCompat.setAlpha(view, 0);
        return animateEntering(view)
                .translationY(0)
                .alpha(1);
    }

    public static AnimatorSet animateSetEnteringUp(View ... views) {
        AnimatorSet set = new AnimatorSet();

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            set.play(animateEnteringUp(view).setStartDelay(225 / 4 * i));
        }

        return set;
    }

    public static ViewPropertyAnimatorCompat animateSpring(View view, float tension) {
        return ViewCompat.animate(view)
                .setDuration(195)
                .setInterpolator(new OvershootInterpolator(tension));
    }

    public static ViewPropertyAnimatorCompat animateSpring(View view) {
        return animateSpring(view, 2.0f);
    }

    public static ViewPropertyAnimatorCompat animate(View view) {
        return ViewCompat.animate(view)
                .setDuration(300)
                .setInterpolator(new FastOutSlowInInterpolator());
    }

    public static AnimatorSet animateSetEntering() {
        return new AnimatorSet()
                .setDuration(225)
                .setInterpolator(new LinearOutSlowInInterpolator());
    }

    public static AnimatorSet animateSetLeaving() {
        return new AnimatorSet()
                .setDuration(195)
                .setInterpolator(new FastOutLinearInInterpolator());
    }

    public static AnimatorSet animateSet() {
        return new AnimatorSet()
                .setDuration(300)
                .setInterpolator(new FastOutSlowInInterpolator());
    }
}
