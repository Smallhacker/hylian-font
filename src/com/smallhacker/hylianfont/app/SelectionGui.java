package com.smallhacker.hylianfont.app;

import com.smallhacker.gui.Gui;
import com.smallhacker.gui.GuiCanvas;
import com.smallhacker.gui.Handlers;
import com.smallhacker.hylianfont.font.Font;
import com.smallhacker.hylianfont.font.Palette;
import com.smallhacker.hylianfont.font.Tile;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.function.Consumer;

import static com.smallhacker.gui.GuiCanvas.leftClick;
import static com.smallhacker.gui.GuiCanvas.rightClick;
import static com.smallhacker.gui.Handlers.handlers;

final class SelectionGui extends Gui {
    private static final int ZOOM = 2;

    private static final int WIDTH_IN_TILES = 32;
    private static final int HEIGHT_IN_TILES = 16;

    private static final int WIDTH_IN_PIXELS = Tile.WIDTH * ZOOM * WIDTH_IN_TILES;
    private static final int HEIGHT_IN_PIXELS = Tile.HEIGHT * ZOOM * HEIGHT_IN_TILES;

    private static final int MENU_HEIGHT = 25;

    private final Palette palette;
    private final GuiCanvas canvas;

    private final Handlers<Boolean> onFocusChange = handlers();
    private final Handlers<Tile> onTileSelect = handlers();
    private final Handlers<Tile> onTileCopy = handlers();
    private final Handlers<Path> onLoad = handlers();
    private final Handlers<Void> onSave = handlers();
    private final FilePicker fileChooser;

    private Font font;
    private MenuItem saveMenuItem;

    SelectionGui(Stage stage, Palette palette) {
        super(stage, "Hylian Font 1.0", WIDTH_IN_PIXELS, HEIGHT_IN_PIXELS + MENU_HEIGHT);

        this.palette = palette;

        buildMenu();

        this.canvas = canvas(WIDTH_IN_PIXELS, HEIGHT_IN_PIXELS);

        canvas.onMousePressed(
                leftClick(me -> {
                    if (font != null) {
                        int index = getTileNumber(me);
                        Tile tile = font.getTile(index);
                        onTileSelect.invoke(tile);
                    }
                })
        );

        canvas.onMousePressed(
                rightClick(me -> {
                    if (font != null) {
                        int index = getTileNumber(me);
                        Tile tile = font.getTile(index);
                        onTileCopy.invoke(tile);
                    }
                })
        );

        stage.focusedProperty().addListener(
                (observable, oldValue, newValue) -> onFocusChange.invoke(newValue)
        );

        this.fileChooser = fileChooser(onLoad::invoke, "Load ROM", ext("SNES ROM", "*.sfc", "*.smc"));
    }

    public void setFont(Font font) {
        this.font = font;
        render();
        Tile tile = font.getTile(0);
        onTileSelect.invoke(tile);
        saveMenuItem.setDisable(false);
    }

    private void buildMenu() {
        menuBar(
                menu("File",
                        menuItem("Load ROM", this::load),
                        saveMenuItem = menuItem("Save ROM", this::save),
                        menuItem("Exit", stage()::close)
                )
        );
        saveMenuItem.setDisable(true);
    }

    private void load() {
        fileChooser.run();
    }

    private void save() {
        onSave.invoke(null);
    }

    public void onFocusChange(Consumer<Boolean> handler) {
        onFocusChange.add(handler);
    }

    public void onTileSelect(Consumer<Tile> handler) {
        onTileSelect.add(handler);
    }

    public void onTileCopy(Consumer<Tile> handler) {
        onTileCopy.add(handler);
    }

    public void onLoad(Consumer<Path> handler) {
        onLoad.add(handler);
    }

    public void onSave(Runnable handler) {
        onSave.add(x -> handler.run());
    }

    public void rerender(Tile tile) {
        // TODO: Optimize
        render();
    }

    private void render() {
        canvas.render(image -> {
            int x = 0;
            int y = 0;
            for (Tile tile : font) {
                tile.render(image, palette, x * Tile.WIDTH * ZOOM, y * Tile.HEIGHT * ZOOM, ZOOM);
                x++;
                if (x >= WIDTH_IN_TILES) {
                    x = 0;
                    y++;
                    if (y >= HEIGHT_IN_TILES) {
                        break;
                    }
                }
            }
        });
    }


    private int getTileNumber(MouseEvent me) {
        int x = (int) me.getX() / ZOOM;
        int y = (int) me.getY() / ZOOM;
        x /= Tile.WIDTH;
        y /= Tile.HEIGHT;
        return x + (y * 32);
    }
}
