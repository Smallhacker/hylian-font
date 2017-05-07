package com.smallhacker.hylianfont.app;

import com.smallhacker.gui.Gui;
import com.smallhacker.gui.GuiCanvas;
import com.smallhacker.gui.Handlers;
import com.smallhacker.hylianfont.font.Rendering;
import com.smallhacker.hylianfont.font.Tile;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

import static com.smallhacker.gui.Handlers.handlers;

final class TileGui extends Gui {
    private static final int TILE_LINES_TO_HIDE = 2;
    private static final int BUTTON_SIZE = 24;

    private final GuiCanvas canvas;
    private final Button[] colorButtons = new Button[4];
    private final Handlers<Tile> onTileUpdate = handlers();

    private Tile currentTile;
    private byte color;
    private Rendering rendering;

    TileGui(Stage stage, Rendering rendering) {
        super(stage, "Tile Editor", tileWidth(rendering), tileHeight(rendering) + BUTTON_SIZE);
        stage.initStyle(StageStyle.UTILITY);

        this.rendering = rendering;
        this.canvas = canvas(tileWidth(rendering), tileHeight(rendering));
        for (byte i = 0; i < 4; i++) {
            byte color = i;
            Button button = colorButton();
            button.setOnMouseClicked(me -> setColor(color));
            colorButtons[i] = button;
        }
        updateButtonColors();

        setColor((byte) 0);

        canvas.onMousePressed(this::onMouse);
        canvas.onMouseDragged(this::onMouse);
    }

    private static int tileWidth(Rendering rendering) {
        return rendering.getScaledWidth();
    }

    private static int tileHeight(Rendering rendering) {
        return rendering.getScaledHeight() - (TILE_LINES_TO_HIDE * rendering.getScale());
    }

    private void onMouse(MouseEvent me) {
        if (me.isPrimaryButtonDown()) {
            plot(me);
        } else if (me.isSecondaryButtonDown()) {
            pick(me);
        }
    }

    private Button colorButton() {
        Button button = button();
        button.resize(BUTTON_SIZE, BUTTON_SIZE);
        return button;
    }

    private void updateButtonColors() {
        for (int i = 0; i < colorButtons.length; i++) {
            Button button = colorButtons[i];
            Color color = rendering.getPalette().getColor(i);
            button.setGraphic(new Rectangle(16, 16, color));
        }
    }

    private void setColor(byte color) {
        this.color = color;
        for (int i = 0; i < 4; i++) {
            colorButtons[i].setDisable(i == color);
        }
    }

    private void plot(MouseEvent me) {
        if (currentTile != null) {
            int index = toIndex(me);
            if (currentTile.set(index, color)) {
                render();
                onTileUpdate.invoke(currentTile);
            }
        }
    }

    private void pick(MouseEvent me) {
        if (currentTile != null) {
            int index = toIndex(me);
            setColor(currentTile.get(index));
        }
    }

    private int toIndex(MouseEvent me) {
        int scale = rendering.getScale();
        int offsetY = TILE_LINES_TO_HIDE * scale;
        int x = ((int) me.getX()) / scale;
        int y = ((int) me.getY() + offsetY) / scale;
        return Tile.coordsToIndex(x, y);
    }

    public void setTile(Tile tile) {
        currentTile = tile;
        render();
    }

    public Rendering getRendering() {
        return rendering;
    }

    public void setRendering(Rendering rendering) {
        if (this.rendering.getScaledWidth() != rendering.getScaledWidth() || this.rendering.getScaledHeight() != rendering.getScaledHeight()) {
            throw new IllegalArgumentException("Scaled size cannot change.");
        }
        this.rendering = rendering;
        render();
        updateButtonColors();
    }

    private void render() {
        if (currentTile != null) {
            canvas.render(image -> {
                int offsetY = -TILE_LINES_TO_HIDE * rendering.getScale();
                currentTile.render(image, 0, offsetY, rendering);
            });
        }
    }

    public void onTileUpdate(Consumer<Tile> handler) {
        onTileUpdate.add(handler);
    }

}
