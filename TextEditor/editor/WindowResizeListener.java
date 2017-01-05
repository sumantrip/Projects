package editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.security.Key;


public class WindowResizeListener {

    private int newWidth, newHeight;
    private final Scene scene;
    private final KeyEventHandler kH;

    public WindowResizeListener(Scene s, KeyEventHandler k) {

        scene = s;
        kH = k;

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                newWidth = newScreenWidth.intValue();
                kH.setWindowWidth(newWidth);
                double usableScreenWidth = newWidth - kH.getScroll().getLayoutBounds().getWidth();
                kH.getScroll().setLayoutX(usableScreenWidth);
                kH.setLetters(kH.changedPositions(kH.getLetters()));
                kH.displayLetters();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                newHeight = newScreenHeight.intValue();
                kH.setWindowHeight(newHeight);
                kH.getScroll().setPrefHeight(newHeight);
                kH.setLetters(kH.changedPositions(kH.getLetters()));
                kH.displayLetters();
            }
        });
    }

}