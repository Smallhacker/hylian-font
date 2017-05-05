package com.smallhacker.hylianfont.font;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.List;

public final class Tile {
    public static final int WIDTH = 11;
    public static final int HEIGHT = 16;
    private static final int TILE_COUNT = 512;

    private final int index;
    private final byte[] pixels;

    private Tile(int index, byte[] pixels) {
        this.index = index;
        this.pixels = pixels;
    }

    public int index() {
        return index;
    }

    public byte get(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
            throw new IllegalArgumentException();
        }
        return pixels[coordsToIndex(x, y)];
    }

    public byte get(int index) {
        if (index < 0 || index >= WIDTH * HEIGHT) {
            throw new IllegalArgumentException();
        }
        return pixels[index];
    }

    public boolean set(int x, int y, byte color) {
        if (color < 0 || x < 0 || y < 0 || color >= 4 || x >= WIDTH || y >= HEIGHT) {
            throw new IllegalArgumentException();
        }
        int index = coordsToIndex(x, y);
        byte oldColor = pixels[index];
        pixels[index] = color;
        return oldColor != color;
    }

    public boolean set(int index, byte color) {
        if (color < 0 || index < 0 || color >= 4 || index >= WIDTH * HEIGHT) {
            throw new IllegalArgumentException();
        }
        byte oldColor = pixels[index];
        pixels[index] = color;
        return oldColor != color;
    }

    public void copy(Tile source) {
        System.arraycopy(source.pixels, 0, pixels, 0, pixels.length);
    }

    public void render(WritableImage img, Palette palette, int baseX, int baseY, int scale) {
        PixelWriter writer = img.getPixelWriter();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int paletteIndex = pixels[coordsToIndex(x, y)];
                for (int x2 = 0; x2 < scale; x2++) {
                    int px = x * scale + baseX + x2;
                    if (px < 0 || px >= img.getWidth()) {
                        continue;
                    }
                    for (int y2 = 0; y2 < scale; y2++) {
                        int py = y * scale + baseY + y2;
                        if (py < 0 || py >= img.getHeight()) {
                            continue;
                        }
                        writer.setColor(px, py, palette.getColor(paletteIndex));
                    }
                }
            }
        }
    }

    static List<Tile> load(byte[] file, int startOfTileData, int startOfTileBitmask) {
        int tilePointer = startOfTileData;
        int bitmaskPointer = startOfTileBitmask;

        List<Tile> tiles = new ArrayList<>();

        for (int tile = 0; tile < TILE_COUNT; tile++) {
            long bitmask = 0;
            for (int i = 0; i < 5; i++) {
                bitmask <<= 8;
                bitmask |= Byte.toUnsignedInt(file[bitmaskPointer++]);
            }

            byte[] packed = new byte[44];

            for (int i = 0; i < 40; i++) {
                if ((bitmask & 0x80_0000_0000L) != 0) {
                    packed[i + 4] = file[tilePointer++];
                }
                bitmask <<= 1;
            }

            byte[] unpacked = unpack(packed);

            tiles.add(new Tile(tile, unpacked));
        }
        return tiles;
    }

    public Output encode() {
        byte[] packed = pack();
        byte[] filtered = new byte[packed.length];
        int bytes = 0;
        long bitmask = 0;
        for (int i = 4; i < packed.length; i++) {
            byte b = packed[i];
            bitmask <<= 1;
            if (b != 0) {
                filtered[bytes++] = b;
                bitmask |= 1;
            }
        }

        byte[] bitmaskBytes = new byte[5];

        for (int i = 0; i < 5; i++) {
            bitmaskBytes[i] = (byte)((bitmask >> 32) & 0xFF);
            bitmask <<= 8;
        }

        byte[] shrunk = new byte[bytes];

        System.arraycopy(filtered, 0, shrunk, 0, bytes);

        return new Output(shrunk, bitmaskBytes);
    }


    private static byte[] unpack(byte[] packed) {
        final int LENGTH = WIDTH * HEIGHT / 8;

        byte[] unpacked = new byte[WIDTH * HEIGHT];
        for (int i = 0; i < LENGTH; i++) {
            byte low = packed[i * 2];
            byte high = packed[i * 2 + 1];
            for (int bit = 0; bit < 8; bit++) {
                byte pixel = (byte) (((low >> 7) & 1) | ((high >> 6) & 2));

                int index = toCoords(i, bit);

                unpacked[index] = pixel;

                low <<= 1;
                high <<= 1;
            }
        }
        return unpacked;
    }

    private byte[] pack() {
        byte[] packed = new byte[44];
        for (byte i = 0; i < 44; i += 2) {
            byte low = 0;
            byte high = 0;

            byte x;
            byte y;

            byte j = (byte)(i / 2);
            if (j < 11) {
                if (j < 8) {
                    x = 0;
                    y = j;
                } else {
                    x = j;
                    y = 0;
                }
            } else {
                if (j < 19) {
                    x = 0;
                    y = (byte)(j - 3);
                } else {
                    x = (byte)(j - 11);
                    y = 8;
                }
            }

            byte dx = (byte)(x == 0 ? 1 : 0);
            byte dy = (byte)(1 - dx);

            for (int bit = 0; bit < 8; bit++) {
                low <<= 1;
                high <<= 1;
                int val = get(x, y);
                low |= (byte)(val & 1);
                val >>= 1;
                high |= (byte)(val & 1);

                x += dx;
                y += dy;

            }
            packed[i] = low;
            packed[i + 1] = high;

        }

        return packed;
    }

    private static int toCoords(int row, int bit) {
        if (row < 8) {
            return coordsToIndex(bit, row);
        } else if (row < 11) {
            return coordsToIndex(row, bit);
        } else if (row < 19) {
            return coordsToIndex(bit, row - 3);
        } else {
            return coordsToIndex(row - 19 + 8, bit + 8);
        }
    }

    public static int coordsToIndex(int x, int y) {
        return x + y * WIDTH;
    }

    public static final class Output {
        private final byte[] tileData;
        private final byte[] bitmask;

        private Output(byte[] tileData, byte[] bitmask) {
            this.tileData = tileData;
            this.bitmask = bitmask;
        }

        public byte[] getTileData() {
            return tileData;
        }

        public byte[] getBitmask() {
            return bitmask;
        }
    }
}
