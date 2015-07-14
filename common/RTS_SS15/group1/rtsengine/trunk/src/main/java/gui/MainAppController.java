package gui;

import features.FeatureMain;
import iocontroller.IOController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.ConfigurationObject;
import model.QueryReturnObject;
import model.TransportObject;
import model.TweetObject;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;

/**
 * Created by Guerki on 16/5/15.
 */
public class MainAppController implements Initializable {
    volatile static ObservableList<TweetObject> displaysTweets = FXCollections.observableArrayList();
    IOController ioController = new IOController();
    FeatureMain featureMain = new FeatureMain();

    @FXML
    Button start;
    @FXML
    ComboBox<String> indexType;
    @FXML
    TextField numberOfTweets;
    @FXML
    TextField wSignificance;
    @FXML
    TextField wSimilarity;
    @FXML
    TextField wFreshness;
    @FXML
    TableColumn usernameCol;
    @FXML
    TableColumn contentCol;
    @FXML
    TableColumn timestampCol;
    @FXML
    TableColumn followersCol;
    @FXML
    TableView<TweetObject> tweetTable;
    @FXML
    ScrollPane scroll;
    @FXML
    TextField queryfield;
    @FXML
    Button analyze;
    @FXML
    Button analyze_all;

    /**
     * Checks for empty results
     */
    Task<Boolean> task = new Task<Boolean>() {
        @Override
        protected Boolean call() throws Exception {
            Boolean message;
            while (true) {
                message = ioController.getMessage();
                if (message) {
                    ioController.putMessage(false);
                    updateValue(true);
                    Thread.sleep(200);
                    updateValue(false);
                }
            }
        }
    };

    public static void emptyResult(){
        System.out.println("emptyResult");
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Results Found");
        alert.setHeaderText(null);
        alert.setContentText("Your Query did not match any Tweets in our database. Please try another one.");
        alert.showAndWait();
        return;
    }

    // Send Queryresults to the GUI
    public static void sendQueryResults(QueryReturnObject result){
        displaysTweets.addAll(result.getResults());
    }

    // Convert Index String to Enum
    private ConfigurationObject.IndexTypes toIndex(String index){
        switch (index) {
            case "AO":
                return ConfigurationObject.IndexTypes.APPEND_ONLY;
            case "TPL":
                return ConfigurationObject.IndexTypes.TRIPLE_POSTING_LIST;
            case "LSII":
                return ConfigurationObject.IndexTypes.LSII;
            default:
                return ConfigurationObject.IndexTypes.APPEND_ONLY;
        }
    }

    public void initialize(URL url, ResourceBundle rsrcs) {
        ioController.loadTweetDictionaryFromDatabase("temp");
        usernameCol.setCellValueFactory(new PropertyValueFactory<TweetObject, String>("username"));
        contentCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        followersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfAuthorFollowers"));
        tweetTable.setItems(displaysTweets);
        ConfigurationObject.setIndexType(ConfigurationObject.IndexTypes.APPEND_ONLY);
        ioController.startAll();
        ioController.collectTweets();
        ioController.saveDatabasePeriodically("temp", 30000);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);

        // start button pushed
        start.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                startButtonPushed();

            }
        });

        // analyze button pushed
        analyze.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                analyzeButtonPushed();
            }
        });

        // analyze_all button pushed
        analyze_all.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                analyzeAllButtonPushed();
            }
        });

        // Listen for changes to the indexType
        indexType.setOnAction(this::indexChanged);

        // Enter Key is Pressed while in a textfield
        numberOfTweets.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        queryfield.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    analyzeButtonPushed();
                }
            }
        });

        wSignificance.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        wFreshness.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        wSimilarity.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });

        /**
         * Creates a new Thread that checks for empty results
         */
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        task.valueProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(task.valueProperty().getValue()){
                    emptyResult();
                }
            }
        });
    }

    // Attach a listener to the index combobox so the indices get filled before anything is searched
    private void indexChanged(ActionEvent event) {
        ConfigurationObject.setIndexType(toIndex(indexType.getSelectionModel().getSelectedItem()));
    }

    private void analyzeButtonPushed(){
        String hashtag = queryfield.getText();
        featureMain.analyze(hashtag);
        //featureMain.analyzeForEachHashtag();

    }

    private void analyzeAllButtonPushed(){
        featureMain.analyzeForEachHashtag();

    }

    private void startButtonPushed() {
        /**
         * Get all the information from the GUI
         */
        int nNumberOfTweets = Integer.parseInt(numberOfTweets.getCharacters().toString());
        int nWSignificance = Integer.parseInt(wSignificance.getCharacters().toString());
        int nWSimilarity = Integer.parseInt(wSimilarity.getCharacters().toString());
        int nWFreshness = Integer.parseInt(wFreshness.getCharacters().toString());
        ConfigurationObject.IndexTypes nIndexType = toIndex(indexType.getSelectionModel().getSelectedItem());
        String queries = queryfield.getText();

        /**
         * Check if data is valid
         */
        if (nWFreshness+nWSignificance+nWSimilarity != 100){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Wrong Weights Ratio");
            alert.setHeaderText(null);
            alert.setContentText("The sum of all the weights needs to equal 100%.");
            alert.showAndWait();
            return;
        }
        if (nNumberOfTweets < 1){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Number of Tweets too small");
            alert.setHeaderText(null);
            alert.setContentText("The Number of Tweets needs to be bigger than 0.");
            alert.showAndWait();
            return;
        }
        /**
         * Save all the parameters into the ConfigurationObject
         */
        ConfigurationObject.setIndexType(nIndexType);
        ConfigurationObject.setNumberOfTweets(nNumberOfTweets);
        ConfigurationObject.setwFreshness((float) nWFreshness / (float) 100);
        ConfigurationObject.setwSignificance((float) nWSignificance / (float) 100);
        ConfigurationObject.setwSimilarity((float) nWSimilarity / (float) 100);
        TransportObject query = new TransportObject(queries, Calendar.getInstance().getTime(), nNumberOfTweets);
        displaysTweets.clear();
        ioController.addTransportObject(query);


    }

    public IOController getIOController(){
        return ioController;
    }
}
