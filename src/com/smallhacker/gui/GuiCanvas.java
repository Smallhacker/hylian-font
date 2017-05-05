package com.smallhacker.gui;

import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

import static com.smallhacker.gui.Handlers.handlers;

public final class GuiCanvas {
    private final int width;
    private final int height;
    private final ImageView view;

    private final Handlers<MouseEvent> onMousePressed = handlers();
    private final Handlers<MouseEvent> onMouseReleased = handlers();
    private final Handlers<MouseEvent> onMouseDragged = handlers();

    GuiCanvas(Pane root, int width, int height) {
        this.width = width;
        this.height = height;

        this.view = new ImageView();
        view.resize(width, height);
        root.getChildren().add(view);

        view.setOnMousePressed(capped(onMousePressed));
        view.setOnMouseReleased(capped(onMouseReleased));
        view.setOnMouseDragged(capped(onMouseDragged));

        render(img -> {});
    }


    private EventHandler<MouseEvent> capped(Handlers<MouseEvent> handler) {
        return me -> {
            if (me.getX() >= 0 && me.getY() >= 0 && me.getX() < width && me.getY() < height) {
                handler.invoke(me);
            }
        };
    }

    public static Consumer<MouseEvent> leftClick(EventHandler<MouseEvent> handler) {
        return me -> {
            if (me.isPrimaryButtonDown()) {
                handler.handle(me);
            }
        };
    }

    public static Consumer<MouseEvent> rightClick(EventHandler<MouseEvent> handler) {
        return me -> {
            if (me.isSecondaryButtonDown()) {
                handler.handle(me);
            }
        };
    }

    public void setImage(Image image) {
        view.setImage(image);
    }

    public void render(Consumer<WritableImage> renderer) {
        WritableImage image = new WritableImage(width, height);
        renderer.accept(image);
        setImage(image);
    }

    public void onMousePressed(Consumer<MouseEvent> event) {
        onMousePressed.add(event);
    }

    public void onMouseReleased(Consumer<MouseEvent> event) {
        onMouseReleased.add(event);
    }

    public void onMouseDragged(Consumer<MouseEvent> event) {
        onMouseDragged.add(event);
    }
}
