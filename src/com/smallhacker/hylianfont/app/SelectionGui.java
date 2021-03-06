package com.smallhacker.hylianfont.app;

import com.smallhacker.gui.Gui;
import com.smallhacker.gui.GuiCanvas;
import com.smallhacker.gui.Handlers;
import com.smallhacker.hylianfont.font.Font;
import com.smallhacker.hylianfont.font.Palette;
import com.smallhacker.hylianfont.font.Rendering;
import com.smallhacker.hylianfont.font.Tile;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.smallhacker.gui.GuiCanvas.leftClick;
import static com.smallhacker.gui.GuiCanvas.rightClick;
import static com.smallhacker.gui.Handlers.handlers;

final class SelectionGui extends Gui {
    private static final int WIDTH_IN_TILES = 32;
    private static final int HEIGHT_IN_TILES = (int)Math.ceil(Tile.TILE_COUNT / WIDTH_IN_TILES);

    private static final int MENU_HEIGHT = 25;

    private final GuiCanvas canvas;

    private final Handlers<Boolean> onFocusChange = handlers();
    private final Handlers<Tile> onTileSelect = handlers();
    private final Handlers<Tile> onTileCopy = handlers();
    private final Handlers<Path> onLoad = handlers();
    private final Handlers<Void> onSave = handlers();
    private final Handlers<ViewMode> onViewModeChange = handlers();
    private final Handlers<Palette> onPaletteChange = handlers();
    private final FilePicker fileChooser;

    private Font font;
    private MenuItem saveMenuItem;
    private Rendering rendering;

    SelectionGui(Stage stage, List<Palette> palettes, Rendering rendering) {
        super(stage, "Hylian Font 1.1.0", getWidth(rendering), getHeight(rendering) + MENU_HEIGHT);

        this.rendering = rendering;

        buildMenu(palettes);

        this.canvas = canvas(getWidth(rendering), getHeight(rendering));

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

    private static int getWidth(Rendering rendering) {
        return rendering.getScaledWidth() * WIDTH_IN_TILES;
    }

    private static int getHeight(Rendering rendering) {
        return rendering.getScaledHeight() * HEIGHT_IN_TILES;
    }

    public void setFont(Font font) {
        this.font = font;
        render();
        Tile tile = font.getTile(0);
        onTileSelect.invoke(tile);
        saveMenuItem.setDisable(false);
    }

    public Rendering getRendering() {
        return rendering;
    }

    public void setRendering(Rendering rendering) {
        this.rendering = rendering;
        render();
    }

    private void buildMenu(List<Palette> palettes) {
        Menu file = menu("File",
                menuItem("Load ROM", this::load),
                saveMenuItem = menuItem("Save ROM", this::save),
                menuItem("Exit", stage()::close)
        );

        List<MenuItem> viewModes = Stream.of(ViewMode.values())
                .map(viewMode -> menuItem(viewMode.getName(), () -> {
                    onViewModeChange.invoke(viewMode);
                }))
                .collect(Collectors.toList());
        Menu view = menu("View", viewModes);

        List<MenuItem> paletteItems = palettes.stream()
                .map(palette -> menuItem(palette.getName(), () -> {
                    onPaletteChange.invoke(palette);
                }))
                .collect(Collectors.toList());
        Menu paletteMenu = menu("Palettes", paletteItems);

        menuBar(file, view, paletteMenu);
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

    public void onViewModeChange(Consumer<ViewMode> handler) {
        onViewModeChange.add(handler);
    }

    public void onPaletteChange(Consumer<Palette> handler) {
        onPaletteChange.add(handler);
    }

    public void rerender(Tile tile) {
        // TODO: Optimize
        render();
    }

    private void render() {
        if (font != null) {
            canvas.render(image -> {
                int x = 0;
                int y = 0;
                int width = rendering.getScaledWidth();
                int height = rendering.getScaledHeight();
                for (Tile tile : font) {
                    tile.render(image, x * width, y * height, rendering);
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
    }


    private int getTileNumber(MouseEvent me) {
        int x = (int) me.getX() / rendering.getScaledWidth();
        int y = (int) me.getY() / rendering.getScaledHeight();
        return x + (y * 32);
    }
}
