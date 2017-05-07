package com.smallhacker.hylianfont.app;

import com.smallhacker.gui.Gui;
import com.smallhacker.hylianfont.font.Font;
import com.smallhacker.hylianfont.font.Palette;
import com.smallhacker.hylianfont.font.Rendering;
import com.smallhacker.hylianfont.font.Tile;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Path;

public final class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Path currentFile;
    private Font currentFont;
    private Tile currentTile;

    @Override
    public void start(Stage primaryStage) {
        ViewMode defaultViewMode = ViewMode.WIDE_MODE;
        Palette palette = new Palette(0x008888, 0x000073, 0xFFFFFF, 0xC60000);

        Stage tileStage = new Stage();
        tileStage.initOwner(primaryStage);
        TileGui tileGui = new TileGui(tileStage, new Rendering(16, defaultViewMode.getTileMode(), palette));
        SelectionGui selectionGui = new SelectionGui(primaryStage, new Rendering(2, defaultViewMode.getSelectionMode(), palette));

        selectionGui.onLoad(path -> {
            try {
                Font font = Font.load(path);
                selectionGui.setFont(font);
                currentFont = font;
                currentFile = path;
            } catch (MessageException e) {
                error(e.getMessage());
            }

        });

        selectionGui.onSave(() -> {
            try {
                if (currentFile != null) {
                    currentFont.save(currentFile);
                    information("Changes saved to " + currentFile);
                }
            } catch (MessageException e) {
                error(e.getMessage());
            }
        });

        selectionGui.onTileSelect(tile -> {
            if (!tileGui.stage().isShowing()) {
                snapTopRight(selectionGui, tileGui);
                tileGui.stage().show();
            }
            tileGui.setTile(tile);
            currentTile = tile;
        });

        selectionGui.onTileCopy(tile -> {
            if (currentTile != null) {
                tile.copy(currentTile);
                selectionGui.rerender(tile);
            }
        });

        selectionGui.onViewModeChange(viewMode -> {
            tileGui.setRendering(tileGui.getRendering().withMode(viewMode.getTileMode()));
            selectionGui.setRendering(selectionGui.getRendering().withMode(viewMode.getSelectionMode()));
        });
        tileGui.onTileUpdate(selectionGui::rerender);
        selectionGui.onFocusChange(focus -> tileGui.stage().setAlwaysOnTop(focus));

        InputStream icon = App.class.getResourceAsStream("icon.png");
        primaryStage.getIcons().add(new Image(icon));

        selectionGui.stage().show();
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void information(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void snapTopRight(Gui source, Gui target) {
        Stage sorceStage = source.stage();
        Stage targetStage = target.stage();

        targetStage.setX(sorceStage.getX() + sorceStage.getWidth() + 20);
        targetStage.setY(sorceStage.getY());
    }

}
