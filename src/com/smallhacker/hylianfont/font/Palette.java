package com.smallhacker.hylianfont.font;

import javafx.scene.paint.Color;

import java.util.Arrays;

public final class Palette {
    private final String name;
    private final int bpp;
    private final Color[] palette;

    public Palette(String name, int... palette) {
        this.name = name;
        this.bpp = palette.length;
        this.palette = Arrays.stream(palette)
                .mapToObj(Palette::toColor)
                .toArray(Color[]::new);
    }

    public String getName() {
        return name;
    }

    public int getBpp() {
        return bpp;
    }

    public Color getColor(int index) {
        if (index < 0 || index >= bpp) {
            throw new IllegalArgumentException();
        }

        return palette[index];
    }

    private static Color toColor(int intColor) {
        return Color.rgb(
                (intColor >> 16) & 0xFF,
                (intColor >> 8) & 0xFF,
                intColor & 0xFF
        );
    }
}
