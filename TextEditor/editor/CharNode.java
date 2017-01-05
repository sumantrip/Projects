package editor;

import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Created by suman on 2/27/16.
 */
public class CharNode {
    public Text t;
    public Rectangle textBoundingBox;

    public CharNode(Text txt, Rectangle b) {
        textBoundingBox = b;
        t = txt;
    }

    public Text getText() {
        return t;
    }

    public Rectangle getRectangle() {
        return textBoundingBox;
    }

}

