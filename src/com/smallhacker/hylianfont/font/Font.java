package com.smallhacker.hylianfont.font;

import com.smallhacker.hylianfont.app.MessageException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;

import static java.lang.System.arraycopy;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public final class Font implements Iterable<Tile> {
    private static final int START_OF_TILE_DATA = 0x70000;
    private static final int START_OF_TILE_BITMASK = 0x73844;
    private static final int MAX_TILE_DATA_LENGTH = 0x3844;

    private final List<Tile> tiles;

    private Font(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public int size() {
        return tiles.size();
    }

    public static Font load(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            List<Tile> tiles = Tile.load(bytes, START_OF_TILE_DATA, START_OF_TILE_BITMASK);
            return new Font(tiles);
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new MessageException("Failed to load file.", e);
        }
    }

    public void save(Path filePath) {
        List<Tile.Output> encodedTiles = tiles.stream()
                .map(Tile::encode)
                .collect(toList());

        byte[] tileData = encodedTiles.stream()
                .map(Tile.Output::getTileData)
                .collect(concatArrays());

        byte[] bitmasks = encodedTiles.stream()
                .map(Tile.Output::getBitmask)
                .collect(concatArrays());

        if (tileData.length > MAX_TILE_DATA_LENGTH) {
            throw new MessageException(
                    String.format(
                            "Tile data is too large. (%s bytes, %s available)",
                            tileData.length,
                            MAX_TILE_DATA_LENGTH
                    )
            );
        }


        File file = filePath.toFile();
        if (!file.exists()) {
            throw new MessageException("File does not exist.");
        }

        try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
            out.seek(START_OF_TILE_DATA);
            out.write(tileData);
            out.seek(START_OF_TILE_BITMASK);
            out.write(bitmasks);
        } catch (IOException e) {
            throw new MessageException("Saving failed.");
        }
    }

    @Override
    public Iterator<Tile> iterator() {
        return tiles.iterator();
    }

    private static Collector<byte[], ?, byte[]> concatArrays() {
        return collectingAndThen(toList(), Font::concat);
    }

    private static byte[] concat(List<byte[]> arrays) {
        int totalLength = arrays.stream().mapToInt(i -> i.length).sum();
        byte[] joined = new byte[totalLength];
        int i = 0;
        for (byte[] array : arrays) {
            arraycopy(array, 0, joined, i, array.length);
            i += array.length;
        }
        return joined;
    }
}
