package Model;

import Controller.OperatorBubbleController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Created by mmursith on 12/16/2015.
 */
public class OperatorBubble {
    private Parent root;

    public static void main(String [] args){

    }

    public OperatorBubble(String name, String  chatMessage, String time) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("operatorbubble.fxml"));
        root = fxmlLoader.load();

        OperatorBubbleController operatorBubbleController= fxmlLoader.<OperatorBubbleController>getController();
        operatorBubbleController.setOperatorname(name);
        operatorBubbleController.setOperatormessage(chatMessage);
        operatorBubbleController.setTime(time);

    }

    public Parent getRoot() {
        GridPane.setHalignment(root, HPos.RIGHT);

        return root;
    }
}