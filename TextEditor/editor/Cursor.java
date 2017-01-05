package editor;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;


public class Cursor {
    private int colorInd, cursorInd;
    private final Rectangle shape;
    private int fontSize = 12;
    private String fontName = "Verdana";
    private final Color[] boxColors = {Color.BLACK, Color.TRANSPARENT};
    private Text t = new Text();


    public Cursor() {
        t.setFont(Font.font(fontName, fontSize));
        shape = new Rectangle(1, t.getLayoutBounds().getHeight());
        shape.setX(5.0);
        shape.setY(0.0);
        colorInd = 0;
        cursorInd = 0;
    }

    public void setInd(int i) {
        cursorInd = i;
    }

    public int getInd() {
        return cursorInd;
    }

    public int XYtoIndex(double x, double y, ArrayList<Integer> indices, LinkedChar letters) {
        int row, lastIndexInRow;
        if (indices.size() > 0) {
            row = Math.min((int) (y / t.getLayoutBounds().getHeight()), indices.size() - 1);
        } else {
            return 0;
        }

        int firstIndexInRow = indices.get(row);
        if (y > letters.get(firstIndexInRow).getText().getY() + t.getLayoutBounds().getHeight()) {
            return letters.size();
        }

        int properIndex = firstIndexInRow;
        if (row < indices.size() - 1) {
            lastIndexInRow = indices.get(row + 1) - 2;
        } else {
            lastIndexInRow = letters.size() - 1;
        }

        if (x > letters.get(lastIndexInRow).getText().getX()) {
            return lastIndexInRow + 1;
        } else if (x > 5) {
            int lessThanX = firstIndexInRow;
            int moreThanX = firstIndexInRow + 1;
            for (int ind = firstIndexInRow; ind < lastIndexInRow; ind++) {
                if (letters.get(moreThanX).getText().getX() >= x && letters.get(lessThanX).getText().getX() <= x) {
                    double distLess = x - letters.get(lessThanX).getText().getX();
                    double distMore = letters.get(moreThanX).getText().getX() - x;
                    if (distLess < distMore) {
                        properIndex = lessThanX;
                    } else {
                        properIndex = moreThanX;
                    }
                } else {
                    lessThanX++;
                    moreThanX++;
                }
            }
        }

        return properIndex;
    }

    public void changeColor() {
        shape.setFill(boxColors[colorInd]);
        colorInd = 1 - colorInd;
    }

    public void setCursorHeight(int size) {
        fontSize = size;
        t.setFont(Font.font(fontName, fontSize));
        shape.setHeight(t.getLayoutBounds().getHeight());
    }

    public double getX() {
        return shape.getX();
    }

    public double getY() {
        return shape.getY();
    }

    public void setX(double x) {
        shape.setX(x);
    }

    public void setY(double y) {
        shape.setY(y);
    }

    public Rectangle getShape() {
        return shape;
    }

    public void fixCursorXY(int cursorIndex, LinkedChar letters) {
        double x, y;
        if (cursorIndex == 0) {
            x = 5.0;
            y = 0;
        } else if (cursorIndex >= letters.size()) {
            x = letters.get(letters.size() - 1).getText().getX() + letters.get(letters.size() - 1).getText().getLayoutBounds().getWidth();
            y = letters.get(letters.size() - 1).getText().getY();
        } else {
            x = letters.get(cursorIndex).getText().getX();
            y = letters.get(cursorIndex).getText().getY();
        }
        setX(Math.round(x));
        setY(Math.round(y));
    }
}