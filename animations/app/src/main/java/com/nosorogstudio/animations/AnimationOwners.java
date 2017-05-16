package com.nosorogstudio.animations;

public class AnimationOwners {
    public static AnimationOwner forView(android.view.View view) {
        return new ForView(view);
    }

    private static class ForView implements AnimationOwner {
        private final android.view.View view;

        public ForView(android.view.View view) {
            this.view = view;
        }

        @Override
        public void addOnDetachListener(Runnable listener) {
            view.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(android.view.View view) {
                }

                @Override
                public void onViewDetachedFromWindow(android.view.View view) {
                    listener.run();
                }
            });
        }
    }
}
