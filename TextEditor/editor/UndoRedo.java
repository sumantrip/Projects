package editor;
import java.util.Stack;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Created by suman on 3/6/16.
 */
public class UndoRedo {

    private Stack<KeyEvent> u = new Stack();
    private Stack<KeyEvent> r = new Stack();
    private KeyEventHandler key;
    private Stack<Integer> uIndices = new Stack();
    private Stack<Integer> rIndices = new Stack();

    public UndoRedo(KeyEventHandler kH) {
        key = kH;
    }

    public void undo() {
        if (u.isEmpty()) {
            return;
        }
        KeyEvent topU = u.peek();
        if (!topU.getCharacter().equals("")) {
            key.deleteLetter(uIndices.peek());
            addToRedoStack(u.pop(), uIndices.pop());
        } else {
            int i = 1;
            while (u.get(u.size()-i).getCharacter().equals("") || !(uIndices.get(u.size()-i) == uIndices.peek())) {
                i++;
            }
            key.addLetter(u.get(u.size()-i).getCharacter(), uIndices.peek());
            addToRedoStack(u.pop(), uIndices.pop());
        }
    }

    public void redo() {
        if (r.isEmpty()) {
            return;
        }
        KeyEvent topR = r.peek();
        if (!topR.getCharacter().equals("")) {
            key.addLetter(topR.getCharacter(), rIndices.peek());
            addToUndoStack(r.pop(), rIndices.pop());
        } else {
            key.deleteLetter(rIndices.peek());
            addToUndoStack(r.pop(), rIndices.pop());
            }
        }


    public void addToUndoStack(KeyEvent k, int ind) {
        if (u.size() == 100) {
            u.removeElementAt(0);
            uIndices.removeElementAt(0);
        }
        u.push(k);
        uIndices.push(ind);
    }

    public void addToRedoStack(KeyEvent k, int ind) {
        if (r.size() == 100) {
            r.removeElementAt(0);
            rIndices.removeElementAt(0);
        }
        r.push(k);
        rIndices.push(ind);
    }

    public void clearRedoStack() {
        r.clear();
        rIndices.clear();
    }
}
