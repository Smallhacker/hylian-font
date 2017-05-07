package com.smallhacker.hylianfont.app;

import com.smallhacker.hylianfont.font.Rendering;

public enum ViewMode {
    WIDE_MODE ("Wide Tiles", Rendering.Mode.STANDARD, Rendering.Mode.STANDARD),
    NARROW_MODE ("Narrow Tiles", Rendering.Mode.NARROW, Rendering.Mode.MASKED);

    private final String name;
    private final Rendering.Mode selectionMode;
    private final Rendering.Mode tileMode;

    ViewMode(String name, Rendering.Mode selectionMode, Rendering.Mode tileMode) {
        this.name = name;
        this.selectionMode = selectionMode;
        this.tileMode = tileMode;
    }

    public String getName() {
        return name;
    }

    public Rendering.Mode getSelectionMode() {
        return selectionMode;
    }

    public Rendering.Mode getTileMode() {
        return tileMode;
    }
}
