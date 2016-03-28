package Controller;

import Model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import java.util.ArrayList;

/**
 * Created by mmursith on 12/19/2015.
 */
public class SettingsController  implements ChangeListener, EventHandler<KeyEvent> {

    @FXML public Button cancelConfiguration;
    @FXML public Button okConfiguration;

    @FXML public Button cancelVariable;
    @FXML public Button okVariable;

    @FXML public Button addVariable;
    @FXML public Button deleteVariable;
    @FXML public Button settings_closeButton;
    @FXML public ScrollPane tableViewContainer;
    @FXML public Button removeVariableButton;
    @FXML private Button applyConfigurationButton;
    @FXML private  Button applyVariableButton;
    @FXML private TextField destination;
    @FXML private TextField topic;
    @FXML private TextField subscription;
    @FXML private TextField operator;
    @FXML private TextField URL;
    ObservableList<Variable> data;
    private TableView<Variable> tableVariables;
    private Stage parentStage;
    private boolean isError;
    private boolean isConnectedAlready;
    private SettingsController settingController;
    private ChatController chatController;
    private Stage settingsStage;
    private TableColumn variableName;
    private TableColumn variableID;
    public SettingsController(){

    }
    public void setListeners(){
        isError = false;
        isConnectedAlready = false;
        tableVariables = new TableView<>();

        topic.setOnKeyReleased(this);
        operator.setOnKeyReleased((this));
        topic.textProperty().addListener(this);
        topic.textProperty().addListener(this);
        destination.textProperty().addListener(this);
        operator.textProperty().addListener(this);
        subscription.textProperty().addListener(this);
        operator.textProperty().addListener(this);

    }

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }




    public void showSettingsWindow(Stage parentStage) throws Exception {
        //  controller.getStage().toBack();
        //System.out.println(settingsStage);
        // setting.start(settingsStage);

//  System.out.println(settingsStage);
        settingsStage.show();
        double x = parentStage.getX() + parentStage.getWidth() / 2 - settingsStage.getWidth() / 2;
        double y = parentStage.getY() + parentStage.getHeight() / 2 - settingsStage.getHeight() / 2;
        settingsStage.setX(x);
        settingsStage.setY(y);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fillConfiguration();
                fillVariable();
            }
        });
    }

    private void fillVariable() {
        ArrayList<Variable> contextMenuVariables = (ArrayList<Variable>) VariablesController.readVariables();
        data =FXCollections.observableArrayList(contextMenuVariables);
//
//        for (Variable variable :contextMenuVariables) {
//           data.add(variable);
//        }



        variableName = new TableColumn("Variable name");
        variableName.setPrefWidth(270);
        variableName.setEditable(true);

        variableName.setCellValueFactory(new PropertyValueFactory<Variable,String>("Name"));
        variableName.setCellFactory(TextFieldTableCell.forTableColumn());

        variableName.cellFactoryProperty().addListener((observable, oldValue, newValue) -> applyVariableButton.setDisable(false));
        variableName.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Variable, String>> () {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Variable, String> t) {
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setName(t.getNewValue());
                        applyVariableButton.setDisable(false);
                    }
                }
        );

        variableID = new TableColumn("Variable ID");
        variableID.setEditable(true);
        variableID.setPrefWidth(270);

        variableID.setCellValueFactory(new PropertyValueFactory<Variable,String>("ID"));
        variableID.setCellFactory(TextFieldTableCell.forTableColumn());
        variableID.cellFactoryProperty().addListener((observable, oldValue, newValue) -> applyVariableButton.setDisable(false));

        variableID.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Variable, String>> () {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Variable, String> t) {
                        (t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setID(t.getNewValue());
                        applyVariableButton.setDisable(false);
                    }
                }
        );
        tableVariables.setItems(data);
        tableVariables.getColumns().addAll(variableName, variableID);

        tableViewContainer.setContent(tableVariables);
        tableViewContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tableVariables.setEditable(true);
        //System.out.println(tableVariables.getColumns().get(0));

    }



    private void fillConfiguration() {
        Configuration oldConfiguration = ConfigurationController.readConfig();
//        destination;
//        topic;
//        subscription;
//        operator;
//        URL;
//        System.out.println("old Configuration "+ settingController.destination +"    "+ settingController.topic);
        destination.setText(oldConfiguration.getDestination());
        topic.setText(oldConfiguration.getTopic());
        subscription.setText(oldConfiguration.getSubscription());
        operator.setText(oldConfiguration.getOperator());
        URL.setText(oldConfiguration.getURL());
    }




    public void setOkConfiguration(ActionEvent actionEvent) {
        //applyConfiguration();


        if(!applyConfigurationButton.isDisable()  )
            apply();

        if(!isError && !isConnectedAlready) {
            settingsStage.hide();
            settingsStage.close();
        }
    }

    public void applyConfiguration(ActionEvent actionEvent) {
        apply();


    }
    public void apply()  {
        if(isError) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Topic Name Error.\nEg: chat.*");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(settingsStage);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.show();
        }

        {
            NetworkDownHandler networkDownHandler = new NetworkDownHandler();
            networkDownHandler.start();
        }
    }
    public void setOkVariable(ActionEvent actionEvent) {
        if(!okVariable.isDisabled())
            changeVariable();
        settingsStage.hide();
        settingsStage.close();


    }

    public void changeVariable(){

        ArrayList<Variable> previousList = VariablesController.readVariables();
        ObservableList<Variable> currentList = tableVariables.getItems();

        ArrayList<Variable> List = new ArrayList<>();
        for (Variable variable: currentList) {
            if(!variable.getID().trim().equals("") && !variable.getName().trim().equals("")){
                List.add(variable);

            }

        }

        System.out.println(!List.equals(previousList));
        if(!List.equals(previousList)){
            VariablesController.writeVariables(List);
        }

        applyVariableButton.setDisable(true);
        chatController.addMenuItems();

    }
    public void applyVariable(ActionEvent actionEvent) {

        changeVariable();


    }

    public void addVariable(ActionEvent actionEvent) {
        data.add(new Variable("new", "new"));
    }

    public void removeVariable(ActionEvent actionEvent) {
        Variable variable = tableVariables.getSelectionModel().getSelectedItem();
        data.remove(variable);
        //if(removeVariableButton.isDisabled())
        applyVariableButton.setDisable(false);
    }



    public Stage getSettingsStage() {
        return settingsStage;
    }

    public void setSettingsStage(Stage settingsStage) {
        this.settingsStage = settingsStage;
    }

    public void cancel(ActionEvent actionEvent) {
        settingsStage.close();
    }

    public Stage getParentStage() {
        return parentStage;
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        applyConfigurationButton.setDisable(false);
    }

    @Override
    public void handle(KeyEvent event) {

        TextField textField = (TextField)event.getTarget();
        String text = textField.getText();
        String ID = textField.getId();
        String operator= "."+subscription.getText();
        String topicText = topic.getText();
        topicText = topicText.substring(0,topicText.length()-1);

        if(ID.equalsIgnoreCase("operator")) {
            text =text.replaceAll("\\s+","");
            subscription.setText(text);
            destination.setText(topicText+text);

        }
        else if(ID.equalsIgnoreCase("topic")){
            try {
                String prefix = text.replace(".*","");
                destination.setText(prefix + operator);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            String suffix = text.substring(text.length()-2,text.length());
            //   System.out.println(suffix);



            if(text.substring(text.length()-2,text.length()).equals(".*")) {
                isError = false;
                topic.setStyle("-fx-text-fill: black; -fx-background-color:#E7F0F5;");
                destination.setStyle("-fx-text-fill: black;-fx-background-color:#E7F0F5;");


            }
            else {
                isError = true;
                topic.setStyle("-fx-text-fill: red;-fx-background-color:#E7F0F5;");
                destination.setStyle("-fx-text-fill: red;-fx-background-color:#E7F0F5;");

            }

        }



    }


    class NetworkDownHandler extends Thread{
        Image image_offline = new Image(getClass().getResourceAsStream("offline.png")); //===========================
        Image image_online = new Image(getClass().getResourceAsStream("online.png"));   //===========================
        boolean isConnected = false;

        Thread thread = this;
        Configuration currentConfiguration = new Configuration();
        public void run() {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    currentConfiguration.setDestination(destination.getText());
                    currentConfiguration.setTopic(topic.getText());
                    currentConfiguration.setSubscription(subscription.getText());
                    currentConfiguration.setURL(URL.getText());

                    currentConfiguration.setOperator(operator.getText());

                    Operator.messageBrokerUrl = currentConfiguration.getURL();
                }
            });
            chatController.getNetworkHandler().stopThread();
            thread = Thread.currentThread();
            System.out.println(chatController.isOnline());
            String ID = Constant.operatorID;//Constant.getRandomString();
            try{
                Operator operator1 = new Operator(ID, ID);
                isConnected = operator1.isConnected();
            } catch (JMSException e) {
                e.printStackTrace();
            }

            while (!isConnected) {
                try {
                    System.out.println("[INFO] Settings Changed: Trying to resolve ");
                    Operator operator = new Operator(ID, ID);
                    isConnected = operator.isConnected();

                    //         System.out.println("inside:  " + isOnline);
                    if (isConnected) {
                        chatController.statusImageView.setImage(image_online); //==========================
                        chatController.setOnline(true);
                        isConnected = true;
                        System.out.println("Re-connected");
                        operator.closeConnection();
                    }
                    else {
                        chatController.statusImageView.setImage(image_offline);//===========================
                        chatController.setOnline(false);
                        isConnected = false;
                    }
                    //   operator = null;

                } catch (IllegalStateException e) {
                    chatController.setOnline(false);
                    isConnected = false;
                    System.out.println("Offline: Session");
                    System.out.println("Re-connected");
                } catch (JMSException e) {
                    chatController.setOnline(false);
                    isConnected = false;
                    try {

                        thread.sleep(200);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("Offline: JMS");
                    System.out.println("Re-connected");
                }

                try {
                    thread.sleep(200);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if(chatController.isOnline() && !isError){

                System.out.println("Working");
                Configuration previousConfiguraion = ConfigurationController.readConfig();


                if(!previousConfiguraion.equals(currentConfiguration) ){

                    Platform.runLater(() -> ConfigurationController.writeConfig(currentConfiguration));
                    applyConfigurationButton.setDisable(true);
                    String prefix = currentConfiguration.getTopic().replace("*","");
                    Constant.topicPrefix = prefix;
                    Constant.configuration = currentConfiguration;
                    BindOperator bindOperator = chatController.getHashMapOperator().get(chatController.getDefaultOperator());
                    OperatorController previous = bindOperator.getOperatorController();
                    bindOperator.setOperatorController(null);

                    System.out.println("Message in que:         "+ previous.getChatMessagess().size());

                    //  System.out.println("previius:   " + chatController.getHashMapOperator().get(chatController.getDefaultOperator()).getOperatorController());

                    chatController.getHashMapOperator().remove(chatController.getDefaultOperator());
                    chatController.setConfig(currentConfiguration);



/***************   can be done by creating new session*************/
                    OperatorController operatorController = null;
                    try {
                        operatorController.getOperator().messageBrokerUrl = currentConfiguration.getURL();

                        operatorController = new OperatorController(currentConfiguration.getOperator(), currentConfiguration.getTopic(),chatController);
                        System.out.println(operatorController.getOperator().getMessageBrokerUrl());
//                        operatorController.createSession();
//                        operatorController.startDefaultOperatorAction();

                        operatorController.getOperator().create();
                        boolean isItConnected = operatorController.getOperator().isConnected();

                        if(isItConnected) {
                            //operatorController.createSession();
                            isConnectedAlready = false;
                            operatorController.startDefaultOperatorAction();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    settingsStage.close();
                                }
                            });


                        }
                        else{
                            System.out.println("Operator "+currentConfiguration.getOperator() +" is already Connected");
                            isConnectedAlready = true;
                            Platform.runLater(() -> {
                                try {

                                    System.out.println("Showing settings ");
                                    //settingsStage.show();
                                    applyConfigurationButton.setDisable(false);
                                    //  chatController.setOnline(false);
                                    chatController.statusImageView.setImage(image_offline);
                                    if(isConnectedAlready) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR,"Operator "+currentConfiguration.getOperator() +" is already Connected");
                                        alert.initModality(Modality.APPLICATION_MODAL);
                                        alert.initOwner(settingsStage);
                                        alert.initStyle(StageStyle.UNDECORATED);
                                        alert.show();

                                    }
                                    settingsStage.show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        try {

                            previous.closeConnection();

                        } catch(Exception e ){
                            System.out.println(e.getMessage());
                        }

                        try {
                            System.out.println("Stopping thread");
                            bindOperator.getOperatorController().getNetworkHandler().stopThread();
                            // previous.getNetworkHandler().stopThread();

                        } catch(Exception e ){
                            System.out.println(e.getMessage());
                        }

                        try {
                            previous.getOfflineNetworkDownHandler().stopThread();


                        } catch(Exception e ){
                            System.out.println(e.getMessage());
                        }

                        try {

                            previous.getMessageDistributionHandler().stopThread();


                        } catch(Exception e ){
                            System.out.println(e.getMessage());
                        }

                        try {

                            previous.getTimer().cancel();

                        } catch(Exception e ){
                            System.out.println(e.getMessage());
                        }

                        operatorController.setMessageCounter(previous.getMessageCounter());

                        operatorController.setMessageProduceID(previous.getMessageProduceID());
                        operatorController.getMessageProduceID().remove(0);
                        operatorController.getMessageProduceID().add(0,currentConfiguration.getOperator());
                        operatorController.setChatMessagess(previous.getChatMessagess());


                    }
                    catch (JMSException e) {
                        //    e.printStackTrace();
                    }
                    catch (Exception e){
                        //  e.printStackTrace();
                    }

                    chatController.getHashMapOperator().put(currentConfiguration.getOperator(), new BindOperator(operatorController, chatController.getGridPane()) );
                    chatController.setDefaultOperator(currentConfiguration.getOperator());
/******************************/

//            if(chatController.isOnline()){
//                try {
//                    OperatorController operatorController = new OperatorController(currentConfiguration.getOperator(), currentConfiguration.getTopic(),chatController);
//                    chatController.getHashMapOperator().put(chatController.getDefaultOperator(), new BindOperator(operatorController, chatController.getGridPane()));
//                    //   historyController = hashMapOperator.get(config.getSubscription()).getHistoryController();
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
//            }
//           else {


                    //         chatController.setOnline(false);
                    //         chatController.getNetworkHandler().start();

                    //}
//                System.out.println("new" + chatController.getHashMapOperator().get(chatController.getDefaultOperator()).getOperatorController());

                }
                stopThread();
            }





        }

        public  void stopThread(){
            Thread t = thread;
            thread = null;
            t.interrupt();
        }
    }


}
