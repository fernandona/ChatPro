package Model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by mmursith on 12/19/2015.
 */
public class Settings extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        //  AquaFx.style();


        //Parent root = FXMLLoader.load(getClass().getResource("Operator.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Settings.fxml"));
        Parent root = fxmlLoader.load();

        root.setCache(true);
        root.setCacheHint(CacheHint.DEFAULT);

        Scene scene = new Scene(root);//, 550, 605);
       // scene.getStylesheets().add(getClass().getResource("theme.css").toExternalForm());

        System.out.println("ChatController Starting");


        primaryStage.setScene(scene);
        System.out.println("show");
        //FlatterFX.style();
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        primaryStage.setResizable(false);




    }


    public static void main(String[] args) {
        System.out.println("main");
        launch(args);
    }
}