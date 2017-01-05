package editor;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;


public class Scroll extends ScrollBar{
    private int windowWidth = 500;
    private int windowHeight = 500;
    private final Group textRoot;
    private double scrollChange;

    public Scroll(Group root) {
        setOrientation(Orientation.VERTICAL);
        setPrefHeight(windowHeight);
        double usableScreenWidth = windowWidth - getLayoutBounds().getWidth();
        setLayoutX(usableScreenWidth);
        textRoot = (Group) root.getChildren().get(0);

        valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                scrollChange = newValue.doubleValue() - oldValue.doubleValue();
                textRoot.setLayoutY(-(newValue.intValue()));
            }
        });
    }

    public void adjustHeight(double cursorY, LinkedChar lc, Scroll s) {
        if (!lc.isEmpty()) {
            if (lc.getLast().getText().getY() < windowHeight) {
                s.setMax(0);
            } else {
                s.setMax(windowHeight);
                if (cursorY > windowHeight) {
                    textRoot.setLayoutY(-(cursorY - windowHeight));
                    scrollChange = 0;
                } else if (cursorY <= textRoot.getLayoutY()) {
                    textRoot.setLayoutY(-cursorY);
                    scrollChange = 0;
                }
            }
        }
    }
}