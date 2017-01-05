package editor;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import java.io.*;
import javafx.scene.text.Text;





public class Editor extends Application {
    private static final int WINDOW_SIZE = 500;
    private static final Cursor cursor = new Cursor();
    private static String f;
    private static LinkedChar chars;

    public void makeCursorBlink() {
        final Timeline timeline = new Timeline();
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorBlink = new CursorBlinkEventHandler(cursor);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorBlink);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void start(Stage primaryStage) {
        final Group root = new Group();
        int windowWidth = WINDOW_SIZE;
        int windowHeight = WINDOW_SIZE;
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);
        EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler(root, cursor, chars, f);
        Scroll scroll = new Scroll(root);
        EventHandler<MouseEvent> mouseClick = new MouseClickEventHandler((KeyEventHandler) keyEventHandler);
        WindowResizeListener wrl = new WindowResizeListener(scene, (KeyEventHandler) keyEventHandler);

        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(mouseClick);


        root.getChildren().add(cursor.getShape());
        makeCursorBlink();
        root.getChildren().add(scroll);


        primaryStage.setTitle("SUMAN'S TEXT EDITOR");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Expected usage: Editor <source filename> ");
            System.exit(1);
        }
        f = args[0];
        if (args.length == 2) {
            String arg2 = args[1];
            if (arg2.equals("debug")) {
                System.out.println("Begin debugging.");
            }
        }
        chars = new LinkedChar();
        try {
            File file = new File(f);
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                int num = -1;
                int x = 0;
                while ((num = br.read()) != -1) {
                    char charRead = (char) num;
                    Text txt = new Text(Character.toString(charRead));
                    CharNode temp = new CharNode(txt, chars.textToRectangle(txt));
                    chars.add(x,temp);
                    x++;
                }
                cursor.setInd(x);
                br.close();
            }
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
        launch(args);
    }
}