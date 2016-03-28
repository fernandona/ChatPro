package Controller;

import Model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


public class ChatController{

    @FXML public Button closeButton;//=========================
    @FXML public Button minimizeButton; // ============================

    @FXML public Label statusIcon; // =========================== new
    @FXML public ImageView statusImageView; // ===================== new


    @FXML public Pane titleBar;

    @FXML public Button sendButton;
    @FXML public Button CloseButton;
    @FXML public TextArea messageTextField;
    @FXML public Label Username;

    @FXML public Button AddChatBtn;
    @FXML public ListView<UserItem> chatUsersList;

    @FXML public ScrollPane messageDisplay;
    @FXML public CheckBox doTrain;
    @FXML private ContextMenu variablesMenu;

    private double xOffset;
    private double yOffset;

    private final HashMap<String, BindOperator> hashMapOperator;
    private volatile  ChatController controller =null;

    private GridPane chatHolder;
    private volatile String previousID; // previous opertor message


    final ObservableList<UserItem> listItems = FXCollections.observableArrayList();
    public Vector<String> messageProducerID;
    private Vector<String> operatorProducerID;
    private ArrayList<Variable> contextMenuVariables;
    public static Configuration config;
    private  OperatorController operatorController ;
    private  HistoryController historyController;
    private NetworkDownHandler networkHandler;
    private String defaultOperator;
    private volatile boolean isOnline;
    private Stage stage;
    private SettingsController settingsController;
    private Stage settingStage;


    public ChatController() throws JMSException {
        System.out.println(System.getProperty("os.name"));

        Image image_offline = new Image(getClass().getResourceAsStream("offline.png")); // =========================== NEW
        Image image_online = new Image(getClass().getResourceAsStream("online.png"));   // =========================== NEW


        defaultOperator = Constant.configuration.getOperator();// "operator1";
        messageProducerID = new Vector<>();

        hashMapOperator = new HashMap<>();
        controller =this;
        previousID = null;
        config = ConfigurationController.readConfig();
        Constant.correalationID = Constant.getRandomString();
        Constant.operatorID = Constant.getRandomString();
        System.out.println(Constant.operatorID);
        //operatorController = new OperatorController("operator0", "chat.*",this);
        this.isOnline = false;


        String ID = Constant.operatorID;//Constant.getRandomString();

        try{
            Operator operator = new Operator(config.getOperator(), config.getTopic());
            operator.create();
            boolean isConnected = operator.isConnected();
            operator.closeConnection();

            System.out.println("startup:  " + isConnected);
            if (isConnected) {
                isOnline = true;
                networkHandler = null;
                System.out.println("connected");

            }
            else {
                isOnline = false;
                System.out.println("initial Offline");


//                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    networkHandler = new NetworkDownHandler();
                    networkHandler.start();
                });
//                latch.await();

            }
            if(isOnline) {
                Platform.runLater(() -> {
                    try {
                        operatorController = new OperatorController(config.getOperator(), config.getTopic(), controller);
                        operatorController.createSession();
                        operatorController.startDefaultOperatorAction();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                });
            }

        }
        catch (Exception e){
            isOnline = false;
            System.out.println("initial Offline exception");
            networkHandler = new NetworkDownHandler();
            networkHandler.start();

        }

        //chatBubble = new TextArea();
        chatHolder = new GridPane();
        contextMenuVariables = (ArrayList<Variable>) VariablesController.readVariables();


   //     listItems.add("operator0");
    //    System.out.println(hashMapOperator);



