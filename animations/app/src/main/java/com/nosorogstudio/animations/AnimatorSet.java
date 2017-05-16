package com.nosorogstudio.animations;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.ArrayList;

// Code from android.support.v7.view.ViewPropertyAnimatorCompatSet
public class AnimatorSet {

    public interface AnimatorBuilder {
        void build(ViewPropertyAnimatorCompat animator);
    }

    private final ArrayList<ViewPropertyAnimatorCompat> mAnimators;

    private long mDuration = -1;
    private Interpolator mInterpolator;
    private ViewPropertyAnimatorListener mListener;
    private Runnable mStartAction;
    private Runnable mEndAction;

    private boolean mIsStarted;

    public AnimatorSet() {
        mAnimators = new ArrayList<>();
    }

    public AnimatorSet play(ViewPropertyAnimatorCompat animator) {
        if (!mIsStarted) {
            mAnimators.add(animator);
        }
        return this;
    }

    public AnimatorSet play(View view, AnimatorBuilder animatorBuilder) {
        ViewPropertyAnimatorCompat animator = ViewCompat.animate(view);
        animatorBuilder.build(animator);
        return play(animator);
    }

    public AnimatorSet playSequentially(ViewPropertyAnimatorCompat anim1,
                                                          ViewPropertyAnimatorCompat anim2) {
        mAnimators.add(anim1);
        anim2.setStartDelay(anim1.getDuration());
        mAnimators.add(anim2);
        return this;
    }

    public void start() {
        if (mIsStarted) return;
        for (ViewPropertyAnimatorCompat animator : mAnimators) {
            if (mDuration >= 0) {
                animator.setDuration(mDuration);
            }
            if (mInterpolator != null) {
                animator.setInterpolator(mInterpolator);
            }
            if (mListener != null || mStartAction != null || mEndAction != null) {
                animator.setListener(mProxyListener);
            }
            animator.start();
        }

        mIsStarted = true;
    }

    private void onAnimationsEnded() {
        mIsStarted = false;
    }

    public void cancel() {
        if (!mIsStarted) {
            return;
        }
        for (ViewPropertyAnimatorCompat animator : mAnimators) {
            animator.cancel();
        }
        mIsStarted = false;
    }

    public AnimatorSet setDuration(long duration) {
        if (!mIsStarted) {
            mDuration = duration;
        }
        return this;
    }

    public AnimatorSet setInterpolator(Interpolator interpolator) {
        if (!mIsStarted) {
            mInterpolator = interpolator;
        }
        return this;
    }

    public AnimatorSet setListener(ViewPropertyAnimatorListener listener) {
        if (!mIsStarted) {
            mListener = listener;
        }
        return this;
    }

    public AnimatorSet withStartAction(Runnable runnable) {
        if (!mIsStarted) {
            mStartAction = runnable;
        }
        return this;
    }

    public AnimatorSet withEndAction(Runnable runnable) {
        if (!mIsStarted) {
            mEndAction = runnable;
        }
        return this;
    }

    private final ViewPropertyAnimatorListenerAdapter mProxyListener
            = new ViewPropertyAnimatorListenerAdapter() {
        private boolean mProxyStarted = false;
        private int mProxyEndCount = 0;

        @Override
        public void onAnimationStart(View view) {
            if (mProxyStarted) {
                return;
            }
            mProxyStarted = true;
            if (mListener != null) {
                mListener.onAnimationStart(null);
            }
            if (mStartAction != null) {
                mStartAction.run();
            }
        }

        void onEnd() {
            mProxyEndCount = 0;
            mProxyStarted = false;
            onAnimationsEnded();
        }

        @Override
        public void onAnimationEnd(View view) {
            if (++mProxyEndCount == mAnimators.size()) {
                if (mListener != null) {
                    mListener.onAnimationEnd(null);
                }
                if (mEndAction != null) {
                    mEndAction.run();
                }
                onEnd();
            }
        }
    };
}
