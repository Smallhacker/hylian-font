package com.smallhacker.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Handlers<T> {
    private final List<Consumer<T>> handlers = new ArrayList<>();

    private Handlers() {
    }

    public static <T> Handlers<T> handlers() {
        return new Handlers<T>();
    }

    public void add(Consumer<T> handler) {
        handlers.add(handler);
    }

    public void invoke(T event) {
        handlers.forEach(h -> h.accept(event));
    }
}
