package Model;

import Controller.ChatController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by PPNPERERA on 11/24/2015.
 */
public class UserItem extends GridPane implements   Runnable, EventHandler<MouseEvent> {



    private ImageView userImage;
    private ImageView statusImage; // =========
    private volatile Label thumbUserName;
    private volatile Label thumbUserIcon;
    private Rectangle statusBar;
    private Button closeButton;
    private Image thumbImage;
    private Image statusOnline; // =========
    private Image statusOffline; // =========
    private Image statusIcon;
    private User user;
    private ChatController chatController;
    private static UserItem userItem;  //addded latest
    private volatile Boolean running;
    private Thread blink;

    public UserItem(User user, ChatController controller){

        this.user = user;
        this.chatController = controller;
        this.setPrefSize(210.2,35);
        this.setMaxWidth(210.2);

        this.setHgap(8);
        this.setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
        //

        // ========================================
        /*statusOnline = new Image(getClass().getResourceAsStream("statusOnline.png"));
        statusOffline = new Image(getClass().getResourceAsStream("statusOffline.png"));
        statusImage = new ImageView();
        statusImage.setFitHeight(10);
        statusImage.setFitWidth(10);
        statusImage.setImage(statusOnline);
        statusImage.setX(0);
        statusImage.setY(70);*/
        // ========================================

        thumbImage = new Image(getClass().getResourceAsStream("dummyImage.png"));
        statusOnline = new Image(getClass().getResourceAsStream("userPicOnline.png"));
        statusOffline = new Image(getClass().getResourceAsStream("userPicOffline.png"));
        statusIcon = new Image(getClass().getResourceAsStream("userOnlineIcon.png"));


        userImage = new ImageView();
        userImage.setFitHeight(30);
        userImage.setFitWidth(30);
        //userImage.setImage(thumbImage);
        userImage.setImage(statusOnline);
        userImage.setStyle("-fx-padding:2px;-fx-border-radius:30px; -fx-background-radius:30px;");
        GridPane.setHalignment(userImage, HPos.RIGHT );

        thumbUserName = new Label();
        thumbUserName.setText(user.getUserName());
        thumbUserName.setStyle("-fx-text-fill:#ffffff; -fx-font-size:12px; -fx-font-weight:bold;");
        //thumbUserName.setStyle("-fx-text-color:#fff; -fx-font-size:11px");
        //thumbUserName.setPrefSize(119, 20); //119,20
        thumbUserName.setFont(Font.font(null, FontWeight.BOLD, 14));
        thumbUserName.setMaxWidth(125);
        thumbUserName.setPrefWidth(125);
        GridPane.setHalignment(thumbUserName, HPos.CENTER );

        // thumbUserIcon =new Label();
        statusImage = new ImageView();
        statusImage.setFitHeight(6);
        statusImage.setFitWidth(6);
        statusImage.setImage(statusIcon);
        statusImage.setStyle("-fx-padding:2px;-fx-border-radius:30px; -fx-background-radius:30px;");
        // GridPane.setHalignment(statusImage, HPos.LEFT );



//        ImageView  close = new ImageView(new Image(getClass().getResourceAsStream("close.png")));
//        close.setFitHeight(20);
//        close.setFitWidth(20);
//
//        closeButton = new Button("",close);
//
//        closeButton.setMaxHeight(15);
//        closeButton.setMaxWidth(15);
//        closeButton.setStyle("-fx-background-radius: 100; -fx-border-radius: 100; -fx-background-color: transparent;-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0 0 0 0, 0, 0, 0;");
//        GridPane.setHalignment(closeButton, HPos.LEFT );
//        closeButton.setOnAction(this);
        // = new Image(new Label(getClass().getResourceAsStream("closeButton.png"))); //();

        ImageView close = new ImageView(new Image(getClass().getResourceAsStream("close.png")));
        close.setFitHeight(9);
        close.setFitWidth(9);
        Label closeLabel = new Label("", close);
        closeLabel.setOnMouseClicked(this);
        GridPane.setHalignment(closeLabel, HPos.RIGHT);


        this.addRow(0,userImage);
        this.addRow(0,thumbUserName);
        this.addRow(0,statusImage);
        this.addRow(0,closeLabel);

        this.disableProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(isDisabled()){
                    userImage.setImage(statusOffline);
                    closeLabel.setDisable(false);
                }else{
                    userImage.setImage(statusOnline);
                }
            }
        });

        this.setOnMouseClicked(event -> {
            //    System.out.println("clicked");
            // blink.interrupt();
            //           stop();
            // blink = null;
            //         System.out.println("stopping");
            Platform.runLater(() -> this.thumbUserName.setStyle("-fx-text-fill:#ffffff; -fx-font-size:12px; -fx-font-weight:bold; "));


            //blink = new Blink();

        });

        //   System.out.println("focueed:  "+this.isFocused());
        userItem = this;
    }

    public  void changePicture(boolean isDisabled){
        try{
            if(isDisabled){
                userImage.setImage(statusOffline);
                statusImage.setImage(null);

            }else{
                userImage.setImage(statusOnline);
                statusImage.setImage(statusIcon);

            }
        }
        catch (Exception e){

        }
    }
    /*
        public void changeStatus(boolean isDisabled) {
            if(isDisabled){
             //   statusImage.
            }else{
                statusImage.setImage(statusIcon);


            }
        }
    */
    public void startBlink(){
        blink = new Thread(this, user.getUserName());
        blink.start ();

    }


    @Override
    public void run() {
        if (userItem == null)
            return;

        try{
            try{
                for (int i = 0; i < 3; i++) {
                    if(thumbUserName==null)
                        break;

                    try {
                        this.thumbUserName.setStyle("-fx-text-fill:#ffa500; -fx-font-size:12px; -fx-font-weight:bold;");
                    }
                    catch (NullPointerException e){
                        break;
                    }
                    catch (Exception e){
                        break;
                    }

                    try {
                        blink.sleep(250);
                    } catch (InterruptedException e) {

                    }
                    catch(Exception e){

                    }


                    try {
                        this.thumbUserName.setStyle("-fx-text-fill: #1e90ff; -fx-font-size:12px; -fx-font-weight:bold;");
                    }
                    catch (NullPointerException e){
                        break;
                    }
                    catch (Exception e){
                        break;
                    }

                    try {
                        blink.sleep(250);
                    } catch (InterruptedException e) {
                        ///  e.printStackTrace();

                    }
                    catch(Exception e){

                    }
                    //    System.out.println("Loop: " + running);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            blink = null;

            /*****Change text Color*/
/*            if(chatController!=null && userItem !=null && chatController.getChatUsersList()!=null ){
                try{

                    if(chatController.getChatUsersList().getSelectionModel().getSelectedItem().equals(userItem) ) {

                        this.thumbUserName.setStyle("-fx-text-fill:#696969; -fx-font-size:12px; -fx-font-weight:bold; ");
                    }
                    else {
                        this.thumbUserName.setStyle("-fx-text-fill:#ffa500; -fx-font-size:12px; -fx-font-weight:bold;");

                    }
                }
                catch (NullPointerException e){
                    e.printStackTrace();

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }*/

            try {
                blink.sleep(250);
            } catch (InterruptedException e) {
                ///  e.printStackTrace();

            }
            catch(Exception e){

            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }



    }
    public ImageView getUserImage() {
        return userImage;
    }

    public void setUserImage(ImageView userImage) {
        this.userImage = userImage;
    }

    public Label getThumbUserName() {
        return thumbUserName;
    }

    public void setThumbUserName(Label thumbUserName) {
        this.thumbUserName = thumbUserName;
    }

    public Rectangle getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(Rectangle statusBar) {
        this.statusBar = statusBar;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public void setCloseButton(Button closeButton) {
        this.closeButton = closeButton;
    }

    public Image getStatusOnline(){return statusOnline;}

    public void setStatusOnline(Image statusOnline){this.statusOnline = statusOnline;}

    public Image getStatusOffline(){return statusOffline;}

    public void setStatusOffline(Image statusOffline){this.statusOffline = statusOffline;}

    public Image getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(Image thumbImage) {
        this.thumbImage = thumbImage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChatController getChatController() {
        return chatController;
    }

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }


    @Override
    public void handle(MouseEvent event) {


        //    chatController.setUsername(userItem);
        Platform.runLater(() -> {
            try {
                final UserItem userItem = this;
                chatController.closeChat(userItem);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        //chatController.closeChat((UserItem)event.getTarget());
    }
}