        Platform.runLater(() -> {
        try{

            if(isOnline){
                statusImageView.setImage(image_online);

            }else{
                statusImageView.setImage(image_offline);

            }
            this.closeButton.setContentDisplay(ContentDisplay.CENTER);
            this.closeButton.getStyleClass().add("closeButton");
            this.minimizeButton.getStyleClass().add("minimizeButton");
            this.sendButton.getStyleClass().add("sendButton");

            chatUsersList.setItems(listItems);

            messageTextField.setDisable(true);
            doTrain.setDisable(true);
            sendButton.setDisable(true);
            this.Username.getStyleClass().add("username");
            messageDisplay.setContent(chatHolder);
            messageDisplay.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            NotificationController.stage.initOwner(stage);

            settingStage.initOwner(stage);
            addMenuItems();
            if(isOnline) {
                hashMapOperator.put(defaultOperator, new BindOperator(operatorController, getGridPane()));
                //  historyController = hashMapOperator.get(config.getSubscription()).getHistoryController();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }





        });

        try {
            loadSettings();
            System.out.println("settings loaded");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void getMyMessage() {

        sendButton.setOnMouseClicked(event -> {
            try {
                sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public void sendMessage() throws IOException {

        String myMessage = messageTextField.getText();

        try {
            if(myMessage.equals(""))
                return;
            if(myMessage.trim().equals(""))
                return;
        }
        catch (Exception e){

        }
        ChatMessage myMessageMod = getObjectMessage(myMessage, operatorController.getSubscriptionName());
        try {
            if(myMessage==null)
                return;

//            if(!myMessage.trim().equals("") && !myMessage.trim().equalsIgnoreCase("exit")){

               // int counter =  operatorController.getMessageCounter();
                int ID = operatorController.getIDtracker();
          //      System.out.println("value chat:  "+counter+"     ****"+operatorController);
                OperatorBubble bubble = new OperatorBubble(defaultOperator, myMessageMod.getTextMessage(), myMessageMod.getTime() );
                //       GridPane.setHalignment(bubble.getFromBubble(), HPos.RIGHT);
               // Parent root = bubble.getRoot(defaultOperator, myMessageMod.getTextMessage(), myMessageMod.getTime());
                historyController.writehistory(Constant.ID_O, Constant.configuration.getOperator(), myMessageMod);
                chatHolder.addRow(ID, bubble.getRoot());
                messageDisplay.setContent(chatHolder);

                if(!doTrain.isSelected()){
                    myMessage = Constant.DO_NOT_TRAIN_TAG + myMessage;
                    doTrain.setSelected(true);
                }

                myMessage = getReplacedVariables(myMessage);
                myMessageMod = getObjectMessage(myMessage, operatorController.getSubscriptionName());

                //System.out.println("Sending:     "+hashMapOperator.size() +"       "+operatorController.getSesssion());
                operatorController.sendMessage(myMessageMod, operatorController);

            //    System.out.println("Message sent");
                Thread.sleep(50);
                Platform.runLater(() -> messageDisplay.setVvalue(messageDisplay.getVmax()));


//            }

//            else if(myMessage.trim().equalsIgnoreCase("exit")){
//                if(!operatorController.getSubscriptionName().equals(defaultOperator)) {
//
//                    operatorController.sendMessage(myMessageMod, operatorController);
//                    int counter = (int) operatorController.getMessageCounter();
//                                 System.out.println("value chat:  "+counter+"     ****"+operatorController);
//
//                    OperatorBubble bubble = new OperatorBubble(defaultOperator, myMessageMod.getTextMessage(),myMessageMod.getTime() );
//                    //           GridPane.setHalignment(bubble.getFromBubble(), HPos.RIGHT);
//
//
//                    chatHolder.addRow(counter, bubble.getRoot());
//                    //          chatBubble.appendText("Admin : "+myMessageMod);
//                    messageDisplay.setContent(chatHolder);
//                    Thread.sleep(50);
//                    Platform.runLater(() -> messageDisplay.setVvalue(messageDisplay.getVmax()));
//
//                    controller.closeChat();//closeConnection();
//                }
//                else {
//
//                    System.exit(0);
//                }
//            }



        } catch (JMSException | InterruptedException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        messageTextField.setText("");
    }


    public void sendMessage(ChatMessage chatMessage, OperatorController operatorController) throws IOException {
        String myMessage = chatMessage.getTextMessage();
        ChatMessage myMessageMod = getObjectMessage(myMessage, operatorController.getSubscriptionName());
        try {
            operatorController.sendMessage(myMessageMod, operatorController);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }



    public String getReplacedVariables(String message){
        String replacedmessage = message;

        for (Variable variable:contextMenuVariables) {
            if(replacedmessage.contains("{"+variable.getName()+"}")) {
                String ID = variable.getID();
                String name = "{"+variable.getName()+"}";
                replacedmessage = replacedmessage.replace(name, ID);


            }
        }


        return  replacedmessage;
    }
    // Set usernames from the users list to Header label
    public void setUsername() {

        try {
            String message = messageTextField.getText();
            if(previousID !=null) {
             //   System.out.println("ID Selected:  "+ previousID + "     "+ hashMapOperator.get(previousID));
             //   chatUsersList.getItems().get(messageProducerID.indexOf(previousID)).setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
                if(message!=null)
                    hashMapOperator.get(previousID).setTypedMessage(message.trim());
            }
            messageTextField.setText("");


            UserItem useritem = chatUsersList.getSelectionModel().getSelectedItem();
         //   useritem.setStyle("-fx-background-color:#e7f0f5; -fx-border-color:#e7f0f5;");
            useritem.getThumbUserName().setStyle("-fx-text-fill:#696969; -fx-font-size:12px; -fx-font-weight:bold; ");
            String name = useritem.getUser().getUserName();
            String userID = useritem.getUser().getSubscriptionName();
            previousID = userID;
    //        System.out.println("previousID:     "+previousID);

            Username.setText(name);
            Platform.runLater(() -> {
           //     chatBubble = hashMapOperator.get(name).getTextArea();
       //         System.out.println(chatHolder +"                    ");

                try{
                    messageTextField.setText(hashMapOperator.get(userID).getTypedMessage());
                    chatHolder = hashMapOperator.get(userID).getChatHolder();
                    operatorController = hashMapOperator.get(userID).getOperatorController();
                    historyController = hashMapOperator.get(userID).getHistoryController();
                    messageDisplay.setContent(chatHolder);
                    messageDisplay.setVvalue(messageDisplay.getVmax());

                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }



            });
            if(hashMapOperator.get(userID).getChatHolder().isDisabled()) {
                sendButton.setDisable(true);
                messageTextField.setDisable(true);
                doTrain.setDisable(true);
            }
            else {
                sendButton.setDisable(false);
                messageTextField.setDisable(false);
                doTrain.setDisable(false);
            }


        }

        catch (RuntimeException r ){
        //        r.printStackTrace();
        }
    }

    public void setUsername(UserItem useritem) {

        if(useritem==null)
            return;
        try {
            String message = messageTextField.getText();
            if(previousID !=null) {
               // chatUsersList.getItems().get(messageProducerID.indexOf(previousID)).setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
                chatUsersList.getItems().get(messageProducerID.indexOf(previousID)).getThumbUserName().setStyle("-fx-text-fill:#696969; -fx-font-size:12px; -fx-font-weight:bold; ");
                //   System.out.println("ID Selected:  "+ previousID + "     "+ hashMapOperator.get(previousID));
                if(message!=null)
                    hashMapOperator.get(previousID).setTypedMessage(message.trim());
            }
            messageTextField.setText("");
            //useritem.setStyle("-fx-background-color:#e7f0f5; -fx-border-color:#e7f0f5;");



            String name = useritem.getUser().getUserName();
            String userID = useritem.getUser().getSubscriptionName();
            previousID = userID;
            //        System.out.println("previousID:     "+previousID);

            Username.setText(name);
            operatorController = hashMapOperator.get(userID).getOperatorController();
            historyController = hashMapOperator.get(userID).getHistoryController();
            Platform.runLater(() -> {
                //     chatBubble = hashMapOperator.get(name).getTextArea();
                //         System.out.println(chatHolder +"                    ");

                try{
                    messageTextField.setText(hashMapOperator.get(userID).getTypedMessage());
                    chatHolder = hashMapOperator.get(userID).getChatHolder();
                    messageDisplay.setContent(chatHolder);
                    messageDisplay.setVvalue(messageDisplay.getVmax());
                    //Thread.sleep(10);
                    useritem.getThumbUserName().setStyle("-fx-text-fill:#696969; -fx-font-size:12px; -fx-font-weight:bold; ");

                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }


            });

            if(hashMapOperator.get(userID).getChatHolder().isDisabled()) {
                sendButton.setDisable(true);
                messageTextField.setDisable(true);
                doTrain.setDisable(true);

            }
            else {
                sendButton.setDisable(false);
                messageTextField.setDisable(false);
                doTrain.setDisable(false);
            }

//            try {
//                Thread.sleep(100);
//                Platform.runLater(() -> useritem.getThumbUserName().setStyle("-fx-text-fill:#696969; -fx-font-size:12px; -fx-font-weight:bold; "));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }


        }

        catch (RuntimeException r ){
            //r.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public ChatMessage getObjectMessage(String messageText, String producerID){
        ChatMessage chatMessage =  new ChatMessage();
        chatMessage.setProducerID(producerID);
        chatMessage.setTextMessage(messageText);
        return chatMessage;
    }

    // Get Usernames from TextField and add them to the ArrayList
//    public void setUserList() throws JMSException {
//        System.out.println("Click");
//        String tempName = sampleNameInput.getText();
//        listItems.add(tempName);
//        chatUsersList.setItems(listItems);
//        sampleNameInput.clear();
//        createChatSpace();
//
//    }

    // Remove users from the chat and ArrayList
    public void closeChat() throws JMSException, IOException, InterruptedException {

        int index = chatUsersList.getSelectionModel().getSelectedIndex();
        System.out.println("Closing index: "+ index);
      //  operatorController.closeConnection();

        if(!listItems.isEmpty() ) {

            String myMessage = "exit";
            String bubbleMessage = "Chat closed by Operator";
            ChatMessage myMessageMod = getObjectMessage(myMessage, operatorController.getSubscriptionName());
            ChatMessage bubbleMessageMod = getObjectMessage(bubbleMessage, operatorController.getSubscriptionName());

          //  int counter = (int) operatorController.getMessageCounter();
            int ID = operatorController.getIDtracker();
            OperatorBubble bubble = new OperatorBubble(defaultOperator, bubbleMessageMod.getTextMessage(),bubbleMessageMod.getTime() );

            historyController.writehistory(Constant.ID_O, Constant.configuration.getOperator(), bubbleMessageMod);
            chatHolder.addRow(ID, bubble.getRoot());
            messageDisplay.setContent(chatHolder);
            operatorController.sendMessage(myMessageMod, operatorController);

            UserItem useritem = controller.getListItems().get(index);
            String userID = useritem.getUser().getSubscriptionName();
//            hashMapOperator.remove(name);
 //           hashMapOperator.get(defaultOperator).getOperatorController().getMessageProduceID().remove(name);
            //listItems.remove(index);
            operatorController.closeConnection();
            hashMapOperator.get(userID).getChatHolder().setDisable(true);
            sendButton.setDisable(true);
            messageTextField.setDisable(true);
            doTrain.setDisable(true);
            useritem.setDisable(true);

//            if(index>0) {
//                 useritem = controller.getListItems().get(index-1);
//
//                userID = useritem.getUser().getSubscriptionName();
//
//                //chatBubble = hashMapOperator.get(name).getTextArea();
//                chatHolder = hashMapOperator.get(userID).getChatHolder();
//                operatorController = hashMapOperator.get(userID).getOperatorController();
//                messageDisplay.setContent(chatHolder);
//
//            }
            Thread.sleep(50);
            Platform.runLater(() -> messageDisplay.setVvalue(messageDisplay.getVmax()));

        }


    }


    public void closeChat(UserItem useritem)  {

        try{
            int index = chatUsersList.getSelectionModel().getSelectedIndex();

            //   Platform.runLater(() -> setUsername(useritem));

            System.out.println("Closing index: "+ index);
            //  operatorController.closeConnection();

            if(!listItems.isEmpty()) {

                String myMessage = "exit";
                String bubbleMessage = "Chat closed by Operator";
                ChatMessage myMessageMod = getObjectMessage(myMessage, operatorController.getSubscriptionName());
                ChatMessage bubbleMessageMod = getObjectMessage(bubbleMessage, operatorController.getSubscriptionName());

                //   if(!operatorController.isClosedAlready() && operatorController.isSessionCreated()) {

//            }
                //  System.out.println("exit");
                // int counter = (int) operatorController.getMessageCounter();


/*** add bubble*****/
            /*int ID = operatorController.getIDtracker();
            OperatorBubble bubble = new OperatorBubble(defaultOperator, bubbleMessageMod.getTextMessage(), bubbleMessageMod.getTime() );

            chatHolder.addRow(ID, bubble.getRoot());
            messageDisplay.setContent(chatHolder);*/

                historyController.writehistory(Constant.ID_O, Constant.configuration.getOperator(),myMessageMod);

                //        UserItem useritem = controller.getListItems().get(index);
                String userID = useritem.getUser().getSubscriptionName();
//            hashMapOperator.remove(name);
                //           hashMapOperator.get(defaultOperator).getOperatorController().getMessageProduceID().remove(name);
                //listItems.remove(index);
/**Major Changes**/
//            hashMapOperator.get(userID).getChatHolder().setDisable(true);
//            sendButton.setDisable(true);
//            messageTextField.setDisable(true);
//            doTrain.setDisable(true);
//            useritem.setDisable(true);
                // Thread.sleep(50);

                Platform.runLater(() -> {
                    messageDisplay.setVvalue(messageDisplay.getVmax());
                    if(index>=0 && index< listItems.size()){
                        try{

                            System.out.println("REMOVING:  "+index +"       " + messageProducerID.get(index) +"       SIZE: "+ listItems.size());
                            //         hashMapOperator.remove(messageProducerID.get(index));

                            // chatUsersList.getItems().remove(index);

                            //operatorProducerID.remove(index+1);
                            if(index>0 && (index +1)< listItems.size()){
                                System.out.println("SETTING NEXT USERITEM");
                                try {

                                    UserItem userItem =listItems.get(index +1);
                                    setUsername(userItem);
                                }
                                catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                                catch (ArrayIndexOutOfBoundsException e){
                                    //           e.printStackTrace();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            else if(index==0  ){

                                if(listItems.size()==2) {
                                    System.out.println("SETTING NEXT USERITEM");
                                    try {
                                        UserItem userItem =listItems.get(index +1);
                                        setUsername(userItem);
                                    }
                                    catch (NullPointerException e){
                                        e.printStackTrace();
                                    }
                                    catch (ArrayIndexOutOfBoundsException e){
                                        //           e.printStackTrace();
                                    }
                                    catch (Exception e){
                                                  e.printStackTrace();
                                    }
                                }

                                //messageDisplay.setContent(getGridPane());
                                //messageDisplay.setDisable(true);

                            }
                            else if(listItems.size() == (index+1)  ){

                                if(listItems.size()>0) {
                                    System.out.println("SETTING previous USERITEM");
                                    try {
                                        UserItem userItem =listItems.get(index -1);
                                        setUsername(userItem);
                                    }
                                    catch (NullPointerException e){
                                        e.printStackTrace();
                                    }
                                    catch (ArrayIndexOutOfBoundsException e){
                                        //           e.printStackTrace();
                                    }
                                    catch (Exception e){
                                                  e.printStackTrace();
                                    }
                                }

                                //messageDisplay.setContent(getGridPane());
                                //messageDisplay.setDisable(true);

                            }



                            try {
                                hashMapOperator.remove(messageProducerID.get(index));
                                messageProducerID.remove(index);
                                listItems.remove(index);
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }



                            if(listItems.isEmpty()){
                                sendButton.setDisable(true);
                                messageTextField.setDisable(true);
                                doTrain.setDisable(true);
                                Username.setText("");
                                messageDisplay.setContent(null);

                            }


                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                });

                try{
                    operatorController.sendMessage(myMessageMod, operatorController);
                    operatorController.closeConnection();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }




    }
    public void doSendMessage(Event event) throws IOException {

        if (((KeyEvent)event).getCode().equals(KeyCode.ENTER)){
            sendMessage();
            event.consume();
            //System.out.println("sending");

        }
    }

    public  void addMenuItems(){
        contextMenuVariables = (ArrayList<Variable>) VariablesController.readVariables();
        variablesMenu.getItems().remove(0, variablesMenu.getItems().size());

        for (Variable variable :contextMenuVariables) {
            {
                MenuItem menuitem = new MenuItem(variable.getName());
                menuitem.setId(variable.getID());
                variablesMenu.getItems().add(menuitem);
            }
        }
    }
//    public void createChatSpace() throws JMSException {
//
////        chatBubble.setPrefSize(309,362);
////        chatBubble.setLayoutX(0);
////        chatBubble.setLayoutY(0);
////        chatBubble.setEditable(false);
//
//
//     chatHolder = getGridPane();
//
//        messageDisplay.setContent(chatHolder);
//
//
//   //     isSet = true;
//
//
//
//    }
//    private  void applyDimensions(){
////        chatBubble.setPrefSize(309,362);
////        chatBubble.setLayoutX(0);
////        chatBubble.setLayoutY(0);
////        chatBubble.setEditable(false);
//            chatHolder = getGridPane();
////        chatHolder.setMaxSize(431, 413);
//// //       System.out.println("MessageDisplay"+messageDisplay);
////        //messageDisplay.setFitToWidth(true);
////
////
////        chatHolder.setVgap(7);
////        ColumnConstraints c1 = new ColumnConstraints();
////        c1.setPercentWidth(100);
////        chatHolder.getColumnConstraints().add(c1);
//        messageDisplay.setContent(chatHolder);
//    }

    public void closeAllConnections() throws JMSException {
        if(!listItems.isEmpty()) {
            String myMessage = "exit";
            String bubbleMessage = "Chat closed by Operator";


            for(int index=0; index< listItems.size(); index++) {
  //              UserItem useritem = controller.getListItems().get(index);
//                String name = useritem.getUser().getSubscriptionName();


       //         hashMapOperator.remove(name);
       //         hashMapOperator.get(defaultOperator).getOperatorController().getMessageProduceID().remove(name);
           if(isOnline && !listItems.get(index).isDisabled()) {

               String producerID = messageProducerID.get(index);
               BindOperator bindOperator = hashMapOperator.get(producerID);


               ChatMessage myMessageMod = getObjectMessage(myMessage, bindOperator.getOperatorController().getSubscriptionName());
               ChatMessage bubbleMessageMod = getObjectMessage(bubbleMessage, operatorController.getSubscriptionName());
               if(!bindOperator.getOperatorController().isClosedAlready() && bindOperator.getOperatorController().isSessionCreated()) {
                //   bindOperator.getOperatorController().sendMessage(myMessageMod, bindOperator.getOperatorController());

               }
               //int counter = (int) bindOperator.getOperatorController().getMessageCounter();
            //   OperatorBubble bubble = new OperatorBubble(defaultOperator, bubbleMessageMod.getTextMessage(), bubbleMessageMod.getTime() );
               bindOperator.getHistoryController().writehistory(Constant.ID_O, Constant.configuration.getOperator(),bubbleMessageMod);
               //bindOperator.getChatHolder().addRow(counter, bubble.getRoot());

           }
        ///        hashMapOperator.get(defaultOperator).getOperatorController().closeConnection();
             //   hashMapOperator.get(defaultOperator).getOperatorController().getExecutor().shutdown();
              //  while(! hashMapOperator.get(defaultOperator).getOperatorController().getExecutor().isTerminated()){}
       //         listItems.remove(index);
                System.out.println("Removed");
            }
        }
        System.exit(0);

    }
    public void setSelectedItem(ActionEvent actionEvent) {
        String item = " {"+((MenuItem)(actionEvent.getTarget())).getText()+"}";
        messageTextField.appendText(item);

    }
    public synchronized ChatController getInstance(){
        if(controller==null){
            controller = this;
        }
        return  controller;
    }


    public void closeApp() throws JMSException {
        controller.closeAllConnections();
        System.exit(0);
        //closeButton.setOnMouseClicked(event -> System.exit(0));

        //closeLabel.setOnMouseReleased(event -> System.exit(0));
    }
//
//    public void minimizeApp() throws JMSException {
//       // stage.setFocused(false);
//        //System.out.println(stage.isFocused());
//
//        minimizeLabel.setOnMouseClicked(event -> {
//
//
//
//        });
//
//        //minimizeLabel.setOnMouseClicked(event -> stage.setIconified(true));
//        //minimizeLabel.setOnMouseReleased(event -> stage.setIconified(true));
//        //minimizeLabel.setOnMousePressed(event -> System.out.println("minimized"));
//    }

    static boolean firstClick = false;
    public void moveApp(){
        titleBar.setOnMousePressed(event -> {
            if(!firstClick){
                firstClick = true;
            }else{
                //Do nothing
            }
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        titleBar.setOnMouseDragged(event -> {
            if(!firstClick){
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
                firstClick=true;
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }else{
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }

        });
    }


    public void minimizeApp(Event event) {
        stage.setIconified(true);

    }

    public void showSettings(Event event) {
//        settingStage.show();
        try {
//            if(settingStage.getOwner()==null)
//                    settingStage.initOwner(stage);

            settingsController.showSettingsWindow(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadSettings() throws IOException {
        settingStage = new Stage();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Settings.fxml"));
        Parent root = fxmlLoader.load();
        settingsController = fxmlLoader.<SettingsController>getController();
        settingsController.setChatController(this);
        settingsController.setSettingsStage(settingStage);
        settingsController.setListeners();
        root.setCache(true);
        root.setCacheHint(CacheHint.DEFAULT);

        Scene scene = new Scene(root);//, 550, 605);
//            SettingsController chatController = fxmlLoader.<SettingsController>getController();
//            chatController.setStage(primaryStage);
        settingStage.setScene(scene);
        Image ico = new Image(getClass().getResourceAsStream("settingIcon.png"));
        settingStage.getIcons().add(ico);

        System.out.println("show");
        //FlatterFX.style();

        settingStage.initStyle(StageStyle.UNDECORATED);

        settingStage.initModality(Modality.APPLICATION_MODAL);


        settingStage.setResizable(false);
    }



    public GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        //gridPane.setMaxSize(431, 413);
        int width = 400;
        gridPane.setPrefWidth(width);
        gridPane.setMinWidth(width);
        gridPane.setMaxWidth(width);
        gridPane.setPrefHeight(413);
        gridPane.setVgap(7);
        ColumnConstraints c1 = new ColumnConstraints();

        c1.setPercentWidth(100);
        gridPane.getColumnConstraints().add(c1);

        return gridPane;
    }

    public GridPane getChatHolder() {
        return chatHolder;
    }

    public void setChatHolder(GridPane chatHolder) {
        this.chatHolder = chatHolder;
    }

    public HistoryController getHistoryController() {
        return historyController;
    }

    public void setHistoryController(HistoryController historyController) {
        this.historyController = historyController;
    }

    public OperatorController getOperatosrController() {
        return operatorController;
    }

    public void setOperatorController(OperatorController operatorController) {
        this.operatorController = operatorController;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public void setSendButton(Button sendButton) {
        this.sendButton = sendButton;
    }

    public Button getCloseButton() {
        return CloseButton;
    }

    public void setCloseButton(Button closeButton) {
        CloseButton = closeButton;
    }

    public TextArea getMessageTextField() {
        return messageTextField;
    }

    public void setMessageTextField(TextArea messageTextField) {
        this.messageTextField = messageTextField;
    }

    public Label getUsername() {
        return Username;
    }

    public void setUsername(Label username) {
        Username = username;
    }

    public String getDefaultOperator() {
        return defaultOperator;
    }

    public void setDefaultOperator(String defaultOperator) {
        this.defaultOperator = defaultOperator;
    }
    public Button getAddChatBtn() {
        return AddChatBtn;
    }

    public void setAddChatBtn(Button addChatBtn) {
        AddChatBtn = addChatBtn;
    }

    public synchronized ListView<UserItem> getChatUsersList() {
        return chatUsersList;
    }

    public void setChatUsersList(ListView<UserItem> chatUsersList) {
        this.chatUsersList = chatUsersList;
    }

    public NetworkDownHandler getNetworkHandler() {
        return new NetworkDownHandler();
    }

    public void setNetworkHandler(NetworkDownHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    public CheckBox getDoTrain() {
        return doTrain;
    }

    public ScrollPane getMessageDisplay() {
        return messageDisplay;
    }

    public void setMessageDisplay(ScrollPane messageDisplay) {
        this.messageDisplay = messageDisplay;
    }

    public synchronized ObservableList<UserItem> getListItems() {
        return listItems;
    }

    public synchronized HashMap<String, BindOperator> getHashMapOperator() {
        return hashMapOperator;
    }

    public synchronized Vector<String> getMessageProducerID() {
        return messageProducerID;
    }

    public void setMessageProducerID(Vector<String> messageProducerID) {
        this.messageProducerID = messageProducerID;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public String getPreviousID() {
        return previousID;
    }

    public void setPreviousID(String previousID) {
        this.previousID = previousID;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setStage(Stage stage) {
       // System.out.println("setting scene");
      //  System.out.println(hashMapOperator);

        this.stage = stage;
    }

    public void showLastmessage(Event event) {
        Platform.runLater(() -> messageDisplay.setVvalue(messageDisplay.getVmax()));

    }


    class NetworkDownHandler extends Thread{
        Image image_offline = new Image(getClass().getResourceAsStream("offline.png")); //===========================
        Image image_online = new Image(getClass().getResourceAsStream("online.png"));   //===========================
        Thread thread = this;
        volatile boolean isStopped = false;

        public void run() {
            thread = Thread.currentThread();
            System.out.println(isOnline);
            String ID = Constant.operatorID;//Constant.getRandomString();
                while (!isOnline && !isStopped) {
                    try {
                        System.out.println("Trying to resolve");
                        Operator operator = new Operator(ID, ID);
                        operator.create();
                        boolean isConnected = operator.isConnected();


                        //         System.out.println("inside:  " + isOnline);
                        if (isConnected) {
                            statusImageView.setImage(image_online); //==========================
                            isOnline = true;
                            System.out.println("Server is Online");
                            operator.closeConnection();
                        }
                        else {
                            statusImageView.setImage(image_offline);//===========================
                            isOnline = false;
                        }
                        try {
                            thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                    } catch (IllegalStateException e) {
                        isOnline = false;
                        try {
                            thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("Offline: Session");
                        System.out.println("Re-connected");
                    } catch (JMSException e) {
                        isOnline = false;
                        try {

                            thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("Offline: JMS");
                        System.out.println("Re-connected");
                    }
                    catch (NullPointerException e){

                    }
                }

            if(isOnline){

                try {
                    OperatorController operatorController = new OperatorController(config.getOperator(), config.getTopic(),controller);

                    operatorController.getOperator().create();
                    boolean isItConnected = operatorController.getOperator().isConnected();

                    if(isItConnected) {
                        //operatorController.createSession();
                        System.out.println("Re-connected and put the operator");
                        operatorController.startDefaultOperatorAction();
                    }
                    else{
                        System.out.println("Operator "+config.getOperator() +" is already Connected");
                        Platform.runLater(() -> {
                            try {

                               settingsController.showSettingsWindow(stage);
                                statusImageView.setImage(image_offline);
                              //  setOnline(false);
                                    Alert alert = new Alert(Alert.AlertType.ERROR,"Operator "+config.getOperator() +" is already Connected.\nPlease Change the Operator Name");
                                    alert.initModality(Modality.APPLICATION_MODAL);
                                    alert.initOwner(settingStage);
                                    alert.initStyle(StageStyle.UNDECORATED);
                                    alert.show();



                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    System.out.println("from network      "+operatorController.getSesssion());
                    hashMapOperator.put(config.getOperator(), new BindOperator(operatorController, getGridPane()));
                    setUsername(chatUsersList.getSelectionModel().getSelectedItem());


                 //   historyController = hashMapOperator.get(config.getSubscription()).getHistoryController();
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                if(controller.isOnline()){
                    for (int index = 0; index < messageProducerID.size(); index++) {

                 //       UserItem useritem = controller.getListItems().get(index);
                        String producerID =messageProducerID.get(index);
                        System.out.println("producerID:     "+producerID);
//                        OperatorController operatorController = null;
//                        try {
//                            operatorController = new OperatorController(producerID, "chat." + producerID, controller);
//                        } catch (JMSException e) {
//                            e.printStackTrace();
//                        }
//                        int count =   controller.getHashMapOperator().get(producerID).getOperatorController().getMessageCounter() -1;
//
//                        operatorController.setMessageCounter(count);
//                        ChatMessage chat = new ChatMessage();
//                        chat.setTextMessage("sdfsdfssdgs");
                        //  operatorController.sendMessage(chat, operatorController);
                        System.out.println("Printting hash:  "+  controller.getHashMapOperator().get(producerID).getOperatorController().getSesssion());
                    }
                }


            }

            //stopThread();
        }

        public  void stopThread(){
            try{
                isStopped = true;
                //thread.stop();
//                Thread t = thread;
//                thread = null;
//                t.interrupt();
            }
            catch (Exception e){
                    e.printStackTrace();
            }
        }
    }






}











