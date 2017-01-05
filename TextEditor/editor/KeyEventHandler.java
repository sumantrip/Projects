package editor;

import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.io.*;
import java.util.ArrayList;

public class KeyEventHandler implements EventHandler<KeyEvent> {

        private final int WINDOW_SIZE = 500;
        private int windowWidth, windowHeight;
        private int fontSize = 12;
        private double charHeight;
        private String fontName = "Verdana";
        private LinkedChar letters;
        private Cursor c;
        private Group r, textRoot;
        private Scroll scroll;
        private Text temp = new Text("d");
        private String f;
        private UndoRedo ur;
        private ArrayList<Integer> indices = new ArrayList();

        public KeyEventHandler(final Group root, Cursor c, LinkedChar let, String fileName) {
            r = root;
            textRoot = new Group();
            r.getChildren().add(0, textRoot);
            this.c = c;
            scroll = new Scroll(root);
            windowHeight = WINDOW_SIZE;
            windowWidth = WINDOW_SIZE;
            temp.setFont(Font.font(fontName, fontSize));
            charHeight = temp.getLayoutBounds().getHeight();
            ur = new UndoRedo(this);
            f = fileName;
            letters = let;
            setLetters(changedPositions(letters));
            c.fixCursorXY(c.getInd(), letters);
            displayLetters();
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.isShortcutDown()) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.MINUS) {
                    fontSize = Math.max(0, fontSize - 4);
                    fixSize();
                } else if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                    fontSize += 4;
                    fixSize();
                } else if (code == KeyCode.P) {
                    cursorPos();
                } else if (code == KeyCode.Z) {
                    ur.undo();
                } else if (code == KeyCode.Y) {
                    ur.redo();
                } else if (code == KeyCode.S) {
                    File file = new File(f);
                    try {
                        FileWriter w = new FileWriter(file);
                        for(int i = 0; i < letters.size(); i++) {
                            String charac = letters.get(i).getText().getText();
                            w.write(charac);
                        }
                        w.close();
                    } catch (IOException ioException) {
                        System.out.println("Error when saving; exception was: " + ioException);
                    }
                }

            } else {
                if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                    addLetter(keyEvent.getCharacter(), c.getInd());
                    ur.clearRedoStack();
                    if (keyEvent.getCharacter().equals("")) {
                        ur.addToUndoStack(keyEvent, c.getInd());
                    } else {
                        ur.addToUndoStack(keyEvent, c.getInd() - 1);
                    }
                    keyEvent.consume();
                } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                    KeyCode code = keyEvent.getCode();
                    if (code == KeyCode.DELETE || code == KeyCode.BACK_SPACE) {
                        deleteLetter(c.getInd() - 1);
                        ur.clearRedoStack();
                        keyEvent.consume();
                    } else if (code == KeyCode.RIGHT) {
                        moveRight(keyEvent);
                    } else if (code == KeyCode.LEFT) {
                        moveLeft(keyEvent);
                    } else if (code == KeyCode.UP) {
                        moveUp(keyEvent);
                    } else if (code == KeyCode.DOWN) {
                        moveDown(keyEvent);
                    }
                }
            }
        }

        public void addLetter(String let, int index) {
            if (let.length() > 0 && let.charAt(0) != 8) {
                letters.add(index, letters.stringToNode(let));
                setLetters(changedPositions(letters));
                displayLetters();
                c.setInd(index + 1);
                c.fixCursorXY(c.getInd(), letters);
            }
        }

        public void deleteLetter(int index) {
            if (index >= 0) {
                letters.remove(index);
            }
            setLetters(changedPositions(letters));
            c.setInd(Math.max(0, index));
            c.fixCursorXY(c.getInd(), letters);
            displayLetters();
        }

        public void moveRight(KeyEvent keyEvent) {
            if (c.getInd() < letters.size() - 1) {
                c.setInd(c.getInd() + 1);
                c.fixCursorXY(c.getInd(), letters);
            }
            displayLetters();
            keyEvent.consume();
        }

        public void moveLeft(KeyEvent keyEvent) {
            if (c.getInd() != 0) {
                c.setInd(c.getInd() - 1);
                c.fixCursorXY(c.getInd(), letters);
            }
            displayLetters();
            keyEvent.consume();
        }

        public void moveUp(KeyEvent keyEvent) {
            if (c.getY() > 0) {
                c.setY(Math.round(c.getY() - charHeight/2));
                c.setInd(c.XYtoIndex(c.getX(), c.getY(), indices, letters));
                c.fixCursorXY(c.getInd(), letters);
            }
            displayLetters();
            keyEvent.consume();
        }

        public void moveDown(KeyEvent keyEvent) {
            int row = Math.min((int) (c.getY()/charHeight), indices.size() - 1);
            if (row < indices.size() - 1) {
                c.setY(Math.round(c.getY() + charHeight));
                c.setInd(c.XYtoIndex(c.getX(), c.getY(), indices, letters));
                c.fixCursorXY(c.getInd(), letters);
            }
            displayLetters();
            keyEvent.consume();
        }

        public void cursorPos() {
            System.out.println((int) c.getX() + ", " + (int) c.getY());
        }

        public void fixSize() {
            for (CharNode l : letters) {
                l.getText().setFont(Font.font(fontName, fontSize));
                l.getText().setTextOrigin(VPos.TOP);
            }
            c.setCursorHeight(fontSize);
            setLetters(changedPositions(letters));
            c.fixCursorXY(c.getInd(), letters);
            displayLetters();
        }

        public void displayLetters() {
            r.getChildren().clear();
            textRoot.getChildren().clear();
            for (CharNode l : letters) {
                Text displayText = (l.getText());
                displayText.setTextOrigin(VPos.TOP);
                displayText.setFont(Font.font(fontName, fontSize));
                textRoot.getChildren().add(displayText);
            }
            textRoot.getChildren().add(c.getShape());
            scroll.adjustHeight(c.getY(), letters, scroll);
            r.getChildren().add(scroll);
            r.getChildren().add(0, textRoot);
        }


        public LinkedChar changedPositions(LinkedChar lc) {
            indices.clear();
            int counter = 0;
            LinkedChar replace = new LinkedChar();
            double y = 0.0;
            double x;
            temp.setFont(Font.font(fontName, fontSize));
            charHeight = temp.getLayoutBounds().getHeight();

            while (!lc.isEmpty()) {
                // reset x to 5 everytime we go to a new line
                x = 5.0;
                c.setX(x);
                indices.add(counter);
                // continue until we reach end of line
                boolean newLine = false;
                while (x <= (windowWidth - 5 - scroll.getWidth()) && !lc.isEmpty()) {
                    Text temp = lc.removeFirst().getText();
                    temp.setFont(Font.font(fontName, fontSize));
                    if (temp.getText().equals("\r")) {
                        replace.add(new CharNode(new Text(x, y, temp.getText()), lc.textToRectangle(temp)));
                        counter++;
                        newLine = true;
                        break;
                    }
                    if (x + Math.round(temp.getLayoutBounds().getWidth()) < (windowWidth - 5 - scroll.getWidth())) {
                        replace.add(new CharNode(new Text(x, y, temp.getText()), lc.textToRectangle(temp)));
                        counter++;
                        x += Math.round(temp.getLayoutBounds().getWidth());
                        c.setX(x);
                    } else {
                        lc.addFirst(new CharNode(temp, lc.textToRectangle(temp)));
                        break;
                    }
                }

                if (lc.isEmpty()) {
                    break;
                } else if (newLine) {
                } else if (!lc.getFirst().getText().getText().equals(" ")) {
                    boolean hasSpace = false;
                    int position = replace.size() - 1;
                    int lettersInWord = 0;
                    while (position >= 0 && replace.get(position).getText().getY() == y) {
                        if (replace.get(position).getText().getText().equals(" ")) {
                            hasSpace = true;
                            break;
                        }
                        position--;
                        lettersInWord++;
                    }

                    if (hasSpace) {
                        while (lettersInWord > 0) {
                            Text lastOfAboveLine = replace.removeLast().getText();
                            lastOfAboveLine.setY(y + charHeight);
                            lc.addFirst(new CharNode(lastOfAboveLine, letters.textToRectangle(lastOfAboveLine)));
                            lettersInWord--;
                        }
                    }
                }
                y += temp.getLayoutBounds().getHeight();
                c.setY(y);
            }
            return replace;
        }

    public Scroll getScroll() {
        return scroll;
    }

    public void setWindowWidth(int width) {
        windowWidth = width;
    }

    public void setWindowHeight(int height) {
        windowHeight = height;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    public LinkedChar getLetters() {
        return letters;
    }

    public Cursor getCursor() {
        return c;
    }

    public void setLetters(LinkedChar replace) {
        letters = replace;
    }

    public Group getRoot() {
        return r;
    }
}