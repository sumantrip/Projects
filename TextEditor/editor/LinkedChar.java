package editor;
import java.util.LinkedList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LinkedChar extends LinkedList<CharNode>{

    public LinkedChar() {
    }

    public CharNode stringToNode(String s) {
        Text t = new Text(s);
        t.setFont(Font.font("Verdana", 12));
        CharNode ch = new CharNode(t, textToRectangle(t));
        return ch;
    }

    public CharNode textToNode(Text t) {
        t.setFont(Font.font("Verdana", 12));
        CharNode ch = new CharNode(t, textToRectangle(t));
        return ch;
    }

    public Rectangle textToRectangle(Text t) {
        double width = t.getLayoutBounds().getWidth();
        double height = t.getLayoutBounds().getHeight();
        return new Rectangle(width, height);
    }

    /**
     * Available methods from LinkedList:
     * - addFirst
     * - addLast
     * - add(ind, elem)
     * - clear
     * - clone
     * - contains(obj)
     * - getFirst
     * - getLast
     * - indexOf(obj)
     * - removeFirst
     * - removeLast
     * - size
     */
}