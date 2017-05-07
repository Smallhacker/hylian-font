package com.smallhacker.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class Gui {
    private final Stage stage;
    private final Pane pane;

    public Gui(Stage stage, String title, int width, int height) {
        this.stage = stage;
        this.stage.setTitle(title);
        pane = new FlowPane();

        stage.setScene(new Scene(pane, width, height));
        stage.setResizable(false);
        stage.sizeToScene();
    }

    public final Stage stage() {
        return stage;
    }

    protected final GuiCanvas canvas(int width, int height) {
        return new GuiCanvas(pane, width, height);
    }

    protected final Button button() {
        Button button = new NoFocusButton();
        pane.getChildren().add(button);
        return button;
    }

    protected final MenuBar menuBar(Menu... menus) {
        MenuBar menuBar = new MenuBar(menus);
        pane.getChildren().add(menuBar);
        menuBar.prefWidthProperty().bind(stage.widthProperty());
        return menuBar;
    }


    private static final class NoFocusButton extends Button {
        @Override
        public void requestFocus() {
        }
    }

    protected FilePicker fileChooser(Consumer<Path> callback, String title, FileChooser.ExtensionFilter... extensions) {
        return new FilePicker(callback, title, extensions);
    }


    protected static FileChooser.ExtensionFilter ext(String description, String... extensions) {
        return new FileChooser.ExtensionFilter(description, extensions);
    }

    protected Menu menu(String name, MenuItem... items) {
        return menu(name, Arrays.asList(items));
    }

    protected Menu menu(String name, List<MenuItem> items) {
        Menu menu = new Menu(name);
        menu.getItems().addAll(items);
        return menu;
    }

    protected MenuItem menuItem(String text, Runnable action) {
        MenuItem load = new MenuItem(text);
        load.setOnAction(ae -> action.run());
        return load;
    }

    protected final class FilePicker implements Runnable {
        private final FileChooser fileChooser;
        private final Consumer<Path> callback;
        private File initial;

        private FilePicker(Consumer<Path> callback, String title, FileChooser.ExtensionFilter... extensions) {
            this.fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.getExtensionFilters().addAll(extensions);
            this.callback = callback;
        }


        @Override
        public void run() {
            if (initial != null) {
                fileChooser.setInitialDirectory(initial);
            }
            File f = fileChooser.showOpenDialog(stage());
            if (f != null) {
                Path file = f.toPath();
                callback.accept(file);
                initial = f.getParentFile();
            }
        }
    }
}
