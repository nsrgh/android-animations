package com.nosorogstudio.animations.util;


import java.util.function.Consumer;

public class Lambda {
    public static final Runnable none = () -> {};

    public static <T> Consumer<T> noneConsumer() {
        return t -> {};
    }
}
