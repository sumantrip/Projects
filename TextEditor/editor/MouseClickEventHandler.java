package editor;


import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A JavaFX application that illustrates how to handle mouse click events. Whenever the mouse is
 * clicked, the current position of the mouse is displayed on the screen.
 */
public class MouseClickEventHandler implements EventHandler<MouseEvent>  {
    private KeyEventHandler k;

    public MouseClickEventHandler(KeyEventHandler kH) {
        k = kH;
    }


    @Override
    public void handle(MouseEvent mouseEvent) {
        double mousePressedX = mouseEvent.getX();
        double mousePressedY = mouseEvent.getY();
        int newIndex = k.getCursor().XYtoIndex(mousePressedX, mousePressedY, k.getIndices(), k.getLetters());
        k.getCursor().setInd(newIndex);
        k.getCursor().fixCursorXY(newIndex, k.getLetters());

    }

}