package com.smallhacker.hylianfont.font;

public final class Rendering {
    private final int scale;
    private final Mode mode;
    private final Palette palette;

    public Rendering(int scale, Mode mode, Palette palette) {
        this.scale = scale;
        this.mode = mode;
        this.palette = palette;
    }

    public int getScale() {
        return scale;
    }

    public Mode getMode() {
        return mode;
    }

    public Palette getPalette() {
        return palette;
    }

    public Rendering withMode(Mode mode) {
        return new Rendering(scale, mode, palette);
    }

    public Rendering withPalette(Palette palette) {
        return new Rendering(scale, mode, palette);
    }

    public int getWidth() {
        return mode.width;
    }

    public int getHeight() {
        return mode.height;
    }

    public int getScaledWidth() {
        return mode.width * scale;
    }

    public int getScaledHeight() {
        return mode.height * scale;
    }

    public int getOriginX() {
        return mode.baseX;
    }

    public enum Mode {
        STANDARD(11, 16, 0),
        MASKED(11, 16, 0),
        NARROW(8, 16, 1);

        private final int width;
        private final int height;
        private final int baseX;

        Mode(int width, int height, int baseX) {
            this.width = width;
            this.height = height;
            this.baseX = baseX;
        }
    }
}
