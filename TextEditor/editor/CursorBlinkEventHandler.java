package editor;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;


public class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
    private Cursor c;

    // Set the color to be the first color in the list.
    public CursorBlinkEventHandler(Cursor c) {
        this.c = c;
        this.c.changeColor();
    }

    @Override
    public void handle(ActionEvent event) {
        c.changeColor();
    }

}

