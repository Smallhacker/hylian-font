package com.smallhacker.hylianfont.app;

import com.smallhacker.gui.Gui;
import com.smallhacker.gui.GuiCanvas;
import com.smallhacker.gui.Handlers;
import com.smallhacker.hylianfont.font.Palette;
import com.smallhacker.hylianfont.font.Tile;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

import static com.smallhacker.gui.Handlers.handlers;

final class TileGui extends Gui {
    private static final int ZOOM = 16;
    private static final int WIDTH = Tile.WIDTH * ZOOM;
    private static final int HEIGHT = Tile.HEIGHT * ZOOM;
    private static final int PIXELS_TO_HIDE = 2 * ZOOM;
    private static final int BUTTON_SIZE = 24;

    private final Palette palette;
    private final GuiCanvas canvas;
    private final Button[] colorButtons = new Button[4];

    private Tile currentTile;
    private byte color;
    private final Handlers<Tile> onTileUpdate = handlers();

    TileGui(Stage stage, Palette palette) {
        super(stage, "Tile Editor", WIDTH, HEIGHT - PIXELS_TO_HIDE + BUTTON_SIZE);
        stage.initStyle(StageStyle.UTILITY);

        this.palette = palette;
        this.canvas = canvas(WIDTH, HEIGHT - PIXELS_TO_HIDE);
        for (byte i = 0; i < 4; i++) {
            byte color = i;
            Button button = colorButton(i);
            button.setOnMouseClicked(me -> setColor(color));
            colorButtons[i] = button;
        }

        setColor((byte) 0);

        canvas.onMousePressed(this::onMouse);
        canvas.onMouseDragged(this::onMouse);


    }

    private void onMouse(MouseEvent me) {
        if (me.isPrimaryButtonDown()) {
            plot(me);
        } else if (me.isSecondaryButtonDown()) {
            pick(me);
        }
    }

    private Button colorButton(int id) {
        Button button = button();
        button.setGraphic(new Rectangle(16, 16, palette.getColor(id)));
        button.resize(BUTTON_SIZE, BUTTON_SIZE);
        return button;
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
            if (index < 2 * Tile.WIDTH) {
                return;
            }
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
        int x = ((int) me.getX()) / ZOOM;
        int y = ((int) me.getY() + PIXELS_TO_HIDE) / ZOOM;
        return Tile.coordsToIndex(x, y);
    }


    public void setTile(Tile tile) {
        currentTile = tile;
        render();
    }

    private void render() {
        canvas.render(image -> {
            currentTile.render(image, palette, 0, -PIXELS_TO_HIDE, ZOOM);
        });
    }

    public void onTileUpdate(Consumer<Tile> handler) {
        onTileUpdate.add(handler);
    }

}
